package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminAuditService {
    private final JdbcTemplate jdbc;
    public AdminAuditService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public void record(String user,String method,String path,String action,String target,String detail){jdbc.update("INSERT INTO operation_log(user_id,method,path,status,action,target_id,detail) VALUES (?,?,?,200,?,?,?)",user,method,path,action,target,detail);}
}
