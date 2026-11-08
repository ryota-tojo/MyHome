package com.example.home_api.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Controller
public class RegistrationController {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    public RegistrationController(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc=jdbc; this.encoder=encoder; }
    @GetMapping("/login") String login() { return "login"; }
    @GetMapping("/register") String register(Model m) { m.addAttribute("form",new RegistrationForm("","","","")); return "register"; }

    @PostMapping("/register") @Transactional
    String register(@Valid @ModelAttribute("form") RegistrationForm f, BindingResult errors, Model model) {
        if(!Objects.equals(f.password(),f.passwordConfirmation())) {
            errors.rejectValue("passwordConfirmation","password.mismatch","パスワードと確認用パスワードが一致しません。");
        }
        if(errors.hasErrors()) return "register";
        try {
            LocalDateTime now=LocalDateTime.now();
            jdbc.update("INSERT INTO \"user\"(user_id,user_password,user_name,permission_flag,approve_flag,delete_flag,password_change_required,approve_date,create_date,update_date) VALUES (?,?,?,?,1,0,0,?,?,?)",
                    f.userId(),encoder.encode(f.password()),f.userName(),1,now,now,now);
            return "redirect:/login?registered";
        } catch(DuplicateKeyException ex) { model.addAttribute("registrationError","このユーザーIDは既に使用されています。"); return "register"; }
    }
    public record RegistrationForm(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{4,32}") String userId,
        @NotBlank @Size(min=8,max=72) String password,
        @NotBlank @Size(min=8,max=72) String passwordConfirmation,
        @NotBlank @Size(max=64) String userName) {}
}
