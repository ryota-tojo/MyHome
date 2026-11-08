package com.example.home_api.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public DatabaseUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public UserDetails loadUserByUsername(String userId) {
        return jdbc.query("SELECT user_id,user_password,permission_flag,approve_flag,delete_flag,locked_until FROM \"user\" WHERE user_id=?", rs -> {
            if (!rs.next()) throw new UsernameNotFoundException("ユーザーが見つかりません");
            boolean enabled = rs.getInt("approve_flag") == 1 && rs.getInt("delete_flag") != 1;
            String role = rs.getInt("permission_flag") == 2 ? "ADMIN" : "USER";
            return User.withUsername(rs.getString("user_id")).password(rs.getString("user_password"))
                    .roles(role).disabled(!enabled).accountLocked(rs.getTimestamp("locked_until")!=null&&rs.getTimestamp("locked_until").toLocalDateTime().isAfter(java.time.LocalDateTime.now())).build();
        }, userId);
    }
}
