package com.example.home_api.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;

@Component
public class OperationLogFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    public OperationLogFilter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        try{chain.doFilter(req,res);}finally{
            String path=req.getRequestURI();
            if(!path.startsWith("/css/")&&!path.startsWith("/js/")&&!path.startsWith("/fonts/")&&!path.equals("/theme.css")&&!path.equals("/favicon.ico")){
                Authentication a=SecurityContextHolder.getContext().getAuthentication();
                String user=a!=null&&a.isAuthenticated()?a.getName():null;
                if(user!=null)try{Boolean audited=jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM operation_log WHERE user_id IS NOT DISTINCT FROM ? AND path=? AND method=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '2 seconds')",Boolean.class,user,path,req.getMethod());if(!Boolean.TRUE.equals(audited))jdbc.update("INSERT INTO operation_log(user_id,method,path,status,action,detail) VALUES (?,?,?,?,?,?)",user,req.getMethod(),path,res.getStatus(),OperationLogLabels.action(null,req.getMethod(),path),message(req));}catch(Exception ignored){}
            }
        }
    }
    private String message(HttpServletRequest req){StringBuilder b=new StringBuilder(req.getMethod()).append(' ').append(req.getRequestURI());List<String> values=new ArrayList<>();req.getParameterMap().forEach((key,v)->{String k=key.toLowerCase(Locale.ROOT);if(k.contains("password")||k.contains("token")||k.contains("secret")||k.equals("_csrf"))return;values.add(key+"="+String.join(",",v));});if(!values.isEmpty())b.append(" / パラメータ: ").append(String.join(", ",values));return b.toString();}
}
