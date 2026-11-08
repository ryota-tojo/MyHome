package com.example.home_api.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    public AccountController(JdbcTemplate jdbc,PasswordEncoder encoder){this.jdbc=jdbc;this.encoder=encoder;}
    @GetMapping("/account/change-password") String view(Authentication auth,Model model){
        model.addAttribute("currentUserId",auth.getName());
        model.addAttribute("initialAdmin",Boolean.TRUE.equals(jdbc.queryForObject("SELECT permission_flag=2 AND password_change_required=1 FROM \"user\" WHERE user_id=?",Boolean.class,auth.getName())));
        return "change-password";
    }
    @GetMapping("/account/profile")
    String profile(Authentication auth,Model model){
        model.addAttribute("user",jdbc.queryForMap("SELECT user_id,user_name FROM \"user\" WHERE user_id=?",auth.getName()));
        model.addAttribute("isAdmin",auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")));
        return "account-profile";
    }
    @PostMapping("/account/profile")
    String updateProfile(@RequestParam String userId,@RequestParam String userName,@RequestParam(required=false,defaultValue="")String password,@RequestParam(required=false,defaultValue="")String confirmation,Authentication auth,Model model,HttpServletRequest request){
        if(userName.isBlank()||userName.length()>100){model.addAttribute("error","表示名を確認してください。");return profile(auth,model);}
        if(!userId.matches("[A-Za-z0-9_-]{4,64}")||(!userId.equals(auth.getName())&&jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=?",Integer.class,userId)>0)){model.addAttribute("error","ユーザーIDの形式または重複を確認してください。");return profile(auth,model);}
        if(!password.isBlank()&&(password.length()<8||!password.equals(confirmation))){model.addAttribute("error","パスワードは8文字以上で、確認用と一致させてください。");return profile(auth,model);}
        if(!userId.equals(auth.getName()))jdbc.update("DELETE FROM auto_login_token WHERE user_id=?",auth.getName());
        jdbc.update("UPDATE \"user\" SET user_id=?,user_name=?,update_date=CURRENT_TIMESTAMP WHERE user_id=?",userId,userName,auth.getName());
        if(!password.isBlank()){jdbc.update("UPDATE \"user\" SET user_password=?,password_change_required=0,password_changed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=?",encoder.encode(password),userId);jdbc.update("DELETE FROM auto_login_token WHERE user_id=?",userId);}
        if(!userId.equals(auth.getName())){try{request.logout();}catch(Exception ignored){}return "redirect:/login?userIdChanged";}
        return "redirect:/account/profile?saved";
    }
    @PostMapping("/account/change-password") String change(@RequestParam String password,@RequestParam String confirmation,@RequestParam(required=false)String userId,Authentication auth,Model model,HttpServletRequest request){
        boolean initialAdmin=Boolean.TRUE.equals(jdbc.queryForObject("SELECT permission_flag=2 AND password_change_required=1 FROM \"user\" WHERE user_id=?",Boolean.class,auth.getName()));
        String nextId=initialAdmin&&userId!=null?userId.trim():auth.getName();
        if(password.length()<8){model.addAttribute("error","パスワードは8文字以上で入力してください。");return changeError(auth,model,initialAdmin);}
        if(!password.equals(confirmation)){model.addAttribute("error","確認用パスワードが一致しません。");return changeError(auth,model,initialAdmin);}
        if(password.equals("adminadmin")){model.addAttribute("error","初期パスワードとは異なるパスワードを設定してください。");return changeError(auth,model,initialAdmin);}
        if(initialAdmin&&(!nextId.matches("[A-Za-z0-9_-]{4,32}")||(!nextId.equals(auth.getName())&&jdbc.queryForObject("SELECT count(*) FROM \"user\" WHERE user_id=?",Integer.class,nextId)>0))){model.addAttribute("error","ユーザーIDは半角英数字・_・-の4～32文字で、未使用の値を指定してください。");return changeError(auth,model,true);}
        if(initialAdmin&&!nextId.equals(auth.getName()))jdbc.update("UPDATE \"user\" SET user_id=? WHERE user_id=?",nextId,auth.getName());
        jdbc.update("UPDATE \"user\" SET user_password=?,password_change_required=0,password_changed_at=CURRENT_TIMESTAMP,update_date=CURRENT_TIMESTAMP WHERE user_id=?",encoder.encode(password),nextId);
        jdbc.update("DELETE FROM auto_login_token WHERE user_id=?",nextId);
        if(initialAdmin){try{request.logout();}catch(Exception ignored){}return "redirect:/login?initialSetupCompleted";}
        boolean admin=auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        if(admin)return "redirect:/admin?passwordChanged";
        Integer groups=jdbc.queryForObject("SELECT count(*) FROM group_member WHERE user_id=? AND approve_flag=1",Integer.class,auth.getName());
        return groups==null||groups==0?"redirect:/groups/setup":groups==1?"redirect:/groups/prepare":"redirect:/groups/select";
    }
    private String changeError(Authentication auth,Model model,boolean initialAdmin){model.addAttribute("currentUserId",auth.getName());model.addAttribute("initialAdmin",initialAdmin);return "change-password";}
}
