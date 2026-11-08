package com.example.home_api.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
public class AutoLoginController {
    private final AutoLoginService service;private final JdbcTemplate jdbc;
    public AutoLoginController(AutoLoginService service,JdbcTemplate jdbc){this.service=service;this.jdbc=jdbc;}

    @PostMapping("/account/auto-login-url")
    String create(@RequestParam String password,Authentication auth,HttpSession session,HttpServletRequest request,HttpServletResponse response,Model model){
        response.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        response.setHeader("Pragma","no-cache");
        try{String group=session.getAttribute("activeGroupId")==null?null:String.valueOf(session.getAttribute("activeGroupId"));String base=request.getScheme()+"://"+request.getServerName()+defaultPort(request)+request.getContextPath();model.addAttribute("autoLoginUrl",service.create(auth.getName(),password,group,base).url());}catch(Exception e){model.addAttribute("autoLoginError",e.getMessage());}
        profile(auth,model);return "account-profile";
    }

    @GetMapping(value="/login",params={"userid","pw"})
    String login(@RequestParam String userid,@RequestParam String pw,@RequestParam(required=false)String group,HttpServletRequest request,HttpServletResponse response,HttpSession session,Model model){
        try{
            response.setHeader("Referrer-Policy","no-referrer");response.setHeader("Cache-Control","no-store");
            AutoLoginService.VerifiedLogin verified=service.verify(userid,pw,group);boolean admin=verified.user().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
            if(!admin&&Boolean.TRUE.equals(jdbc.queryForObject("SELECT maintenance_enabled FROM system_state WHERE id=1",Boolean.class)))return "redirect:/login?maintenance";
            request.changeSessionId();SecurityContext context=SecurityContextHolder.createEmptyContext();UsernamePasswordAuthenticationToken authentication=UsernamePasswordAuthenticationToken.authenticated(verified.user(),null,verified.user().getAuthorities());context.setAuthentication(authentication);SecurityContextHolder.setContext(context);session.setAttribute("SPRING_SECURITY_CONTEXT",context);
            jdbc.update("UPDATE \"user\" SET last_login_at=CURRENT_TIMESTAMP,login_failure_count=0,locked_until=NULL WHERE user_id=?",verified.user().getUsername());
            if(verified.groupId()!=null){var selected=service.group(verified.user().getUsername(),verified.groupId());session.setAttribute("activeGroupId",verified.groupId());session.setAttribute("activeGroupName",selected.get("group_name"));session.setAttribute("activeGroupLeader",Objects.equals(selected.get("leader_flag"),1));session.setAttribute("groupAuthenticated",true);}else{session.removeAttribute("activeGroupId");session.removeAttribute("activeGroupName");session.removeAttribute("activeGroupLeader");session.removeAttribute("groupAuthenticated");}
            Integer required=jdbc.queryForObject("SELECT password_change_required FROM \"user\" WHERE user_id=?",Integer.class,verified.user().getUsername());if(admin&&required!=null&&required==1)return "redirect:/account/change-password";if(admin)return "redirect:/admin";return "redirect:/";
        }catch(Exception e){model.addAttribute("autoLoginError","自動ログインURLが無効か、現在利用できません。");return "login";}
    }

    private void profile(Authentication auth,Model model){model.addAttribute("user",jdbc.queryForMap("SELECT user_id,user_name FROM \"user\" WHERE user_id=?",auth.getName()));model.addAttribute("isAdmin",auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")));}
    private String defaultPort(HttpServletRequest request){int port=request.getServerPort();return ("http".equals(request.getScheme())&&port==80)||("https".equals(request.getScheme())&&port==443)?"":":"+port;}
}
