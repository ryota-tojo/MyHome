package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Year;
import java.util.stream.IntStream;

@ControllerAdvice
public class UiModelAdvice {
    private final JdbcTemplate jdbc;
    public UiModelAdvice(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @ModelAttribute("selectSearchThreshold")
    int selectSearchThreshold(){
        try{return Math.max(1,Math.min(100,Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='select_search_threshold'",String.class))));}
        catch(Exception e){return 8;}
    }
    @ModelAttribute("flashMessageSeconds")
    int flashMessageSeconds(){try{return Math.max(0,Math.min(60,Integer.parseInt(jdbc.queryForObject("SELECT value FROM system_setting WHERE key='flash_message_seconds'",String.class))));}catch(Exception e){return 5;}}
    @ModelAttribute("yearOptions") int[] yearOptions(){return IntStream.rangeClosed(2023,Year.now().getValue()+2).toArray();}
    @ModelAttribute("monthOptions") int[] monthOptions(){return IntStream.rangeClosed(1,12).toArray();}

    @ModelAttribute("latestVersion")
    String latestVersion(){
        try{return jdbc.queryForObject("SELECT version FROM version_info ORDER BY published_at DESC,id DESC LIMIT 1",String.class);}
        catch(Exception e){return "-";}
    }
    @ModelAttribute("activeGroupName") Object activeGroupName(HttpServletRequest request){return request.getSession(false)==null?null:request.getSession(false).getAttribute("activeGroupName");}
    @ModelAttribute("activeGroupLeader") boolean activeGroupLeader(HttpServletRequest request){return request.getSession(false)!=null&&Boolean.TRUE.equals(request.getSession(false).getAttribute("activeGroupLeader"));}
}
