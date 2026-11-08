package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AutoLoginService {
    private static final String SECRET_NAME="auto_login_aes_key";
    private final JdbcTemplate jdbc;private final PasswordEncoder encoder;private final UserDetailsService users;private final SecureRandom random=new SecureRandom();
    public AutoLoginService(JdbcTemplate jdbc,PasswordEncoder encoder,UserDetailsService users){this.jdbc=jdbc;this.encoder=encoder;this.users=users;}

    public AutoLoginLink create(String userId,String password,String groupId,String contextPath){
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT user_password FROM \"user\" WHERE user_id=? AND approve_flag=1 AND delete_flag=0",userId);
        if(rows.isEmpty()||!encoder.matches(password,String.valueOf(rows.get(0).get("user_password"))))throw new IllegalArgumentException("パスワードが正しくありません。");
        if(groupId!=null&&!groupId.isBlank()&&!isMember(userId,groupId))throw new IllegalArgumentException("所属グループを確認できません。");
        byte[] tokenBytes=new byte[32];random.nextBytes(tokenBytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        jdbc.update("DELETE FROM auto_login_token WHERE user_id=?",userId);jdbc.update("INSERT INTO auto_login_token(token_hash,user_id,group_id) VALUES (?,?,?)",hash(token),userId,blankToNull(groupId));
        String userid=encrypt(userId),pw=encrypt(token),group=groupId==null||groupId.isBlank()?null:encrypt(groupId);
        String url=contextPath+"/login?userid="+userid+"&pw="+pw+(group==null?"":"&group="+group);return new AutoLoginLink(url,userid,pw,group);
    }

    public VerifiedLogin verify(String encryptedUser,String encryptedToken,String encryptedGroup){
        String userId=decrypt(encryptedUser),token=decrypt(encryptedToken),group=encryptedGroup==null||encryptedGroup.isBlank()?null:decrypt(encryptedGroup);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT user_id,group_id FROM auto_login_token WHERE token_hash=? AND user_id=?",hash(token),userId);
        if(rows.isEmpty())throw new IllegalArgumentException("自動ログインURLが無効です。");
        String storedGroup=blankToNull((String)rows.get(0).get("group_id"));if(!java.util.Objects.equals(storedGroup,blankToNull(group)))throw new IllegalArgumentException("自動ログインURLが無効です。");
        UserDetails details=users.loadUserByUsername(userId);if(!details.isEnabled()||!details.isAccountNonLocked())throw new IllegalArgumentException("このユーザーではログインできません。");
        if(group!=null&&!isMember(userId,group))throw new IllegalArgumentException("所属グループへアクセスできません。");return new VerifiedLogin(details,group);
    }

    public Map<String,Object> group(String userId,String groupId){return jdbc.queryForMap("SELECT g.group_name,gm.leader_flag FROM group_member gm JOIN \"group\" g ON g.group_id=gm.group_id WHERE gm.user_id=? AND gm.group_id=? AND gm.approve_flag=1 AND g.status='ACTIVE'",userId,groupId);}
    private boolean isMember(String user,String group){Integer n=jdbc.queryForObject("SELECT count(*) FROM group_member gm JOIN \"group\" g ON g.group_id=gm.group_id WHERE gm.user_id=? AND gm.group_id=? AND gm.approve_flag=1 AND g.status='ACTIVE'",Integer.class,user,group);return n!=null&&n>0;}
    private String encrypt(String value){try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key(),"AES"),new GCMParameterSpec(128,iv));byte[] encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));byte[] result=new byte[iv.length+encrypted.length];System.arraycopy(iv,0,result,0,iv.length);System.arraycopy(encrypted,0,result,iv.length,encrypted.length);return Base64.getUrlEncoder().withoutPadding().encodeToString(result);}catch(Exception e){throw new IllegalStateException("自動ログイン情報を暗号化できません。",e);}}
    private String decrypt(String value){try{byte[] all=Base64.getUrlDecoder().decode(value);if(all.length<29)throw new IllegalArgumentException();byte[] iv=java.util.Arrays.copyOfRange(all,0,12),encrypted=java.util.Arrays.copyOfRange(all,12,all.length);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key(),"AES"),new GCMParameterSpec(128,iv));return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalArgumentException("自動ログインURLが無効です。");}}
    private synchronized byte[] key(){List<String> values=jdbc.queryForList("SELECT secret_value FROM application_secret WHERE name=?",String.class,SECRET_NAME);if(values.isEmpty()){byte[] key=new byte[32];random.nextBytes(key);String value=Base64.getEncoder().encodeToString(key);jdbc.update("INSERT INTO application_secret(name,secret_value) VALUES (?,?) ON CONFLICT(name) DO NOTHING",SECRET_NAME,value);values=jdbc.queryForList("SELECT secret_value FROM application_secret WHERE name=?",String.class,SECRET_NAME);}return Base64.getDecoder().decode(values.get(0));}
    private String hash(String value){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private String blankToNull(String value){return value==null||value.isBlank()?null:value;}
    public record AutoLoginLink(String url,String userid,String pw,String group){}
    public record VerifiedLogin(UserDetails user,String groupId){}
}
