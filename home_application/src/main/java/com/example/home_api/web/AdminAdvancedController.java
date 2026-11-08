package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminAdvancedController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final AdminAuditService audit;

    public AdminAdvancedController(JdbcTemplate jdbc, PasswordEncoder encoder, AdminAuditService audit) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.audit = audit;
    }

    @GetMapping("/users/new")
    String newUser(Model model) {
        model.addAttribute("groups", jdbc.queryForList("SELECT group_id,group_name FROM \"group\" WHERE status='ACTIVE' ORDER BY group_name"));
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    @Transactional
    String createUser(@RequestParam String userId, @RequestParam String userName, @RequestParam String password, @RequestParam String confirmation, @RequestParam(required = false) List<String> groupIds, @RequestParam Map<String,String> params, Authentication auth, Model model) {
        if (userId.isBlank() || userName.isBlank() || password.length() < 8 || !password.equals(confirmation) || jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=?", Integer.class, userId) > 0) {
            model.addAttribute("error", "入力値、パスワード確認、またはユーザーIDの重複を確認してください。");
            model.addAttribute("groups", jdbc.queryForList("SELECT group_id,group_name FROM \"group\" WHERE status='ACTIVE' ORDER BY group_name"));
            return "admin/user-form";
        }
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("INSERT INTO \"user\"(user_id,user_password,user_name,permission_flag,approve_flag,delete_flag,password_change_required,approve_date,create_date,update_date) VALUES (?,?,?,1,1,0,0,?,?,?)", userId, encoder.encode(password), userName, now, now, now);
        if (groupIds != null) for (String groupId : new LinkedHashSet<>(groupIds)) {
            if (groupId == null || groupId.isBlank()) continue;
            int leader = "1".equals(params.get("role_" + groupId)) ? 1 : 0;
            jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,approve_date,create_date,update_date) SELECT group_id,?,1,?,?,?,? FROM \"group\" WHERE group_id=? AND status='ACTIVE'", userId, leader, now, now, now, groupId);
        }
        audit.record(auth.getName(), "POST", "/admin/users/new", "USER_CREATE", userId, "管理者によるユーザー登録");
        return "redirect:/admin/users/" + userId;
    }

    @PostMapping("/users/{id}/update")
    @Transactional
    String updateUser(@PathVariable String id, @RequestParam String userId, @RequestParam String userName, Authentication auth) {
        if(!userId.matches("[A-Za-z0-9_-]{4,64}")||(!userId.equals(id)&&jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=?",Integer.class,userId)>0))return "redirect:/admin/users/"+id+"?error=duplicateId";
        jdbc.update("UPDATE \"user\" SET user_id=?,user_name=?,update_date=CURRENT_TIMESTAMP WHERE user_id=?", userId,userName,id);
        audit.record(auth.getName(), "POST", "/admin/users/" + id + "/update", "USER_UPDATE", id, "ユーザー表示名更新");
        return "redirect:/admin/users/" + userId + "?updated";
    }

    @PostMapping("/users/{id}/reset-password")
    String resetPassword(@PathVariable String id, @RequestParam String password, @RequestParam String confirmation, Authentication auth) {
        if (password.length() < 8 || !password.equals(confirmation))
            return "redirect:/admin/users/" + id + "?error=password";
        jdbc.update("UPDATE \"user\" SET user_password=?,password_change_required=0,password_changed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=?", encoder.encode(password), id);
        jdbc.update("DELETE FROM auto_login_token WHERE user_id=?",id);
        audit.record(auth.getName(), "POST", "/admin/users/" + id + "/reset-password", "PASSWORD_RESET", id, "パスワード再設定");
        return "redirect:/admin/users/" + id + "?updated";
    }

    @PostMapping("/users/{id}/unlock")
    String unlock(@PathVariable String id, Authentication auth) {
        jdbc.update("UPDATE \"user\" SET login_failure_count=0,locked_until=NULL,update_date=CURRENT_TIMESTAMP WHERE user_id=?", id);
        audit.record(auth.getName(), "POST", "/admin/users/" + id + "/unlock", "ACCOUNT_UNLOCK", id, "アカウントロック解除");
        return "redirect:/admin/users/" + id + "?updated";
    }

    @PostMapping("/users/{id}/delete")
    @Transactional
    String deleteUser(@PathVariable String id, Authentication auth) {
        if(!Boolean.TRUE.equals(jdbc.queryForObject("SELECT delete_flag=1 FROM \"user\" WHERE user_id=?",Boolean.class,id)))return "redirect:/admin/users/"+id+"?error=disableFirst";
        if (isLastAdmin(id) || Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM group_member gm WHERE gm.user_id=? AND gm.leader_flag=1 AND gm.approve_flag=1 AND NOT EXISTS(SELECT 1 FROM group_member other JOIN \"user\" u ON u.user_id=other.user_id WHERE other.group_id=gm.group_id AND other.user_id<>gm.user_id AND other.leader_flag=1 AND other.approve_flag=1 AND u.approve_flag=1 AND u.delete_flag=0))", Boolean.class, id)))
            return "redirect:/admin/users/" + id + "?error=protected";
        jdbc.update("UPDATE \"user\" SET delete_flag=1,approve_flag=0,delete_date=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=?", id);
        jdbc.update("UPDATE group_member SET approve_flag=0,leader_flag=0,removed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=?", id);
        audit.record(auth.getName(), "POST", "/admin/users/" + id + "/delete", "USER_DELETE", id, "論理削除");
        return "redirect:/admin/users";
    }

    @GetMapping("/groups/new")
    String newGroup(Model model) {
        model.addAttribute("users", jdbc.queryForList("SELECT user_id,user_name FROM \"user\" WHERE approve_flag=1 AND delete_flag=0 AND permission_flag<>2 ORDER BY user_name"));
        return "admin/group-form";
    }

    @PostMapping("/groups/new")
    @Transactional
    String createGroup(@RequestParam String groupId, @RequestParam String groupName, @RequestParam String password, @RequestParam String confirmation, @RequestParam(required=false) List<String> memberIds, @RequestParam Map<String,String> params, @RequestParam(defaultValue = "ACTIVE") String status, Authentication auth, Model model) {
        List<String> members=memberIds==null?new ArrayList<>():new ArrayList<>(new LinkedHashSet<>(memberIds));
        boolean validMembers=!members.isEmpty()&&members.stream().allMatch(id->jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=? AND approve_flag=1 AND delete_flag=0 AND permission_flag<>2",Integer.class,id)>0);
        boolean hasLeader=members.stream().anyMatch(id->"1".equals(params.get("role_"+id)));
        if (groupId.isBlank() || groupName.isBlank() || password.length() < 8 || !password.equals(confirmation) || !validMembers || !hasLeader || jdbc.queryForObject("SELECT count(*) FROM \"group\" WHERE group_id=?", Integer.class, groupId) > 0) {
            model.addAttribute("error", "入力値、パスワード確認、ID重複、またはリーダーの状態を確認してください。");
            model.addAttribute("users", jdbc.queryForList("SELECT user_id,user_name FROM \"user\" WHERE approve_flag=1 AND delete_flag=0 AND permission_flag<>2 ORDER BY user_name"));
            return "admin/group-form";
        }
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("INSERT INTO \"group\"(group_id,group_password,group_name,status,create_date,update_date) VALUES (?,?,?,?,?,?)", groupId, encoder.encode(password), groupName, status, now, now);
        for(String memberId:members){int leader="1".equals(params.get("role_"+memberId))?1:0;jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,approve_date,create_date,update_date) VALUES (?,?,1,?,?,?,?)", groupId, memberId, leader, now, now, now);}
        for (int i = 0; i < GroupDefaults.CATEGORIES.size(); i++)
            jdbc.update("INSERT INTO category(id,group_id,category_no,category_name,disabled_flag,create_date,update_date) VALUES (?,?,?,?,0,?,?)", "cat-" + UUID.randomUUID().toString().substring(0, 12), groupId, i + 1, GroupDefaults.CATEGORIES.get(i), now, now);
        for (int i = 0; i < GroupDefaults.PAYMENT_METHODS.size(); i++)
            jdbc.update("INSERT INTO payment_method(id,group_id,payment_no,payment_name,disabled_flag,create_date,update_date) VALUES (?,?,?,?,0,?,?)", "pay-" + UUID.randomUUID().toString().substring(0, 12), groupId, i + 1, GroupDefaults.PAYMENT_METHODS.get(i), now, now);
        audit.record(auth.getName(), "POST", "/admin/groups/new", "GROUP_CREATE", groupId, groupName);
        return "redirect:/admin/groups/" + groupId;
    }

    @GetMapping("/groups/{id}")
    String group(@PathVariable String id, @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String memberStatuses, @RequestParam(defaultValue = "") String memberRoles, @RequestParam(defaultValue = "") String memberApprovals, @RequestParam(defaultValue = "") String memberCreated, @RequestParam(defaultValue = "user_name") String sort, @RequestParam(defaultValue = "asc") String direction, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue="1") int logPage, Model model) {
        var rows = jdbc.queryForList("SELECT * FROM \"group\" WHERE group_id=?", id);
        if (rows.isEmpty()) return "redirect:/admin/groups";
        int size = 20;
        try {
            size = Math.max(1, Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='page_size'", String.class)));
        } catch (Exception ignored) {
        }
        page = Math.max(1, page);
        Map<String,String> memberSorts=Map.of("user_id","u.user_id","user_name","u.user_name","status","u.delete_flag","leader_flag","gm.leader_flag","approve_flag","gm.approve_flag","create_date","gm.create_date");
        if (!memberSorts.containsKey(sort)) sort = "user_name";
        direction = direction.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String like = "%" + q + "%", createdLike="%"+memberCreated+"%", dateFormat=postgresDateTimeFormat();
        String where = " WHERE gm.group_id=? AND (LOWER(u.user_id) LIKE LOWER(?) OR LOWER(u.user_name) LIKE LOWER(?)) AND (?='' OR CASE WHEN u.delete_flag=1 THEN 'INACTIVE' ELSE 'ACTIVE' END=ANY(string_to_array(?,','))) AND (?='' OR gm.leader_flag::text=ANY(string_to_array(?,','))) AND (?='' OR gm.approve_flag::text=ANY(string_to_array(?,','))) AND (?='' OR to_char(gm.create_date,?) ILIKE ?)";
        Object[] memberArgs={id,like,like,memberStatuses,memberStatuses,memberRoles,memberRoles,memberApprovals,memberApprovals,memberCreated,dateFormat,createdLike};
        Integer total = jdbc.queryForObject("SELECT count(*) FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id" + where, Integer.class, memberArgs);
        model.addAttribute("group", rows.get(0));
        int logSize=50;try{logSize=Math.max(1,Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='log_page_size'",String.class)));}catch(Exception ignored){}logPage=Math.max(1,logPage);Integer groupLogTotal=jdbc.queryForObject("SELECT count(*) FROM operation_log WHERE target_id=? OR detail ILIKE ?",Integer.class,id,"%"+id+"%");
        model.addAttribute("groupLogs",OperationLogLabels.enrich(jdbc.queryForList("SELECT * FROM operation_log WHERE target_id=? OR detail ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?",id,"%"+id+"%",logSize,(logPage-1)*logSize)));model.addAttribute("logPage",logPage);model.addAttribute("logPages",Math.max(1,(int)Math.ceil(groupLogTotal/(double)logSize)));
        List<Object> memberPaged=new ArrayList<>(Arrays.asList(memberArgs));memberPaged.add(size);memberPaged.add((page-1)*size);
        model.addAttribute("members", jdbc.queryForList("SELECT gm.*,u.user_name,u.permission_flag,u.delete_flag FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id" + where + " ORDER BY " + memberSorts.get(sort) + " " + direction + " NULLS LAST LIMIT ? OFFSET ?", memberPaged.toArray()));
        model.addAttribute("candidates", jdbc.queryForList("SELECT user_id,user_name FROM \"user\" WHERE approve_flag=1 AND delete_flag=0 AND permission_flag<>2 ORDER BY user_name"));
        model.addAttribute("q", q);
        model.addAttribute("memberStatuses", memberStatuses);
        model.addAttribute("memberRoles", memberRoles);
        model.addAttribute("memberApprovals", memberApprovals);
        model.addAttribute("memberCreated", memberCreated);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction.toLowerCase());
        model.addAttribute("page", page);
        model.addAttribute("pages", Math.max(1, (int) Math.ceil(total / (double) size)));
        model.addAttribute("memberTotal", total);
        return "admin/group-details";
    }

    @PostMapping("/groups/{id}/update")
    String updateGroup(@PathVariable String id, @RequestParam String groupId, @RequestParam String groupName, Authentication auth) {
        if(!groupId.matches("[A-Za-z0-9_-]{4,64}")||(!groupId.equals(id)&&jdbc.queryForObject("SELECT count(*) FROM \"group\" WHERE group_id=?",Integer.class,groupId)>0))return "redirect:/admin/groups/"+id+"?error=duplicateId";
        jdbc.update("UPDATE \"group\" SET group_id=?,group_name=?,update_date=CURRENT_TIMESTAMP WHERE group_id=?", groupId,groupName,id);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id + "/update", "GROUP_UPDATE", id, groupName);
        return "redirect:/admin/groups/" + groupId + "?updated";
    }

    @PostMapping("/groups/{id}/member")
    @Transactional
    String addMember(@PathVariable String id, @RequestParam String userId, @RequestParam(defaultValue = "0") int leader, Authentication auth) {
        if(Boolean.TRUE.equals(jdbc.queryForObject("SELECT permission_flag=2 FROM \"user\" WHERE user_id=?",Boolean.class,userId)))return "redirect:/admin/groups/"+id+"?error=adminMember";
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,approve_date,create_date,update_date) VALUES (?,?,1,?,?,?,?) ON CONFLICT(group_id,user_id) DO UPDATE SET approve_flag=1,leader_flag=EXCLUDED.leader_flag,removed_at=NULL,approve_date=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP", id, userId, leader, now, now, now);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id + "/member", "MEMBER_ADD", userId, id);
        return "redirect:/admin/groups/" + id;
    }

    @PostMapping("/groups/{id}/members/{user}/approve")
    String approveMember(@PathVariable String id, @PathVariable String user, Authentication auth) {
        jdbc.update("UPDATE group_member SET approve_flag=1,approve_date=CURRENT_TIMESTAMP,removed_at=NULL,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=?", id, user);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id, "MEMBER_APPROVE", user, id);
        return "redirect:/admin/groups/" + id;
    }

    @PostMapping("/groups/{id}/members/{user}/leader")
    @Transactional
    String toggleLeader(@PathVariable String id, @PathVariable String user, Authentication auth) {
        Integer current = jdbc.queryForObject("SELECT leader_flag FROM group_member WHERE group_id=? AND user_id=?", Integer.class, id, user);
        if (current != null && current == 1 && jdbc.queryForObject("SELECT count(*) FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id WHERE gm.group_id=? AND gm.leader_flag=1 AND gm.approve_flag=1 AND u.approve_flag=1 AND u.delete_flag=0", Integer.class, id) <= 1)
            return "redirect:/admin/groups/" + id + "?error=lastLeader";
        jdbc.update("UPDATE group_member SET leader_flag=CASE WHEN leader_flag=1 THEN 0 ELSE 1 END,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=? AND approve_flag=1", id, user);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id, "LEADER_CHANGE", user, id);
        return "redirect:/admin/groups/" + id;
    }

    @PostMapping("/groups/{id}/members/{user}/remove")
    @Transactional
    String removeMember(@PathVariable String id, @PathVariable String user, Authentication auth) {
        Integer leader = jdbc.queryForObject("SELECT leader_flag FROM group_member WHERE group_id=? AND user_id=?", Integer.class, id, user);
        if (leader != null && leader == 1 && jdbc.queryForObject("SELECT count(*) FROM group_member WHERE group_id=? AND leader_flag=1 AND approve_flag=1", Integer.class, id) <= 1)
            return "redirect:/admin/groups/" + id + "?error=lastLeader";
        jdbc.update("UPDATE group_member SET approve_flag=0,leader_flag=0,removed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=?", id, user);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id, "MEMBER_REMOVE", user, id);
        return "redirect:/admin/groups/" + id;
    }

    @PostMapping("/groups/{id}/status")
    String groupStatus(@PathVariable String id, @RequestParam String status, Authentication auth) {
        jdbc.update("UPDATE \"group\" SET status=?,update_date=CURRENT_TIMESTAMP WHERE group_id=?", status, id);
        if (!status.equals("ACTIVE"))
            jdbc.update("UPDATE group_member SET approve_flag=0,removed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE group_id=?", id);
        audit.record(auth.getName(), "POST", "/admin/groups/" + id + "/status", "GROUP_STATUS", id, status);
        return "redirect:/admin/groups/" + id;
    }

    @PostMapping("/groups/{id}/delete")
    @Transactional
    String deleteGroup(@PathVariable String id,Authentication auth){if(!Boolean.TRUE.equals(jdbc.queryForObject("SELECT status='INACTIVE' FROM \"group\" WHERE group_id=?",Boolean.class,id)))return "redirect:/admin/groups/"+id+"?error=disableFirst";jdbc.update("UPDATE group_member SET approve_flag=0,leader_flag=0,removed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE group_id=?",id);jdbc.update("UPDATE \"group\" SET status='DELETED',update_date=CURRENT_TIMESTAMP WHERE group_id=?",id);audit.record(auth.getName(),"POST","/admin/groups/"+id+"/delete","GROUP_DELETE",id,"所属グループ削除");return "redirect:/admin/groups";}

    @GetMapping("/settings")
    String settings(Model model) {
        model.addAttribute("settings", jdbc.queryForList("SELECT * FROM system_setting ORDER BY key"));
        return "admin/settings";
    }

    @GetMapping("/settings/display")
    String displaySettings(Model model) {
        model.addAttribute("settings", jdbc.queryForList("""
                SELECT * FROM system_setting WHERE key LIKE 'admin_%color' OR key='admin_font_family' OR key LIKE 'user_%color' OR key='user_font_family'
                ORDER BY CASE WHEN key LIKE '%header_color' AND key NOT LIKE '%maintenance%' THEN 1 WHEN key LIKE '%maintenance_header_color' THEN 2 WHEN key LIKE '%body_color' THEN 3 WHEN key LIKE '%font_family' THEN 4 WHEN key LIKE '%font_size' THEN 5 ELSE 99 END,key
                """));
        model.addAttribute("uploadedFonts", jdbc.queryForList("SELECT value AS id,description AS name FROM system_setting WHERE key LIKE 'uploaded_font.%' ORDER BY updated_at DESC"));
        model.addAttribute("commonSettings", jdbc.queryForList("SELECT * FROM system_setting WHERE key IN ('ui_scale','font_size') ORDER BY CASE key WHEN 'ui_scale' THEN 1 ELSE 2 END"));
        return "admin/display-settings";
    }

    @GetMapping("/settings/system")
    String systemSettings(Model model,HttpSession session) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM system_setting WHERE NOT(key LIKE 'admin_%color' OR key LIKE 'admin_font_%' OR key LIKE 'user_%color' OR key LIKE 'user_font_%' OR key LIKE 'uploaded_font.%' OR key='uploaded_font' OR key IN ('ui_scale','font_size')) ORDER BY key");
        Map<String,String> names=Map.of("display","一覧・表示","notification","通知","log","ログ","backup","バックアップ","security","認証・セキュリティ","datetime","日時");Map<String,List<Map<String,Object>>> categories=new LinkedHashMap<>();for(String id:List.of("display","notification","log","backup","security","datetime"))categories.put(id,new ArrayList<>());for(Map<String,Object>row:rows)categories.get(systemCategoryId(String.valueOf(row.get("key")))).add(row);categories.get("log").sort(Comparator.comparingInt(row->"log_page_size".equals(String.valueOf(row.get("key")))?0:1));categories.get("notification").sort(Comparator.comparingInt(row->switch(String.valueOf(row.get("key"))){case "line_enabled"->0;case "line_url"->1;case "line_token"->2;case "line_destination_id"->3;default->9;}));List<Map<String,Object>> cards=new ArrayList<>();categories.forEach((id,settings)->{Map<String,Object>card=new LinkedHashMap<>();card.put("id",id);card.put("name",names.get(id));card.put("settings",settings);cards.add(card);});model.addAttribute("settingCards",cards);
        model.addAttribute("notificationTestSuccess",session.getAttribute("notificationTestSuccess"));model.addAttribute("notificationTestError",session.getAttribute("notificationTestError"));session.removeAttribute("notificationTestSuccess");session.removeAttribute("notificationTestError");
        return "admin/system-settings";
    }

    @PostMapping("/settings/display")
    @Transactional
    String saveDisplaySettings(@RequestParam Map<String, String> values, Authentication auth) {
        if (!validDisplayValues(values)) return "redirect:/admin/settings/display?validationError";
        saveSettingValues(values, auth);
        return "redirect:/admin/settings/display?saved";
    }

    @PostMapping("/settings/display/reset")
    @Transactional
    String resetDisplaySettings(Authentication auth) {
        Map<String, String> defaults = Map.ofEntries(Map.entry("ui_scale","100"),Map.entry("font_size","16"),Map.entry("admin_header_color", "#EBEBEB"), Map.entry("admin_maintenance_header_color", "#f8d7da"), Map.entry("admin_body_color", "#e1fad7"), Map.entry("admin_font_family", "system-ui"), Map.entry("user_header_color", "#EBEBEB"), Map.entry("user_maintenance_header_color", "#f8d7da"), Map.entry("user_body_color", "#FAEBD7"), Map.entry("user_font_family", "system-ui"));
        defaults.forEach((key, value) -> jdbc.update("UPDATE system_setting SET value=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE key=?", value, auth.getName(), key));
        audit.record(auth.getName(), "POST", "/admin/settings/display/reset", "DISPLAY_SETTINGS_RESET", null, "表示設定を初期値へ復元");
        return "redirect:/admin/settings/display?reset";
    }

    @PostMapping("/settings/system")
    @Transactional
    String saveSystemSettings(@RequestParam Map<String, String> values, Authentication auth) {
        saveSettingValues(values, auth);
        return "redirect:/admin/settings/system?saved";
    }

    @PostMapping("/settings/system/{category}")
    @Transactional
    String saveSystemSettingsCard(@PathVariable String category,@RequestParam Map<String,String> values,Authentication auth){
        if(!Set.of("display","notification","log","backup","security","datetime").contains(category))return "redirect:/admin/settings/system?error=category";
        saveSystemCategoryValues(category,values,auth);
        return "redirect:/admin/settings/system?saved="+category;
    }

    @PostMapping("/settings/system/notification/test")
    String testLineNotification(@RequestParam Map<String,String> values,@RequestParam(defaultValue="false") boolean saveBeforeTest,Authentication auth,HttpSession session){
        if(saveBeforeTest)saveSystemCategoryValues("notification",values,auth);
        String url=inputOrStored(values,"line_url"),token=inputOrStored(values,"line_token"),destination=inputOrStored(values,"line_destination_id");
        try{sendLineTest(url,token,destination);session.setAttribute("notificationTestSuccess","LINEテスト通知を送信しました。");audit.record(auth.getName(),"POST","/admin/settings/system/notification/test","LINE_TEST_NOTIFICATION",null,"LINEテスト通知送信");}
        catch(Exception ex){session.setAttribute("notificationTestError","LINEテスト通知に失敗しました："+safeLineError(ex));}
        return "redirect:/admin/settings/system";
    }

    @PostMapping("/settings")
    @Transactional
    String saveSettings(@RequestParam Map<String, String> values, Authentication auth) {
        values.forEach((k, v) -> {
            if (k.startsWith("setting_") && !(k.equals("setting_line_token") && v.isBlank()))
                jdbc.update("UPDATE system_setting SET value=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE key=?", v, auth.getName(), k.substring(8));
        });
        audit.record(auth.getName(), "POST", "/admin/settings", "SETTINGS_UPDATE", null, "システム設定更新");
        return "redirect:/admin/settings?saved";
    }

    @GetMapping("/maintenance")
    String maintenance(@RequestParam(defaultValue = "1") int page, Model model) {
        int size = 20;
        try {
            size = Math.max(1, Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='page_size'", String.class)));
        } catch (Exception ignored) {
        }
        page = Math.max(1, page);
        Integer total = jdbc.queryForObject("SELECT count(*) FROM version_info", Integer.class);
        model.addAttribute("state", jdbc.queryForMap("SELECT * FROM system_state WHERE id=1"));
        model.addAttribute("versions", jdbc.queryForList("SELECT * FROM version_info ORDER BY published_at DESC LIMIT ? OFFSET ?", size, (page - 1) * size));
        model.addAttribute("page", page);
        model.addAttribute("pages", Math.max(1, (int) Math.ceil(total / (double) size)));
        return "admin/maintenance";
    }

    @GetMapping("/maintenance/state")
    String maintenanceState(Model model) {
        model.addAttribute("state", jdbc.queryForMap("SELECT * FROM system_state WHERE id=1"));
        return "admin/maintenance-state";
    }

    @GetMapping("/versions")
    String versions(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") int page, Model model) {
        int size = 20;
        try {
            size = Math.max(1, Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='page_size'", String.class)));
        } catch (Exception ignored) {
        }
        page = Math.max(1, page);
        String like = "%" + q + "%";
        Integer total = jdbc.queryForObject("SELECT count(*) FROM version_info WHERE version ILIKE ? OR content ILIKE ?", Integer.class, like, like);
        model.addAttribute("versions", jdbc.queryForList("SELECT * FROM version_info WHERE version ILIKE ? OR content ILIKE ? ORDER BY published_at DESC LIMIT ? OFFSET ?", like, like, size, (page - 1) * size));
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("pages", Math.max(1, (int) Math.ceil(total / (double) size)));
        return "admin/versions";
    }

    @GetMapping("/versions/new")
    String newVersion() {
        return "admin/version-form";
    }

    @PostMapping("/maintenance")
    String maintenanceUpdate(@RequestParam(defaultValue = "false") boolean enabled, @RequestParam(required = false) String reason, @RequestParam(required = false) String message, Authentication auth) {
        jdbc.update("UPDATE system_state SET maintenance_enabled=?,maintenance_reason=?,maintenance_message=?,maintenance_start=CASE WHEN ? THEN COALESCE(maintenance_start,CURRENT_TIMESTAMP) ELSE NULL END,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE id=1", enabled, reason, message, enabled, auth.getName());
        audit.record(auth.getName(), "POST", "/admin/maintenance", "MAINTENANCE_CHANGE", null, String.valueOf(enabled));
        return "redirect:/admin/maintenance/state?saved";
    }

    @PostMapping("/versions")
    String version(@RequestParam String version, @RequestParam String content, @RequestParam(defaultValue = "true") boolean enabled, Authentication auth) {
        jdbc.update("INSERT INTO version_info(version,published_at,content,enabled) VALUES (?,CURRENT_TIMESTAMP,?,?)", version, content, enabled);
        audit.record(auth.getName(), "POST", "/admin/versions", "VERSION_CREATE", version, content);
        return "redirect:/admin/versions";
    }

    @PostMapping("/versions/{id}/toggle")
    String toggleVersion(@PathVariable long id, Authentication auth) {
        jdbc.update("UPDATE version_info SET enabled=NOT enabled,updated_at=CURRENT_TIMESTAMP WHERE id=?", id);
        audit.record(auth.getName(), "POST", "/admin/versions/" + id, "VERSION_TOGGLE", String.valueOf(id), null);
        return "redirect:/admin/versions";
    }

    @GetMapping("/versions/{id}")
    String versionDetail(@PathVariable long id, Model model) {
        var rows = jdbc.queryForList("SELECT * FROM version_info WHERE id=?", id);
        if (rows.isEmpty()) return "redirect:/admin/maintenance";
        model.addAttribute("version", rows.get(0));
        return "admin/version-details";
    }

    @PostMapping("/versions/{id}/update")
    String updateVersion(@PathVariable long id, @RequestParam String version, @RequestParam String content, @RequestParam(defaultValue = "false") boolean enabled, Authentication auth) {
        jdbc.update("UPDATE version_info SET version=?,content=?,enabled=?,updated_at=CURRENT_TIMESTAMP WHERE id=?", version, content, enabled, id);
        audit.record(auth.getName(), "POST", "/admin/versions/" + id + "/update", "VERSION_UPDATE", String.valueOf(id), version);
        return "redirect:/admin/versions/" + id + "?updated";
    }

    @PostMapping("/versions/{id}/delete")
    String deleteVersion(@PathVariable long id, Authentication auth) {
        jdbc.update("DELETE FROM version_info WHERE id=?", id);
        audit.record(auth.getName(), "POST", "/admin/versions/" + id + "/delete", "VERSION_DELETE", String.valueOf(id), null);
        return "redirect:/admin/versions";
    }

    private void saveSettingValues(Map<String, String> values, Authentication auth) {
        values.forEach((k, v) -> {
            if (k.startsWith("setting_") && !(k.equals("setting_line_token") && v.isBlank()))
                jdbc.update("UPDATE system_setting SET value=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE key=?", v, auth.getName(), k.substring(8));
        });
        audit.record(auth.getName(), "POST", "/admin/settings", "SETTINGS_UPDATE", null, "設定更新");
    }

    private String systemCategoryId(String key){return key.startsWith("line_")?"notification":key.contains("debug")||key.equals("log_page_size")?"log":key.startsWith("backup_")?"backup":key.contains("login_")||key.contains("password_")||key.contains("session_")||key.contains("lock_")?"security":key.contains("time")||key.contains("date")?"datetime":"display";}

    private void saveSystemCategoryValues(String category,Map<String,String> values,Authentication auth){
        values.forEach((name,value)->{if(!name.startsWith("setting_"))return;String key=name.substring(8);if(!category.equals(systemCategoryId(key)))return;if(key.equals("line_token")&&value.isBlank())return;jdbc.update("UPDATE system_setting SET value=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE key=?",value.strip(),auth.getName(),key);});
        audit.record(auth.getName(),"POST","/admin/settings/system/"+category,"SETTINGS_UPDATE",category,"システム設定カード更新");
    }

    private String inputOrStored(Map<String,String> values,String key){String input=values.get("setting_"+key);if(input!=null&&(!key.equals("line_token")||!input.isBlank()))return input.strip();String stored=jdbc.queryForObject("SELECT value FROM system_setting WHERE key=?",String.class,key);return stored==null?"":stored.strip();}

    private void sendLineTest(String url,String token,String destination) throws Exception {
        if(url.isBlank()||token.isBlank()||destination.isBlank())throw new IllegalArgumentException("LINE API URL、トークン、送信先IDを入力してください。");URI uri=URI.create(url);if(!Set.of("http","https").contains(uri.getScheme()))throw new IllegalArgumentException("LINE API URLが正しくありません。");String body="{\"to\":\""+json(destination)+"\",\"messages\":[{\"type\":\"text\",\"text\":\"家計簿アプリのLINE通知テストです。\"}]}";HttpRequest request=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).header("Authorization","Bearer "+token).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();HttpResponse<String> response=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build().send(request,HttpResponse.BodyHandlers.ofString());if(response.statusCode()<200||response.statusCode()>=300)throw new IllegalStateException("LINE APIがHTTP "+response.statusCode()+"を返しました。");
    }

    private String json(String value){return value.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n");}
    private String safeLineError(Exception ex){String message=ex.getMessage();if(message==null||message.isBlank())return "通信エラー";return message.length()>180?message.substring(0,180):message;}

    private boolean validDisplayValues(Map<String, String> values) {
        Set<String> fonts = Set.of("system-ui", "sans-serif", "serif", "monospace");
        for (var entry : values.entrySet()) {
            if (!entry.getKey().startsWith("setting_")) continue;
            String key = entry.getKey().substring(8), value = entry.getValue().trim();
            if (key.endsWith("_color") && !value.matches("#[0-9A-Fa-f]{6}")) return false;
            if (key.equals("font_size")) {
                try {
                    int size = Integer.parseInt(value);
                    if (size < 10 || size > 32) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            if (key.equals("ui_scale")) {
                try { int scale=Integer.parseInt(value); if(scale<50||scale>200)return false; }
                catch(NumberFormatException e){ return false; }
            }
            if (key.endsWith("_font_family") && !fonts.contains(value)
                    && !Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM system_setting WHERE key LIKE 'uploaded_font.%' AND value=?)", Boolean.class, value))) return false;
        }
        return true;
    }

    private boolean isLastAdmin(String id) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT permission_flag=2 AND approve_flag=1 AND delete_flag=0 AND (SELECT count(*) FROM \"user\" WHERE permission_flag=2 AND approve_flag=1 AND delete_flag=0)<=1 FROM \"user\" WHERE user_id=?", Boolean.class, id));
    }

    private String postgresDateTimeFormat() {
        try {
            String format=jdbc.queryForObject("SELECT value FROM system_setting WHERE key='datetime_format'",String.class);
            return format.replace("yyyy","YYYY").replace("dd","DD").replace("HH","HH24").replace("mm","MI").replace("ss","SS");
        } catch(Exception ignored) { return "YYYY/MM/DD HH24:MI:SS"; }
    }
}

