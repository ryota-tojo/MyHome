package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class GeneralPortalController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public GeneralPortalController(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @GetMapping("/groups/join")
    String join(@RequestParam(defaultValue = "") String q, Model model) {
        String like = "%" + q + "%";
        model.addAttribute("q", q);
        model.addAttribute("groups", q.isBlank() ? List.of() : jdbc.queryForList("SELECT group_id,group_name FROM \"group\" WHERE status='ACTIVE' AND group_id ILIKE ? ORDER BY group_id LIMIT 50", like));
        return "groups/join";
    }

    @PostMapping("/groups/join")
    @Transactional
    String requestJoin(@RequestParam String groupId, @RequestParam String password, Authentication auth, Model model) {
        var rows = jdbc.queryForList("SELECT group_password,group_name FROM \"group\" WHERE group_id=? AND status='ACTIVE'", groupId);
        if (rows.isEmpty()) return "redirect:/groups/join?error=notFound";
        String stored = String.valueOf(rows.get(0).get("group_password"));
        if (!(stored.startsWith("$2") ? encoder.matches(password, stored) : stored.equals(password)))
            return "redirect:/groups/join?q=" + groupId + "&error=password";
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,create_date,update_date) VALUES (?,?,0,0,?,?) ON CONFLICT(group_id,user_id) DO UPDATE SET approve_flag=CASE WHEN group_member.approve_flag=1 THEN 1 ELSE 0 END,removed_at=NULL,update_date=CURRENT_TIMESTAMP", groupId, auth.getName(), now, now);
        return "redirect:/?requested";
    }

    @GetMapping("/group/members")
    String members(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String memberStatuses, @RequestParam(defaultValue = "") String memberRoles, @RequestParam(defaultValue = "") String memberApprovals, @RequestParam(defaultValue = "") String memberCreated, @RequestParam(defaultValue = "display_no") String sort, @RequestParam(defaultValue = "asc") String direction, @RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        String group = leaderGroup(session);
        int size = 20;
        try {
            size = Math.max(1, Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='page_size'", String.class)));
        } catch (Exception ignored) {
        }
        page = Math.max(1, page);
        Map<String, String> sorts = Map.of("display_no", "gm.member_no", "user_id", "u.user_id", "user_name", "u.user_name", "status", "u.delete_flag", "leader_flag", "gm.leader_flag", "approve_flag", "gm.approve_flag", "create_date", "gm.create_date");
        if (!sorts.containsKey(sort)) sort = "display_no";
        direction = direction.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        String like = "%" + q + "%", createdLike = "%" + memberCreated + "%";
        String where = " WHERE gm.group_id=? AND (u.user_id ILIKE ? OR u.user_name ILIKE ?) AND (?='' OR CASE WHEN u.delete_flag=1 THEN 'INACTIVE' ELSE 'ACTIVE' END=ANY(string_to_array(?,','))) AND (?='' OR gm.leader_flag::text=ANY(string_to_array(?,','))) AND (?='' OR gm.approve_flag::text=ANY(string_to_array(?,','))) AND (?='' OR to_char(gm.create_date,'YYYY-MM-DD HH24:MI:SS') ILIKE ?)";
        Object[] args = {group, like, like, memberStatuses, memberStatuses, memberRoles, memberRoles, memberApprovals, memberApprovals, memberCreated, createdLike};
        Integer total = jdbc.queryForObject("SELECT count(*) FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id" + where, Integer.class, args);
        List<Object> paged = new ArrayList<>(Arrays.asList(args));
        paged.add(size);
        paged.add((page - 1) * size);
        model.addAttribute("members", jdbc.queryForList("SELECT gm.*,u.user_name,u.delete_flag FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id" + where + " ORDER BY " + sorts.get(sort) + " " + direction + " NULLS LAST LIMIT ? OFFSET ?", paged.toArray()));
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
        model.addAttribute("manualOrder", sort.equals("display_no")&&direction.equals("ASC")&&q.isBlank()&&memberStatuses.isBlank()&&memberRoles.isBlank()&&memberApprovals.isBlank()&&memberCreated.isBlank());
        return "group/members";
    }

    @PostMapping("/group/members/order") @ResponseBody @Transactional
    Map<String,Object> memberOrder(@RequestParam List<String> ids,@RequestParam(defaultValue="1") int page,HttpSession session){
        String group=leaderGroup(session);int size=20;
        try{size=Math.max(1,Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='page_size'",String.class)));}catch(Exception ignored){}
        int no=(Math.max(1,page)-1)*size+1;
        for(String user:ids)jdbc.update("UPDATE group_member SET member_no=?,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=?",no++,group,user);
        return Map.of("ok",true);
    }

    @GetMapping("/group/members/new")
    String newMember(HttpSession session) {
        leaderGroup(session);
        return "group/member-new";
    }

    @PostMapping("/group/members/new")
    @Transactional
    String createMember(@RequestParam String userId, @RequestParam String userName, @RequestParam String password, @RequestParam String confirmation, @RequestParam(defaultValue = "0") int leader, HttpSession session, Model model) {
        String group = leaderGroup(session);
        userId = userId.strip();
        userName = userName.strip();
        if (!userId.matches("[A-Za-z0-9_-]{4,64}") || userName.isEmpty() || userName.length() > 100 || password.length() < 8 || !password.equals(confirmation)) {
            model.addAttribute("error", "入力内容または確認用パスワードを確認してください。");
            return "group/member-new";
        }
        Integer exists = jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=?", Integer.class, userId);
        if (exists != null && exists > 0) {
            model.addAttribute("error", "このユーザーIDはすでに使用されています。");
            return "group/member-new";
        }
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("INSERT INTO \"user\"(user_id,user_password,user_name,permission_flag,approve_flag,delete_flag,password_change_required,approve_date,create_date,update_date) VALUES (?,?,?,1,1,0,0,?,?,?)", userId, encoder.encode(password), userName, now, now, now);
        jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,approve_date,create_date,update_date) VALUES (?,?,1,?,?,?,?)", group, userId, leader == 1 ? 1 : 0, now, now, now);
        return "redirect:/group/members?created";
    }

    @GetMapping("/group/members/{user}")
    String memberDetail(@PathVariable String user, @RequestParam(defaultValue = "1") int logPage, HttpSession session, Model model) {
        String group = leaderGroup(session);
        var rows = jdbc.queryForList("SELECT u.user_id,u.user_name,u.last_login_at,u.login_failure_count,u.locked_until,u.password_changed_at,u.create_date,u.update_date,u.delete_flag,gm.leader_flag,gm.approve_flag,gm.approve_date,gm.create_date AS member_create_date FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id WHERE gm.group_id=? AND gm.user_id=?", group, user);
        if (rows.isEmpty()) return "redirect:/group/members";
        model.addAttribute("member", rows.get(0));
        int size = 50;
        try {
            size = Math.max(1, Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='log_page_size'", String.class)));
        } catch (Exception ignored) {
        }
        logPage = Math.max(1, logPage);
        Integer total = jdbc.queryForObject("SELECT count(*) FROM operation_log WHERE user_id=? OR target_id=?", Integer.class, user, user);
        model.addAttribute("logs", OperationLogLabels.enrich(jdbc.queryForList("SELECT * FROM operation_log WHERE user_id=? OR target_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?", user, user, size, (logPage - 1) * size)));
        model.addAttribute("logPage", logPage);
        model.addAttribute("logPages", Math.max(1, (int) Math.ceil(total / (double) size)));
        return "group/member-detail";
    }

    @PostMapping("/group/members/{user}/approve")
    String approve(@PathVariable String user, HttpSession session) {
        String group = leaderGroup(session);
        jdbc.update("UPDATE group_member SET approve_flag=1,approve_date=CURRENT_TIMESTAMP,removed_at=NULL,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=?", group, user);
        return "redirect:/group/members";
    }

    @PostMapping("/group/members/{user}/remove")
    String remove(@PathVariable String user, HttpSession session) {
        String group = leaderGroup(session);
        Integer leader = jdbc.queryForObject("SELECT leader_flag FROM group_member WHERE group_id=? AND user_id=?", Integer.class, group, user);
        if (leader != null && leader == 1 && jdbc.queryForObject("SELECT count(*) FROM group_member WHERE group_id=? AND leader_flag=1 AND approve_flag=1", Integer.class, group) <= 1)
            return "redirect:/group/members?error=lastLeader";
        jdbc.update("UPDATE group_member SET approve_flag=0,leader_flag=0,removed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=?", group, user);
        jdbc.update("UPDATE purchase_pattern SET disabled_flag=1,auto_disabled=1,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND ?=ANY(string_to_array(member_ids,','))", group, user);
        return "redirect:/group/members";
    }

    @PostMapping("/group/members/{user}/leader")
    String leader(@PathVariable String user, HttpSession session) {
        String group = leaderGroup(session);
        Integer current = jdbc.queryForObject("SELECT leader_flag FROM group_member WHERE group_id=? AND user_id=?", Integer.class, group, user);
        if (current != null && current == 1 && jdbc.queryForObject("SELECT count(*) FROM group_member WHERE group_id=? AND leader_flag=1 AND approve_flag=1", Integer.class, group) <= 1)
            return "redirect:/group/members?error=lastLeader";
        jdbc.update("UPDATE group_member SET leader_flag=CASE WHEN leader_flag=1 THEN 0 ELSE 1 END,update_date=CURRENT_TIMESTAMP WHERE group_id=? AND user_id=? AND approve_flag=1", group, user);
        return "redirect:/group/members";
    }

    @GetMapping("/group/settings")
    String settings(HttpSession session, Model model) {
        String group = leaderGroup(session);
        model.addAttribute("group", jdbc.queryForMap("SELECT * FROM \"group\" WHERE group_id=?", group));
        model.addAttribute("displayYear", jdbc.query("SELECT value FROM group_setting WHERE group_id=? AND key='display_year'", rs -> rs.next() ? rs.getInt(1) : java.time.LocalDate.now().getYear(), group));
        model.addAttribute("homeImage", groupSetting(group, "home_image_file"));
        model.addAttribute("lineEnabled", Boolean.parseBoolean(groupSetting(group, "line_enabled")));
        model.addAttribute("lineUrl", groupSetting(group, "line_url"));
        model.addAttribute("lineToken", groupSetting(group, "line_token"));
        model.addAttribute("lineDestinationId", groupSetting(group, "line_destination_id"));
        Object success = session.getAttribute("groupLineTestSuccess");
        Object error = session.getAttribute("groupLineTestError");
        if (success != null) model.addAttribute("lineTestSuccess", success);
        if (error != null) model.addAttribute("lineTestError", error);
        session.removeAttribute("groupLineTestSuccess");
        session.removeAttribute("groupLineTestError");
        return "group/settings";
    }

    @PostMapping("/group/settings")
    String settingsSave(@RequestParam int displayYear, HttpSession session) {
        String group = leaderGroup(session);
        jdbc.update("INSERT INTO group_setting(group_id,key,value,create_date,update_date) VALUES (?,'display_year',?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(group_id,key) DO UPDATE SET value=EXCLUDED.value,update_date=CURRENT_TIMESTAMP", group, String.valueOf(displayYear));
        return "redirect:/group/settings?saved";
    }

    @PostMapping("/group/settings/basic")
    @Transactional
    String settingsBasic(@RequestParam String groupId, @RequestParam String groupName, HttpSession session) {
        String current = leaderGroup(session);
        groupId = groupId.strip();
        groupName = groupName.strip();
        if (!groupId.matches("[A-Za-z0-9_-]{4,64}") || groupName.isEmpty() || groupName.length() > 100)
            return "redirect:/group/settings?error=input";
        Integer duplicate = jdbc.queryForObject("SELECT count(*) FROM \"group\" WHERE group_id=? AND group_id<>?", Integer.class, groupId, current);
        if (duplicate != null && duplicate > 0) return "redirect:/group/settings?error=duplicateId";
        jdbc.update("UPDATE \"group\" SET group_id=?,group_name=?,update_date=CURRENT_TIMESTAMP WHERE group_id=?", groupId, groupName, current);
        session.setAttribute("activeGroupId", groupId);
        session.setAttribute("activeGroupName", groupName);
        return "redirect:/group/settings?updated";
    }

    @GetMapping("/group/feature/categories")
    String categories() {
        return "redirect:/group/masters/categories";
    }

    @GetMapping("/group/feature/payments")
    String payments() {
        return "redirect:/group/masters/payments";
    }

    @GetMapping("/group/feature/labels")
    String labels() {
        return "redirect:/group/masters/labels";
    }

    @GetMapping("/group/feature/input-patterns")
    String inputPatterns() {
        return "redirect:/group/patterns/input-patterns";
    }

    @GetMapping("/group/feature/search-patterns")
    String searchPatterns() {
        return "redirect:/group/patterns/search-patterns";
    }

    @GetMapping("/group/feature/templates")
    String templates() {
        return "redirect:/group/patterns/templates";
    }

    @GetMapping("/group/feature/{feature}")
    String feature(@PathVariable String feature, HttpSession session, Model model) {
        if (session.getAttribute("activeGroupId") == null) return "redirect:/groups/select";
        Map<String, String> names = Map.ofEntries(Map.entry("categories", "カテゴリー管理"), Map.entry("payments", "支払い方法管理"), Map.entry("labels", "ラベル管理"), Map.entry("input-patterns", "購入データ入力パターン管理"), Map.entry("search-patterns", "購入データ検索パターン管理"), Map.entry("templates", "購入データ入力テンプレート管理"), Map.entry("kanban", "カンバン"), Map.entry("budget-year", "予算一覧（年間）"), Map.entry("template-entry", "購入データテンプレート入力"), Map.entry("data", "データ管理"), Map.entry("monthly", "月次処理"), Map.entry("budget-analysis", "予実対比"), Map.entry("year-analysis", "前年対比"));
        model.addAttribute("featureName", names.getOrDefault(feature, feature));
        return "group/feature";
    }

    private String leaderGroup(HttpSession session) {
        Object group = session.getAttribute("activeGroupId");
        if (group == null || !Boolean.TRUE.equals(session.getAttribute("activeGroupLeader")))
            throw new IllegalStateException("リーダー権限が必要です");
        return group.toString();
    }

    private String groupSetting(String group, String key) {
        return jdbc.query("SELECT value FROM group_setting WHERE group_id=? AND key=?", rs -> rs.next() ? rs.getString(1) : "", group, key);
    }
}
