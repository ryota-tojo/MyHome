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
import java.util.*;

@Controller
@RequestMapping("/groups")
public class GroupAccessController {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    public GroupAccessController(JdbcTemplate jdbc,PasswordEncoder encoder){this.jdbc=jdbc;this.encoder=encoder;}

    @GetMapping("/prepare")
    String prepare(Authentication auth){
        var ids=jdbc.queryForList("SELECT group_id FROM group_member WHERE user_id=? AND approve_flag=1",String.class,auth.getName());
        if(ids.isEmpty())return "redirect:/";
        return "redirect:/groups/select";
    }

    @GetMapping("/select")
    String select(Authentication auth,Model model){
        model.addAttribute("groups",jdbc.queryForList("""
            SELECT g.group_id,g.group_name,gm.leader_flag FROM group_member gm
            JOIN "group" g ON g.group_id=gm.group_id WHERE gm.user_id=? AND gm.approve_flag=1 ORDER BY g.group_name
            """,auth.getName()));
        return "groups/select";
    }

    @GetMapping("/activate")
    String activate(@RequestParam String groupId,Authentication auth,HttpSession session){
        var groups=membership(auth.getName(),groupId);if(groups.isEmpty())return "redirect:/groups/select";
        session.setAttribute("activeGroupId",groupId);session.setAttribute("activeGroupName",groups.get(0).get("group_name"));session.setAttribute("groupAuthenticated",true);
        session.setAttribute("activeGroupLeader",Objects.equals(groups.get(0).get("leader_flag"),1));return "redirect:/";
    }

    @GetMapping("/auth")
    String auth(@RequestParam String groupId,@RequestParam(defaultValue="select") String returnTo,Authentication auth,Model model){
        return "redirect:/groups/activate?groupId="+groupId;
    }

    @PostMapping("/auth")
    String authenticate(@RequestParam String groupId,@RequestParam String password,@RequestParam(defaultValue="select") String returnTo,Authentication auth,HttpSession session,Model model){
        return activate(groupId,auth,session);
    }

    @GetMapping("/setup") String setup(){return "groups/setup";}

    @PostMapping("/setup") @Transactional
    String create(@RequestParam String groupName,@RequestParam String password,@RequestParam String confirmation,Authentication auth,HttpSession session,Model model){
        model.addAttribute("groupName",groupName);
        if(groupName.isBlank()){model.addAttribute("error","所属グループ名を入力してください。");return "groups/setup";}
        if(password.length()<8){model.addAttribute("error","パスワードは8文字以上で入力してください。");return "groups/setup";}
        if(!password.equals(confirmation)){model.addAttribute("error","確認用パスワードが一致しません。");return "groups/setup";}
        LocalDateTime now=LocalDateTime.now();String groupId="grp-"+UUID.randomUUID().toString().substring(0,12);
        jdbc.update("INSERT INTO \"group\"(group_id,group_password,group_name,create_date,update_date) VALUES (?,?,?,?,?)",groupId,encoder.encode(password),groupName,now,now);
        jdbc.update("INSERT INTO group_member(group_id,user_id,approve_flag,leader_flag,approve_date,create_date,update_date) VALUES (?,?,1,1,?,?,?)",groupId,auth.getName(),now,now,now);
        seedCategories(groupId,now);seedPaymentMethods(groupId,now);session.setAttribute("activeGroupId",groupId);session.setAttribute("activeGroupName",groupName);session.setAttribute("groupAuthenticated",true);session.setAttribute("activeGroupLeader",true);
        return "redirect:/";
    }

    @PostMapping("/switch") String change(HttpSession session){session.removeAttribute("activeGroupId");session.removeAttribute("activeGroupName");session.removeAttribute("groupAuthenticated");session.removeAttribute("activeGroupLeader");return "redirect:/groups/prepare";}

    @GetMapping("/switch") String changeFromMenu(HttpSession session){return change(session);}

    @PostMapping("/without") String without(HttpSession session){session.removeAttribute("activeGroupId");session.removeAttribute("activeGroupName");session.removeAttribute("groupAuthenticated");session.removeAttribute("activeGroupLeader");return "redirect:/";}

    private List<Map<String,Object>> membership(String user,String group){return jdbc.queryForList("""
        SELECT g.group_id,g.group_name,g.group_password,gm.leader_flag FROM group_member gm JOIN "group" g ON g.group_id=gm.group_id
        WHERE gm.user_id=? AND gm.group_id=? AND gm.approve_flag=1""",user,group);}
    private void seedCategories(String group,LocalDateTime now){for(int i=0;i<GroupDefaults.CATEGORIES.size();i++)jdbc.update("INSERT INTO category VALUES (?,?,?,?,0,?,?)","cat-"+UUID.randomUUID().toString().substring(0,12),group,i+1,GroupDefaults.CATEGORIES.get(i),now,now);}
    private void seedPaymentMethods(String group,LocalDateTime now){for(int i=0;i<GroupDefaults.PAYMENT_METHODS.size();i++)jdbc.update("INSERT INTO payment_method(id,group_id,payment_no,payment_name,disabled_flag,create_date,update_date) VALUES (?,?,?,?,0,?,?)","pay-"+UUID.randomUUID().toString().substring(0,12),group,i+1,GroupDefaults.PAYMENT_METHODS.get(i),now,now);}
}
