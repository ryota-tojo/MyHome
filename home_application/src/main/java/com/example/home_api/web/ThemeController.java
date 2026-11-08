package com.example.home_api.web;

import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
public class ThemeController {
    private final JdbcTemplate jdbc;
    private final Path fontDir=Path.of("data","fonts").toAbsolutePath().normalize();
    public ThemeController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping(value="/theme.css",produces="text/css") @ResponseBody
    ResponseEntity<String> css(){
        Map<String,String>s=new HashMap<>();
        jdbc.query("SELECT key,value FROM system_setting",rs->{s.put(rs.getString(1),rs.getString(2));});
        boolean maintenance=Boolean.TRUE.equals(jdbc.queryForObject("SELECT maintenance_enabled FROM system_state WHERE id=1",Boolean.class));
        String adminHeader=maintenance?v(s,"admin_maintenance_header_color","#f8d7da"):v(s,"admin_header_color","#EBEBEB");
        String userHeader=maintenance?v(s,"user_maintenance_header_color","#f8d7da"):v(s,"user_header_color","#EBEBEB");
        List<String> uploaded=jdbc.query("SELECT value FROM system_setting WHERE key LIKE 'uploaded_font.%'",(rs,rowNum)->rs.getString(1));
        Set<String> uploadedSet=new HashSet<>(uploaded);
        StringBuilder faces=new StringBuilder();
        for(String file:uploaded){
            if(file.matches("[0-9a-fA-F-]+\\.(ttf|otf)"))
                faces.append("@font-face{font-family:'").append(fontFamily(file)).append("';src:url('/fonts/").append(file)
                        .append("') format('").append(file.endsWith(".ttf")?"truetype":"opentype").append("');font-display:swap;}");
        }
        String fontFace=faces.toString();
        String adminFont=cssFont(v(s,"admin_font_family","system-ui"),uploadedSet);
        String userFont=cssFont(v(s,"user_font_family","system-ui"),uploadedSet);
        String css=fontFace+":root{--admin-header:"+adminHeader+";--admin-body:"+v(s,"admin_body_color","#e1fad7")+";--admin-font:"+adminFont+";--user-header:"+userHeader+";--user-body:"+v(s,"user_body_color","#FAEBD7")+";--user-font:"+userFont+"}"
                +"html{font-size:calc("+number(v(s,"font_size","16"))+"px * "+number(v(s,"ui_scale","100"))+"/100)}"
                +".admin-page{background:var(--admin-body)!important;font-family:var(--admin-font)!important}"
                +".admin-page button,.admin-page input,.admin-page select,.admin-page textarea{font-family:inherit}"
                +".admin-nav{background:var(--admin-header)!important}"
                +"body:not(.admin-page){background:var(--user-body);font-family:var(--user-font)!important}"
                +"body:not(.admin-page) *,body:not(.admin-page) button,body:not(.admin-page) input,body:not(.admin-page) select,body:not(.admin-page) textarea{font-family:var(--user-font)!important}"
                +".theme-nav{background:var(--user-header)!important}";
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.valueOf("text/css")).body(css);
    }

    @PostMapping("/admin/settings/font")
    String upload(@RequestParam MultipartFile font)throws IOException{
        String original=Optional.ofNullable(font.getOriginalFilename()).orElse("");
        String lower=original.toLowerCase(Locale.ROOT);
        String ext=lower.endsWith(".ttf")?".ttf":lower.endsWith(".otf")?".otf":"";
        byte[]bytes=font.getBytes();
        if(ext.isEmpty()||bytes.length==0||bytes.length>5*1024*1024||!valid(bytes,ext))return "redirect:/admin/settings/display?fontError";
        Files.createDirectories(fontDir);
        String name=UUID.randomUUID()+ext;
        Files.write(fontDir.resolve(name),bytes,StandardOpenOption.CREATE_NEW);
        String displayName;
        try{displayName=Path.of(original).getFileName().toString();}catch(InvalidPathException e){displayName="uploaded-font"+ext;}
        displayName=displayName.replaceAll("[\\p{Cntrl}]", "").trim();
        if(displayName.isBlank())displayName="uploaded-font"+ext;
        jdbc.update("INSERT INTO system_setting(key,value,description) VALUES (?,?,?)","uploaded_font."+UUID.randomUUID(),name,displayName);
        return "redirect:/admin/settings/display?fontSaved";
    }

    @GetMapping("/fonts/{name}")
    ResponseEntity<byte[]>font(@PathVariable String name)throws IOException{
        if(!name.matches("[0-9a-fA-F-]+\\.(ttf|otf)"))return ResponseEntity.notFound().build();
        Path p=fontDir.resolve(name).normalize();
        if(!p.startsWith(fontDir)||!Files.exists(p))return ResponseEntity.notFound().build();
        MediaType type=MediaType.valueOf(name.endsWith(".ttf")?"font/ttf":"font/otf");
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).contentType(type).body(Files.readAllBytes(p));
    }

    private boolean valid(byte[]b,String ext){if(b.length<4)return false;return ext.equals(".ttf")?b[0]==0&&b[1]==1&&b[2]==0&&b[3]==0:(b[0]=='O'&&b[1]=='T'&&b[2]=='T'&&b[3]=='O');}
    private String v(Map<String,String>s,String k,String d){return s.getOrDefault(k,d).replaceAll("[{};]","");}
    private String number(String s){return s.matches("\\d{1,3}")?s:"16";}
    private String cssFont(String value,Set<String> uploaded){return uploaded.contains(value)?"'"+fontFamily(value)+"',sans-serif":value;}
    private String fontFamily(String file){return "UploadedFont_"+file.replaceAll("[^0-9a-fA-F]","");}
}
