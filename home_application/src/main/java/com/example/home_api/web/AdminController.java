package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final JdbcTemplate jdbc;
    public AdminController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    String dashboard(Model model){
        model.addAttribute("userCount",jdbc.queryForObject("SELECT count(*) FROM \"user\"",Integer.class));
        model.addAttribute("activeUserCount",jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE approve_flag=1 AND delete_flag=0",Integer.class));
        model.addAttribute("disabledUserCount",jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE delete_flag=1",Integer.class));
        model.addAttribute("lockedUserCount",jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE locked_until>CURRENT_TIMESTAMP",Integer.class));
        model.addAttribute("pendingCount",jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE approve_flag=0 AND delete_flag=0",Integer.class));
        model.addAttribute("groupCount",jdbc.queryForObject("SELECT count(*) FROM \"group\"",Integer.class));
        model.addAttribute("activeGroupCount",jdbc.queryForObject("SELECT count(*) FROM \"group\" WHERE status='ACTIVE'",Integer.class));
        model.addAttribute("pendingMemberCount",jdbc.queryForObject("SELECT count(*) FROM group_member WHERE approve_flag=0 AND removed_at IS NULL",Integer.class));
        model.addAttribute("errorCount",jdbc.queryForObject("SELECT count(*) FROM error_log WHERE created_at>CURRENT_TIMESTAMP-INTERVAL '24 hours'",Integer.class));
        model.addAttribute("databaseOk",true);
        model.addAttribute("state",jdbc.queryForMap("SELECT * FROM system_state WHERE id=1"));
        model.addAttribute("version",jdbc.queryForList("SELECT * FROM version_info WHERE enabled=true ORDER BY published_at DESC LIMIT 1"));
        model.addAttribute("recentLogs",OperationLogLabels.enrich(jdbc.queryForList("SELECT * FROM operation_log ORDER BY created_at DESC LIMIT 10")));
        model.addAttribute("shoppingCount",jdbc.queryForObject("SELECT count(*) FROM shopping",Integer.class));
        return "admin/dashboard";
    }

    @GetMapping("/users")
    String users(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="")String permissions,@RequestParam(defaultValue="")String statuses,@RequestParam(defaultValue="")String group,@RequestParam(defaultValue="")String lastLogin,@RequestParam(defaultValue="")String created,@RequestParam(defaultValue="create_date")String sort,@RequestParam(defaultValue="desc")String direction,@RequestParam(defaultValue="1")int page,Model model){
        int size=pageSize("page_size",20);page=Math.max(1,page);
        Map<String,String> sorts=Map.of("user_id","u.user_id","user_name","u.user_name","permission_flag","u.permission_flag","status","u.delete_flag","group","groups","last_login_at","u.last_login_at","create_date","u.create_date");
        if(!sorts.containsKey(sort))sort="create_date";direction=direction.equalsIgnoreCase("asc")?"ASC":"DESC";
        String dateFormat=postgresDateTimeFormat(),like="%"+q+"%",glike="%"+group+"%",loginLike="%"+lastLogin+"%",createdLike="%"+created+"%";
        String where=" WHERE (LOWER(u.user_id) LIKE LOWER(?) OR LOWER(u.user_name) LIKE LOWER(?)) AND (?='' OR u.permission_flag::text=ANY(string_to_array(?,','))) AND (?='' OR CASE WHEN u.delete_flag=1 THEN 'INACTIVE' ELSE 'ACTIVE' END=ANY(string_to_array(?,','))) AND (?='' OR LOWER(g.group_id) LIKE LOWER(?) OR LOWER(g.group_name) LIKE LOWER(?)) AND (?='' OR to_char(u.last_login_at,?) ILIKE ?) AND (?='' OR to_char(u.create_date,?) ILIKE ?) ";
        Object[] args={like,like,permissions,permissions,statuses,statuses,group,glike,glike,lastLogin,dateFormat,loginLike,created,dateFormat,createdLike};
        Integer total=jdbc.queryForObject("SELECT count(DISTINCT u.user_id) FROM \"user\" u LEFT JOIN group_member gm ON gm.user_id=u.user_id LEFT JOIN \"group\" g ON g.group_id=gm.group_id"+where,Integer.class,args);
        String sql="SELECT u.user_id,u.user_name,u.permission_flag,u.approve_flag,u.delete_flag,u.locked_until,u.last_login_at,u.create_date,u.update_date,string_agg(DISTINCT g.group_name,', ') groups FROM \"user\" u LEFT JOIN group_member gm ON gm.user_id=u.user_id AND gm.approve_flag=1 LEFT JOIN \"group\" g ON g.group_id=gm.group_id"+where+" GROUP BY u.user_id ORDER BY "+sorts.get(sort)+" "+direction+" NULLS LAST LIMIT ? OFFSET ?";
        List<Object> paged=new ArrayList<>(Arrays.asList(args));paged.add(size);paged.add((page-1)*size);model.addAttribute("rows",jdbc.queryForList(sql,paged.toArray()));pagination(model,page,size,total);
        model.addAttribute("q",q);model.addAttribute("permissions",permissions);model.addAttribute("statuses",statuses);model.addAttribute("group",group);model.addAttribute("lastLogin",lastLogin);model.addAttribute("created",created);model.addAttribute("sort",sort);model.addAttribute("direction",direction.toLowerCase());
        return "admin/users";
    }

    @PostMapping("/users/{id}/approve")
    String approve(@PathVariable String id){jdbc.update("UPDATE \"user\" SET approve_flag=1,approve_date=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=? AND permission_flag<>2",id);return "redirect:/admin/users";}

    @PostMapping("/users/{id}/revoke")
    String revoke(@PathVariable String id){Boolean last=jdbc.queryForObject("SELECT permission_flag=2 AND approve_flag=1 AND delete_flag=0 AND (SELECT count(*) FROM \"user\" WHERE permission_flag=2 AND approve_flag=1 AND delete_flag=0)<=1 FROM \"user\" WHERE user_id=?",Boolean.class,id);if(Boolean.TRUE.equals(last))return "redirect:/admin/users?error=lastAdmin";jdbc.update("UPDATE \"user\" SET approve_flag=0,approve_date=NULL,update_date=CURRENT_TIMESTAMP WHERE user_id=?",id);return "redirect:/admin/users";}

    @GetMapping("/users/{id}")
    String userDetails(@PathVariable String id,@RequestParam(defaultValue="1")int logPage,Model model){
        var users=jdbc.queryForList("SELECT user_id,user_name,permission_flag,approve_flag,delete_flag,last_login_at,login_failure_count,locked_until,password_changed_at,create_date,update_date FROM \"user\" WHERE user_id=?",id);
        if(users.isEmpty())return "redirect:/admin/users";
        model.addAttribute("user",users.get(0));
        model.addAttribute("userLocked",Boolean.TRUE.equals(jdbc.queryForObject("SELECT locked_until>CURRENT_TIMESTAMP FROM \"user\" WHERE user_id=?",Boolean.class,id)));
        model.addAttribute("groups",jdbc.queryForList("""
            SELECT g.group_id,g.group_name,gm.approve_flag,gm.leader_flag,gm.create_date,
              (SELECT count(*) FROM shopping s WHERE s.group_id=g.group_id) shopping_count,
              (SELECT COALESCE(sum(CASE WHEN s.type=2 THEN s.amount ELSE 0 END),0) FROM shopping s WHERE s.group_id=g.group_id) income_total,
              (SELECT COALESCE(sum(CASE WHEN s.type=1 THEN s.amount ELSE 0 END),0) FROM shopping s WHERE s.group_id=g.group_id) expense_total,
              (SELECT count(*) FROM category c WHERE c.group_id=g.group_id AND c.disabled_flag=0) category_count,
              (SELECT count(*) FROM budgets b WHERE b.group_id=g.group_id) budget_count
            FROM group_member gm JOIN \"group\" g ON g.group_id=gm.group_id
            WHERE gm.user_id=? ORDER BY g.group_name
            """,id));
        int logSize=pageSize("log_page_size",50);logPage=Math.max(1,logPage);Integer logTotal=jdbc.queryForObject("SELECT count(*) FROM operation_log WHERE user_id=? OR target_id=?",Integer.class,id,id);
        model.addAttribute("logs",OperationLogLabels.enrich(jdbc.queryForList("SELECT * FROM operation_log WHERE user_id=? OR target_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?",id,id,logSize,(logPage-1)*logSize)));
        model.addAttribute("logPage",logPage);model.addAttribute("logPages",Math.max(1,(int)Math.ceil(logTotal/(double)logSize)));
        return "admin/user-details";
    }

    @PostMapping("/users/{id}/toggle-disabled")
    String toggle(@PathVariable String id){Boolean last=jdbc.queryForObject("SELECT permission_flag=2 AND approve_flag=1 AND delete_flag=0 AND (SELECT count(*) FROM \"user\" WHERE permission_flag=2 AND approve_flag=1 AND delete_flag=0)<=1 FROM \"user\" WHERE user_id=?",Boolean.class,id);if(Boolean.TRUE.equals(last))return "redirect:/admin/users?error=lastAdmin";jdbc.update("UPDATE \"user\" SET delete_flag=CASE WHEN delete_flag=1 THEN 0 ELSE 1 END,delete_date=CASE WHEN delete_flag=1 THEN NULL ELSE CURRENT_TIMESTAMP END,update_date=CURRENT_TIMESTAMP WHERE user_id=?",id);return "redirect:/admin/users";}

    @PostMapping("/users/{id}/status")
    String userStatus(@PathVariable String id,@RequestParam int disabled){if(Boolean.TRUE.equals(jdbc.queryForObject("SELECT permission_flag=2 FROM \"user\" WHERE user_id=?",Boolean.class,id)))return "redirect:/admin/users/"+id+"?error=adminFixed";jdbc.update("UPDATE \"user\" SET delete_flag=?,delete_date=CASE WHEN ?=1 THEN CURRENT_TIMESTAMP ELSE NULL END,update_date=CURRENT_TIMESTAMP WHERE user_id=?",disabled,disabled,id);return "redirect:/admin/users/"+id+"?updated";}

    @GetMapping("/groups")
    String groups(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="")String statuses,@RequestParam(defaultValue="")String leaders,@RequestParam(defaultValue="")String memberCount,@RequestParam(defaultValue="")String pendingCount,@RequestParam(defaultValue="")String created,@RequestParam(defaultValue="")String updated,@RequestParam(defaultValue="create_date")String sort,@RequestParam(defaultValue="desc")String direction,@RequestParam(defaultValue="1")int page,Model model){
        int size=pageSize("page_size",20);page=Math.max(1,page);Set<String> sorts=Set.of("group_id","group_name","status","leaders","member_count","pending_count","create_date","update_date");if(!sorts.contains(sort))sort="create_date";direction=direction.equalsIgnoreCase("asc")?"ASC":"DESC";
        String dateFormat=postgresDateTimeFormat(),like="%"+q+"%",leaderLike="%"+leaders+"%",memberLike="%"+memberCount+"%",pendingLike="%"+pendingCount+"%",createdLike="%"+created+"%",updatedLike="%"+updated+"%";
        String cte="WITH data AS (SELECT g.group_id,g.group_name,g.status,count(gm.user_id) FILTER(WHERE gm.approve_flag=1) member_count,count(gm.user_id) FILTER(WHERE gm.approve_flag=0 AND gm.removed_at IS NULL) pending_count,COALESCE(string_agg(u.user_name,', ') FILTER(WHERE gm.leader_flag=1 AND gm.approve_flag=1),'') leaders,g.create_date,g.update_date FROM \"group\" g LEFT JOIN group_member gm ON gm.group_id=g.group_id LEFT JOIN \"user\" u ON u.user_id=gm.user_id GROUP BY g.group_id) ";
        String where=" WHERE (LOWER(group_id) LIKE LOWER(?) OR LOWER(group_name) LIKE LOWER(?)) AND (?='' OR status=ANY(string_to_array(?,','))) AND (?='' OR leaders ILIKE ?) AND (?='' OR member_count::text ILIKE ?) AND (?='' OR pending_count::text ILIKE ?) AND (?='' OR to_char(create_date,?) ILIKE ?) AND (?='' OR to_char(update_date,?) ILIKE ?)";
        Object[] args={like,like,statuses,statuses,leaders,leaderLike,memberCount,memberLike,pendingCount,pendingLike,created,dateFormat,createdLike,updated,dateFormat,updatedLike};Integer total=jdbc.queryForObject(cte+"SELECT count(*) FROM data"+where,Integer.class,args);
        List<Object> paged=new ArrayList<>(Arrays.asList(args));paged.add(size);paged.add((page-1)*size);model.addAttribute("rows",jdbc.queryForList(cte+"SELECT * FROM data"+where+" ORDER BY "+sort+" "+direction+" NULLS LAST LIMIT ? OFFSET ?",paged.toArray()));pagination(model,page,size,total);
        model.addAttribute("q",q);model.addAttribute("statuses",statuses);model.addAttribute("leaders",leaders);model.addAttribute("memberCount",memberCount);model.addAttribute("pendingCount",pendingCount);model.addAttribute("created",created);model.addAttribute("updated",updated);model.addAttribute("sort",sort);model.addAttribute("direction",direction.toLowerCase());return "admin/groups";}

    @GetMapping("/logs")
    String logs(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="")String user,@RequestParam(defaultValue="")String screen,@RequestParam(defaultValue="")String action,@RequestParam(defaultValue="")String target,@RequestParam(defaultValue="")String result,@RequestParam(defaultValue="")String content,@RequestParam(defaultValue="")String from,@RequestParam(defaultValue="")String to,@RequestParam(defaultValue="1")int page,Model model){int size=pageSize("log_page_size",50);page=Math.max(1,page);String where=" WHERE (?='' OR CONCAT_WS(' ',ol.user_id,u.user_name) ILIKE ?) AND (?='' OR ol.path ILIKE ?) AND (?='' OR COALESCE(ol.action,ol.method) ILIKE ?) AND (?='' OR ol.target_id ILIKE ?) AND (?='' OR ol.status::text ILIKE ?) AND (?='' OR ol.detail ILIKE ?) AND (?='' OR CONCAT_WS(' ',ol.user_id,u.user_name,ol.path,ol.action,ol.method,ol.target_id,ol.status::text,ol.detail) ILIKE ?) AND (?='' OR ol.created_at>=CAST(NULLIF(?,'') AS timestamp)) AND (?='' OR ol.created_at<=CAST(NULLIF(?,'') AS timestamp))";String like="%"+q+"%";Object[]args={user,"%"+user+"%",screen,"%"+screen+"%",action,"%"+action+"%",target,"%"+target+"%",result,"%"+result+"%",content,"%"+content+"%",q,like,from,from,to,to};String base=" FROM operation_log ol LEFT JOIN \"user\" u ON u.user_id=ol.user_id";Integer total=jdbc.queryForObject("SELECT count(*)"+base+where,Integer.class,args);List<Object>paged=new ArrayList<>(Arrays.asList(args));paged.add(size);paged.add((page-1)*size);model.addAttribute("rows",OperationLogLabels.enrich(jdbc.queryForList("SELECT ol.*,u.user_name"+base+where+" ORDER BY ol.created_at DESC LIMIT ? OFFSET ?",paged.toArray())));pagination(model,page,size,total);model.addAttribute("q",q);model.addAttribute("user",user);model.addAttribute("screen",screen);model.addAttribute("action",action);model.addAttribute("target",target);model.addAttribute("result",result);model.addAttribute("content",content);model.addAttribute("from",from);model.addAttribute("to",to);return "admin/logs";}
    private int pageSize(String key,int fallback){try{return Math.max(1,Math.min(200,Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key=?",String.class,key))));}catch(Exception e){return fallback;}}
    private String postgresDateTimeFormat(){try{String f=jdbc.queryForObject("SELECT value FROM system_setting WHERE key='datetime_format'",String.class);return f.replace("yyyy","YYYY").replace("MM","MM").replace("dd","DD").replace("HH","HH24").replace("mm","MI").replace("ss","SS");}catch(Exception e){return "YYYY/MM/DD HH24:MI:SS";}}
    private void pagination(Model model,int page,int size,Integer total){int pages=Math.max(1,(int)Math.ceil((total==null?0:total)/(double)size));model.addAttribute("page",page);model.addAttribute("pages",pages);model.addAttribute("total",total==null?0:total);}
}

