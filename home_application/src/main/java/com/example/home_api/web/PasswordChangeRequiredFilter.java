package com.example.home_api.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    public PasswordChangeRequiredFilter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException{
        Authentication auth=SecurityContextHolder.getContext().getAuthentication(); String path=req.getRequestURI();
        boolean allowed=path.startsWith("/account/change-password")||path.startsWith("/css/")||path.startsWith("/js/")||path.equals("/logout")||path.equals("/error");
        if(!allowed&&auth!=null&&auth.isAuthenticated()&&!(auth instanceof AnonymousAuthenticationToken)){
            Integer required=jdbc.queryForObject("SELECT password_change_required FROM \"user\" WHERE user_id=?",Integer.class,auth.getName());
            boolean admin=auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
            if(admin&&required!=null&&required==1){res.sendRedirect("/account/change-password");return;}
        }
        chain.doFilter(req,res);
    }
}
