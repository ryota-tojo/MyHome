package com.example.home_api.web;

import com.fasterxml.jackson.databind.*;import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.security.core.Authentication;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Controller;import org.springframework.transaction.annotation.Transactional;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.time.*;import java.time.format.DateTimeFormatter;import java.util.*;

@Controller @RequestMapping("/admin/data") public class AdminDataController {
    private static final String BACKUP_FORMAT_V1="home-application-backup-v1";
    private static final String BACKUP_FORMAT_V2="home-application-backup-v2";
    private static final String BACKUP_FORMAT_V3="home-application-backup-v3";
    private static final long MAX_HOME_IMAGE_SIZE=10L*1024*1024;
    private static final long MAX_HOME_IMAGES_SIZE=60L*1024*1024;
    private static final long MAX_FONT_SIZE=5L*1024*1024;
    private static final long MAX_FONTS_SIZE=30L*1024*1024;
    private static final long MAX_BACKUP_SIZE=150L*1024*1024;
    private static final Path GROUP_HOME_IMAGE_DIR=Path.of("data","group-home-images").toAbsolutePath().normalize();
    private static final Path FONT_DIR=Path.of("data","fonts").toAbsolutePath().normalize();
    private static final List<String> TABLES=List.of("master_setting","choice_master","group_setting_master","user_setting_master","notice","\"group\"","\"user\"","group_member","group_setting","user_setting","category","payment_method","shopping_label","purchase_pattern","kanban_lane","kanban_card","shopping","shopping_entry_list","shopping_input_list","shopping_search_list","budgets","fixed","comment","communication","system_setting","system_state","version_info","notification_history","error_log","operation_log","application_secret","auto_login_token");
    private static final List<String> RESET_TABLES=List.of("auto_login_token","application_secret","operation_log","error_log","notification_history","version_info","system_state","system_setting","communication","comment","fixed","budgets","shopping_search_list","shopping_input_list","shopping_entry_list","shopping","kanban_card","kanban_lane","purchase_pattern","shopping_label","payment_method","category","user_setting","group_setting","group_member","\"user\"","\"group\"","notice");
    private final JdbcTemplate jdbc;private final ObjectMapper mapper;private final PasswordEncoder encoder;private final AdminInitializer initializer;private final AdminAuditService audit;
    public AdminDataController(JdbcTemplate jdbc,ObjectMapper mapper,PasswordEncoder encoder,AdminInitializer initializer,AdminAuditService audit){this.jdbc=jdbc;this.mapper=mapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);this.encoder=encoder;this.initializer=initializer;this.audit=audit;}
    @GetMapping String page(){return "redirect:/admin/data/backup";}
    @GetMapping("/backup")String backupPage(Model model){model.addAttribute("counts",counts());model.addAttribute("homeImageCount",jdbc.queryForObject("SELECT count(*) FROM group_setting WHERE key='home_image_file' AND value IS NOT NULL AND value<>''",Integer.class));try{model.addAttribute("fontCount",fontFileNames().size());}catch(IOException ex){throw new UncheckedIOException(ex);}return "admin/data-backup";}
    @GetMapping("/import")String importPage(){return "admin/data-import";}
    @GetMapping("/initialize")String initializePage(Model model){model.addAttribute("counts",counts());return "admin/data-initialize";}
    @GetMapping("/export")void export(Authentication auth,HttpServletResponse response)throws IOException{Map<String,Object>backup=new LinkedHashMap<>();backup.put("format",BACKUP_FORMAT_V3);backup.put("createdAt",LocalDateTime.now().toString());Map<String,Object>data=new LinkedHashMap<>();for(String t:TABLES)data.put(t.replace("\"",""),jdbc.queryForList("SELECT * FROM "+t));backup.put("data",data);Map<String,Object>files=new LinkedHashMap<>();List<Map<String,String>>homeImages=exportGroupHomeImages();List<Map<String,String>>fonts=exportFonts();files.put("groupHomeImages",homeImages);files.put("fonts",fonts);backup.put("files",files);byte[]body=mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backup);response.setContentType("application/json");response.setHeader("Content-Disposition","attachment; filename=home-backup-"+LocalDate.now()+".json");response.getOutputStream().write(body);audit.record(auth.getName(),"GET","/admin/data/export","BACKUP_EXPORT",null,"全データ・所属グループ画像・フォントエクスポート（画像"+homeImages.size()+"件、フォント"+fonts.size()+"件）");}
    @PostMapping("/import")@Transactional String importData(@RequestParam MultipartFile backup,@RequestParam String confirmation,Authentication auth)throws IOException{
        if(!"IMPORT".equals(confirmation)||backup.isEmpty()||backup.getSize()>MAX_BACKUP_SIZE)return "redirect:/admin/data/import?error=validation";
        JsonNode root;Map<String,byte[]>homeImages;Map<String,byte[]>fonts;String format;
        try{root=mapper.readTree(backup.getBytes());format=root.path("format").asText();if((!BACKUP_FORMAT_V1.equals(format)&&!BACKUP_FORMAT_V2.equals(format)&&!BACKUP_FORMAT_V3.equals(format))||!root.path("data").isObject())return "redirect:/admin/data/import?error=format";homeImages=(BACKUP_FORMAT_V2.equals(format)||BACKUP_FORMAT_V3.equals(format))?readGroupHomeImages(root):null;fonts=BACKUP_FORMAT_V3.equals(format)?readFonts(root):null;if(homeImages!=null)validateGroupHomeImageReferences(root.path("data"),homeImages.keySet());if(fonts!=null)validateFontReferences(root.path("data"),fonts.keySet());}
        catch(IOException|RuntimeException ex){return "redirect:/admin/data/import?error=files";}
        List<String>reverse=new ArrayList<>(TABLES);Collections.reverse(reverse);for(String t:reverse)jdbc.update("DELETE FROM "+t);for(String t:TABLES){String key=t.replace("\"","");JsonNode rows=root.path("data").path(key);if(rows.isArray()&&rows.size()>0)jdbc.update("INSERT INTO "+t+" SELECT * FROM json_populate_recordset(NULL::"+t+", CAST(? AS json))",rows.toString());}resetSequences();if(homeImages!=null)replaceGroupHomeImages(homeImages);if(fonts!=null)replaceFiles(FONT_DIR,fonts,".fonts-import-",".fonts-previous-");audit.record(auth.getName(),"POST","/admin/data/import","BACKUP_IMPORT",null,"バックアップ全置換インポート（画像"+(homeImages==null?"旧形式":homeImages.size()+"件")+"、フォント"+(fonts==null?"旧形式":fonts.size()+"件")+"）");return "redirect:/admin/data/import?imported";
    }
    @PostMapping("/initialize") @Transactional
    String initialize(@RequestParam String confirmation,@RequestParam String password,Authentication auth,HttpServletRequest request){
        List<String> hashes=jdbc.query("SELECT user_password FROM \"user\" WHERE user_id=?",(rs,rowNum)->rs.getString(1),auth.getName());
        if(!"INITIALIZE".equals(confirmation)||hashes.isEmpty()||!encoder.matches(password,hashes.get(0)))return "redirect:/admin/data/initialize?error=authentication";
        jdbc.execute("TRUNCATE TABLE "+String.join(",",RESET_TABLES)+" RESTART IDENTITY CASCADE");
        initializer.run(null);
        clearUploadedFonts();
        clearGroupHomeImages();
        if(request.getSession(false)!=null)request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?initialized";
    }
    private void clearUploadedFonts(){
        if(!Files.isDirectory(FONT_DIR))return;
        try(var files=Files.list(FONT_DIR)){files.filter(Files::isRegularFile).forEach(path->{try{Files.deleteIfExists(path);}catch(IOException e){throw new UncheckedIOException(e);}});}
        catch(IOException e){throw new UncheckedIOException(e);}
    }
    private List<Map<String,String>> exportGroupHomeImages()throws IOException{
        List<String>names=jdbc.query("SELECT DISTINCT value FROM group_setting WHERE key='home_image_file' AND value IS NOT NULL AND value<>'' ORDER BY value",(rs,rowNum)->rs.getString(1));
        List<Map<String,String>>files=new ArrayList<>();long total=0;
        for(String name:names){Path path=safeGroupHomeImagePath(name);if(!Files.isRegularFile(path))throw new IOException("所属グループ画像が見つかりません: "+name);byte[]bytes=Files.readAllBytes(path);if(bytes.length>MAX_HOME_IMAGE_SIZE||!isSupportedImage(name,bytes))throw new IOException("所属グループ画像が不正です: "+name);total+=bytes.length;if(total>MAX_HOME_IMAGES_SIZE)throw new IOException("所属グループ画像の合計サイズが上限を超えています");Map<String,String>item=new LinkedHashMap<>();item.put("name",name);item.put("data",Base64.getEncoder().encodeToString(bytes));files.add(item);}
        return files;
    }
    private List<Map<String,String>>exportFonts()throws IOException{
        List<String>names=fontFileNames();
        List<Map<String,String>>files=new ArrayList<>();long total=0;
        for(String name:names){Path path=safeFontPath(name);if(!Files.isRegularFile(path))throw new IOException("アップロードフォントが見つかりません: "+name);byte[]bytes=Files.readAllBytes(path);if(bytes.length>MAX_FONT_SIZE||!isSupportedFont(name,bytes))throw new IOException("アップロードフォントが不正です: "+name);total+=bytes.length;if(total>MAX_FONTS_SIZE)throw new IOException("アップロードフォントの合計サイズが上限を超えています");Map<String,String>item=new LinkedHashMap<>();item.put("name",name);item.put("data",Base64.getEncoder().encodeToString(bytes));files.add(item);}
        return files;
    }
    private List<String>fontFileNames()throws IOException{
        SortedSet<String>names=new TreeSet<>();
        List<Map<String,Object>>settings=jdbc.queryForList("SELECT key,value FROM system_setting WHERE (key LIKE 'uploaded_font.%' OR key IN ('admin_font_family','user_font_family')) AND value IS NOT NULL AND value<>''");
        for(Map<String,Object>setting:settings){String key=Objects.toString(setting.get("key"),"");String name=Objects.toString(setting.get("value"),"");boolean uploaded=key.startsWith("uploaded_font.");boolean selected=("admin_font_family".equals(key)||"user_font_family".equals(key))&&name.matches("[0-9a-fA-F-]{36}\\.(ttf|otf)");if(uploaded||selected){safeFontPath(name);names.add(name);}}
        if(Files.isDirectory(FONT_DIR)){try(var paths=Files.list(FONT_DIR)){for(Path path:paths.filter(Files::isRegularFile).toList()){String name=path.getFileName().toString();if(name.matches("[0-9a-fA-F-]{36}\\.(ttf|otf)"))names.add(name);}}}
        return new ArrayList<>(names);
    }
    private Map<String,byte[]>readGroupHomeImages(JsonNode root)throws IOException{
        JsonNode images=root.path("files").path("groupHomeImages");if(!images.isArray())throw new IOException("所属グループ画像情報がありません");Map<String,byte[]>result=new LinkedHashMap<>();long total=0;
        for(JsonNode image:images){String name=image.path("name").asText();safeGroupHomeImagePath(name);byte[]bytes;try{bytes=Base64.getDecoder().decode(image.path("data").asText());}catch(IllegalArgumentException ex){throw new IOException("所属グループ画像を復号できません: "+name,ex);}if(bytes.length==0||bytes.length>MAX_HOME_IMAGE_SIZE||!isSupportedImage(name,bytes))throw new IOException("所属グループ画像が不正です: "+name);total+=bytes.length;if(total>MAX_HOME_IMAGES_SIZE)throw new IOException("所属グループ画像の合計サイズが上限を超えています");if(result.putIfAbsent(name,bytes)!=null)throw new IOException("所属グループ画像名が重複しています: "+name);}
        return result;
    }
    private Map<String,byte[]>readFonts(JsonNode root)throws IOException{
        JsonNode nodes=root.path("files").path("fonts");if(!nodes.isArray())throw new IOException("アップロードフォント情報がありません");Map<String,byte[]>result=new LinkedHashMap<>();long total=0;
        for(JsonNode node:nodes){String name=node.path("name").asText();safeFontPath(name);byte[]bytes;try{bytes=Base64.getDecoder().decode(node.path("data").asText());}catch(IllegalArgumentException ex){throw new IOException("アップロードフォントを復号できません: "+name,ex);}if(bytes.length==0||bytes.length>MAX_FONT_SIZE||!isSupportedFont(name,bytes))throw new IOException("アップロードフォントが不正です: "+name);total+=bytes.length;if(total>MAX_FONTS_SIZE)throw new IOException("アップロードフォントの合計サイズが上限を超えています");if(result.putIfAbsent(name,bytes)!=null)throw new IOException("アップロードフォント名が重複しています: "+name);}
        return result;
    }
    private void validateGroupHomeImageReferences(JsonNode data,Set<String>images)throws IOException{
        JsonNode settings=data.path("group_setting");if(!settings.isArray())return;for(JsonNode row:settings){if("home_image_file".equals(row.path("key").asText())){String name=row.path("value").asText();if(!name.isBlank()&&!images.contains(name))throw new IOException("DBが参照する所属グループ画像がバックアップにありません: "+name);}}
    }
    private void validateFontReferences(JsonNode data,Set<String>fonts)throws IOException{
        JsonNode settings=data.path("system_setting");if(!settings.isArray())return;
        for(JsonNode row:settings){String key=row.path("key").asText();String name=row.path("value").asText();boolean uploaded=key.startsWith("uploaded_font.");boolean selected=("admin_font_family".equals(key)||"user_font_family".equals(key))&&name.matches("[0-9a-fA-F-]{36}\\.(ttf|otf)");if((uploaded||selected)&&!name.isBlank()&&!fonts.contains(name))throw new IOException("DBが参照するアップロードフォントがバックアップにありません: "+name);}
    }
    private Path safeGroupHomeImagePath(String name)throws IOException{
        if(name==null||!name.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp|gif)"))throw new IOException("所属グループ画像名が不正です");Path path=GROUP_HOME_IMAGE_DIR.resolve(name).normalize();if(!path.startsWith(GROUP_HOME_IMAGE_DIR))throw new IOException("所属グループ画像のパスが不正です");return path;
    }
    private Path safeFontPath(String name)throws IOException{
        if(name==null||!name.matches("[0-9a-fA-F-]{36}\\.(ttf|otf)"))throw new IOException("アップロードフォント名が不正です");Path path=FONT_DIR.resolve(name).normalize();if(!path.startsWith(FONT_DIR))throw new IOException("アップロードフォントのパスが不正です");return path;
    }
    private boolean isSupportedImage(String name,byte[]bytes){String extension=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);return switch(extension){case "jpg"->bytes.length>=3&&(bytes[0]&255)==0xff&&(bytes[1]&255)==0xd8&&(bytes[2]&255)==0xff;case "png"->bytes.length>=8&&(bytes[0]&255)==0x89&&bytes[1]==0x50&&bytes[2]==0x4e&&bytes[3]==0x47&&bytes[4]==0x0d&&bytes[5]==0x0a&&bytes[6]==0x1a&&bytes[7]==0x0a;case "gif"->bytes.length>=6&&new String(bytes,0,6,StandardCharsets.US_ASCII).matches("GIF8[79]a");case "webp"->bytes.length>=12&&new String(bytes,0,4,StandardCharsets.US_ASCII).equals("RIFF")&&new String(bytes,8,4,StandardCharsets.US_ASCII).equals("WEBP");default->false;};}
    private boolean isSupportedFont(String name,byte[]bytes){if(bytes.length<4)return false;String extension=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);return "ttf".equals(extension)?bytes[0]==0&&bytes[1]==1&&bytes[2]==0&&bytes[3]==0:"otf".equals(extension)&&bytes[0]=='O'&&bytes[1]=='T'&&bytes[2]=='T'&&bytes[3]=='O';}
    private void replaceGroupHomeImages(Map<String,byte[]>images){
        Path parent=GROUP_HOME_IMAGE_DIR.getParent(),staged=null,previous=null;
        try{Files.createDirectories(parent);Files.createDirectories(GROUP_HOME_IMAGE_DIR);staged=Files.createTempDirectory(parent,".group-home-images-import-");previous=Files.createTempDirectory(parent,".group-home-images-previous-");for(Map.Entry<String,byte[]>entry:images.entrySet())Files.write(staged.resolve(entry.getKey()),entry.getValue(),StandardOpenOption.CREATE_NEW);copyDirectoryFiles(GROUP_HOME_IMAGE_DIR,previous);clearDirectoryContents(GROUP_HOME_IMAGE_DIR);copyDirectoryFiles(staged,GROUP_HOME_IMAGE_DIR);deleteTree(staged);staged=null;deleteTree(previous);previous=null;}
        catch(IOException ex){try{clearDirectoryContents(GROUP_HOME_IMAGE_DIR);if(previous!=null&&Files.exists(previous))copyDirectoryFiles(previous,GROUP_HOME_IMAGE_DIR);}catch(IOException restore){ex.addSuppressed(restore);}if(staged!=null)try{deleteTree(staged);}catch(IOException cleanup){ex.addSuppressed(cleanup);}if(previous!=null)try{deleteTree(previous);}catch(IOException cleanup){ex.addSuppressed(cleanup);}throw new UncheckedIOException(ex);}
    }
    private void replaceFiles(Path directory,Map<String,byte[]>files,String stagedPrefix,String previousPrefix){
        Path parent=directory.getParent(),staged=null,previous=null;
        try{Files.createDirectories(parent);Files.createDirectories(directory);staged=Files.createTempDirectory(parent,stagedPrefix);previous=Files.createTempDirectory(parent,previousPrefix);for(Map.Entry<String,byte[]>entry:files.entrySet())Files.write(staged.resolve(entry.getKey()),entry.getValue(),StandardOpenOption.CREATE_NEW);copyDirectoryFiles(directory,previous);clearDirectoryContents(directory);copyDirectoryFiles(staged,directory);deleteTree(staged);staged=null;deleteTree(previous);previous=null;}
        catch(IOException ex){try{clearDirectoryContents(directory);if(previous!=null&&Files.exists(previous))copyDirectoryFiles(previous,directory);}catch(IOException restore){ex.addSuppressed(restore);}if(staged!=null)try{deleteTree(staged);}catch(IOException cleanup){ex.addSuppressed(cleanup);}if(previous!=null)try{deleteTree(previous);}catch(IOException cleanup){ex.addSuppressed(cleanup);}throw new UncheckedIOException(ex);}
    }
    private void copyDirectoryFiles(Path source,Path target)throws IOException{Files.createDirectories(target);if(!Files.isDirectory(source))return;try(var files=Files.list(source)){for(Path file:files.filter(Files::isRegularFile).toList())Files.copy(file,target.resolve(file.getFileName()),StandardCopyOption.REPLACE_EXISTING);}}
    private void clearDirectoryContents(Path directory)throws IOException{if(!Files.isDirectory(directory))return;try(var paths=Files.list(directory)){for(Path path:paths.toList())deleteTree(path);}}
    private void clearGroupHomeImages(){try{Files.createDirectories(GROUP_HOME_IMAGE_DIR);clearDirectoryContents(GROUP_HOME_IMAGE_DIR);}catch(IOException ex){throw new UncheckedIOException(ex);}}
    private void deleteTree(Path root)throws IOException{if(!Files.exists(root))return;try(var paths=Files.walk(root)){for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);}}
    private void resetSequences(){for(String t:List.of("shopping","communication","operation_log","version_info","notification_history","error_log")){try{jdbc.execute("SELECT setval(pg_get_serial_sequence('"+t+"','id'),COALESCE((SELECT max(id) FROM "+t+"),1),true)");}catch(Exception ignored){}}}
    private Map<String,Integer>counts(){Map<String,Integer>counts=new LinkedHashMap<>();for(String t:TABLES)counts.put(t.replace("\"",""),jdbc.queryForObject("SELECT count(*) FROM "+t,Integer.class));return counts;}
}
