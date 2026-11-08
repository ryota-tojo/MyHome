package com.example.home_api.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class GroupAccessRequiredFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    public GroupAccessRequiredFilter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        String p=req.getRequestURI();Authentication a=SecurityContextHolder.getContext().getAuthentication();
        boolean finance=p.startsWith("/shopping")||p.startsWith("/budgets")||p.startsWith("/communication")||p.startsWith("/group/");
        boolean user=a!=null&&a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_USER"));
        if(user&&!Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM \"user\" WHERE user_id=? AND approve_flag=1 AND delete_flag=0)",Boolean.class,a.getName()))){
            req.getSession().invalidate();SecurityContextHolder.clearContext();res.sendRedirect("/login?approvalRevoked");return;
        }
        if(finance&&user){
            Object group=req.getSession().getAttribute("activeGroupId");
            boolean authenticated=Boolean.TRUE.equals(req.getSession().getAttribute("groupAuthenticated"));
            boolean stillMember=authenticated&&group!=null&&Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM group_member WHERE user_id=? AND group_id=? AND approve_flag=1)",Boolean.class,a.getName(),group.toString()));
            if(!stillMember){req.getSession().removeAttribute("activeGroupId");req.getSession().removeAttribute("activeGroupName");req.getSession().removeAttribute("groupAuthenticated");req.getSession().removeAttribute("activeGroupLeader");res.sendRedirect("/groups/select");return;}
        }
        chain.doFilter(req,res);
    }
}
