package com.example.home_api.web;

import com.fasterxml.jackson.databind.*;import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.security.core.Authentication;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Controller;import org.springframework.transaction.annotation.Transactional;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.time.*;import java.time.format.DateTimeFormatter;import java.util.*;

@Controller @RequestMapping("/admin/data") public class AdminDataController {
    private static final List<String> TABLES=List.of("master_setting","choice_master","group_setting_master","user_setting_master","notice","\"group\"","\"user\"","group_member","group_setting","user_setting","category","payment_method","shopping_label","purchase_pattern","shopping","shopping_entry_list","shopping_input_list","shopping_search_list","budgets","fixed","comment","communication","system_setting","system_state","version_info","notification_history","error_log","operation_log","application_secret","auto_login_token");
    private static final List<String> RESET_TABLES=List.of("auto_login_token","application_secret","operation_log","error_log","notification_history","version_info","system_state","system_setting","communication","comment","fixed","budgets","shopping_search_list","shopping_input_list","shopping_entry_list","shopping","purchase_pattern","shopping_label","payment_method","category","user_setting","group_setting","group_member","\"user\"","\"group\"","notice");
    private final JdbcTemplate jdbc;private final ObjectMapper mapper;private final PasswordEncoder encoder;private final AdminInitializer initializer;private final AdminAuditService audit;
    public AdminDataController(JdbcTemplate jdbc,ObjectMapper mapper,PasswordEncoder encoder,AdminInitializer initializer,AdminAuditService audit){this.jdbc=jdbc;this.mapper=mapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);this.encoder=encoder;this.initializer=initializer;this.audit=audit;}
    @GetMapping String page(){return "redirect:/admin/data/backup";}
    @GetMapping("/backup")String backupPage(Model model){model.addAttribute("counts",counts());return "admin/data-backup";}
    @GetMapping("/import")String importPage(){return "admin/data-import";}
    @GetMapping("/initialize")String initializePage(Model model){model.addAttribute("counts",counts());return "admin/data-initialize";}
    @GetMapping("/export")void export(Authentication auth,HttpServletResponse response)throws IOException{Map<String,Object>backup=new LinkedHashMap<>();backup.put("format","home-application-backup-v1");backup.put("createdAt",LocalDateTime.now().toString());Map<String,Object>data=new LinkedHashMap<>();for(String t:TABLES)data.put(t.replace("\"",""),jdbc.queryForList("SELECT * FROM "+t));backup.put("data",data);byte[]body=mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backup);response.setContentType("application/json");response.setHeader("Content-Disposition","attachment; filename=home-backup-"+LocalDate.now()+".json");response.getOutputStream().write(body);audit.record(auth.getName(),"GET","/admin/data/export","BACKUP_EXPORT",null,"全データエクスポート");}
    @PostMapping("/import")@Transactional String importData(@RequestParam MultipartFile backup,@RequestParam String confirmation,Authentication auth)throws IOException{if(!"IMPORT".equals(confirmation)||backup.isEmpty()||backup.getSize()>100*1024*1024)return "redirect:/admin/data/import?error=validation";JsonNode root=mapper.readTree(backup.getBytes());if(!"home-application-backup-v1".equals(root.path("format").asText())||!root.path("data").isObject())return "redirect:/admin/data/import?error=format";List<String>reverse=new ArrayList<>(TABLES);Collections.reverse(reverse);for(String t:reverse)jdbc.update("DELETE FROM "+t);for(String t:TABLES){String key=t.replace("\"","");JsonNode rows=root.path("data").path(key);if(rows.isArray()&&rows.size()>0)jdbc.update("INSERT INTO "+t+" SELECT * FROM json_populate_recordset(NULL::"+t+", CAST(? AS json))",rows.toString());}resetSequences();audit.record(auth.getName(),"POST","/admin/data/import","BACKUP_IMPORT",null,"バックアップ全置換インポート");return "redirect:/admin/data/import?imported";}
    @PostMapping("/initialize") @Transactional
    String initialize(@RequestParam String confirmation,@RequestParam String password,Authentication auth,HttpServletRequest request){
        List<String> hashes=jdbc.query("SELECT user_password FROM \"user\" WHERE user_id=?",(rs,rowNum)->rs.getString(1),auth.getName());
        if(!"INITIALIZE".equals(confirmation)||hashes.isEmpty()||!encoder.matches(password,hashes.get(0)))return "redirect:/admin/data/initialize?error=authentication";
        jdbc.execute("TRUNCATE TABLE "+String.join(",",RESET_TABLES)+" RESTART IDENTITY CASCADE");
        initializer.run(null);
        clearUploadedFonts();
        if(request.getSession(false)!=null)request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?initialized";
    }
    private void clearUploadedFonts(){
        Path fontDir=Path.of("data","fonts").toAbsolutePath().normalize();
        if(!Files.isDirectory(fontDir))return;
        try(var files=Files.list(fontDir)){files.filter(Files::isRegularFile).forEach(path->{try{Files.deleteIfExists(path);}catch(IOException e){throw new UncheckedIOException(e);}});}
        catch(IOException e){throw new UncheckedIOException(e);}
    }
    private void resetSequences(){for(String t:List.of("shopping","communication","operation_log","version_info","notification_history","error_log")){try{jdbc.execute("SELECT setval(pg_get_serial_sequence('"+t+"','id'),COALESCE((SELECT max(id) FROM "+t+"),1),true)");}catch(Exception ignored){}}}
    private Map<String,Integer>counts(){Map<String,Integer>counts=new LinkedHashMap<>();for(String t:TABLES)counts.put(t.replace("\"",""),jdbc.queryForObject("SELECT count(*) FROM "+t,Integer.class));return counts;}
}
