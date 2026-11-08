package com.example.home_api.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JdbcTemplate jdbc;
    public LoginSuccessHandler(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public void onAuthenticationSuccess(HttpServletRequest req,HttpServletResponse res,Authentication auth) throws IOException, ServletException {
        jdbc.update("UPDATE \"user\" SET last_login_at=CURRENT_TIMESTAMP,login_failure_count=0,locked_until=NULL WHERE user_id=?",auth.getName());
        Integer required=jdbc.queryForObject("SELECT password_change_required FROM \"user\" WHERE user_id=?",Integer.class,auth.getName());
        boolean admin=auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        if(admin&&required!=null&&required==1){res.sendRedirect("/account/change-password");return;}
        if(admin){res.sendRedirect("/admin");return;}
        if(Boolean.TRUE.equals(jdbc.queryForObject("SELECT maintenance_enabled FROM system_state WHERE id=1",Boolean.class))){req.getSession().invalidate();SecurityContextHolder.clearContext();res.sendRedirect("/login?maintenance");return;}
        Integer groups=jdbc.queryForObject("SELECT count(*) FROM group_member WHERE user_id=? AND approve_flag=1",Integer.class,auth.getName());
        res.sendRedirect(groups==null||groups==0?"/":"/groups/select");
    }
}
