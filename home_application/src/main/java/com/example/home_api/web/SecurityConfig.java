package com.example.home_api.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean FilterRegistrationBean<PasswordChangeRequiredFilter> passwordChangeFilterRegistration(PasswordChangeRequiredFilter filter){
        FilterRegistrationBean<PasswordChangeRequiredFilter> registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;
    }

    @Bean FilterRegistrationBean<GroupAccessRequiredFilter> groupAccessFilterRegistration(GroupAccessRequiredFilter filter){
        FilterRegistrationBean<GroupAccessRequiredFilter> registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler,LoginFailureHandler failureHandler,
                                            PasswordChangeRequiredFilter passwordChangeRequiredFilter,
                                            GroupAccessRequiredFilter groupAccessRequiredFilter,MaintenanceFilter maintenanceFilter) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/theme.css", "/fonts/**", "/error", "/system-maintenance").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/groups/**").hasRole("USER")
                        .requestMatchers("/", "/shopping/**", "/budgets/**", "/communication/**").hasRole("USER")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").successHandler(successHandler).failureHandler(failureHandler).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                .addFilterAfter(passwordChangeRequiredFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(groupAccessRequiredFilter, PasswordChangeRequiredFilter.class)
                .addFilterAfter(maintenanceFilter, GroupAccessRequiredFilter.class)
                .build();
    }

    @Bean FilterRegistrationBean<MaintenanceFilter> maintenanceFilterRegistration(MaintenanceFilter filter){FilterRegistrationBean<MaintenanceFilter> registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
}
