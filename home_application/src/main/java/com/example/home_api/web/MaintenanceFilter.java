package com.example.home_api.web;

import jakarta.servlet.*;import jakarta.servlet.http.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.security.core.Authentication;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import java.io.IOException;

@Component
public class MaintenanceFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;public MaintenanceFilter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        String path=req.getRequestURI();Authentication a=SecurityContextHolder.getContext().getAuthentication();boolean user=a!=null&&a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_USER"));
        if(user&&!path.equals("/system-maintenance")&&!path.equals("/logout")&&!path.startsWith("/css/")&&Boolean.TRUE.equals(jdbc.queryForObject("SELECT maintenance_enabled FROM system_state WHERE id=1",Boolean.class))){res.sendRedirect("/system-maintenance");return;}chain.doFilter(req,res);
    }
}
