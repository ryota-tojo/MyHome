package com.example.home_api.web;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Controller;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.GetMapping;
@Controller public class MaintenanceController {private final JdbcTemplate jdbc;public MaintenanceController(JdbcTemplate jdbc){this.jdbc=jdbc;}@GetMapping("/system-maintenance")String page(Model model){model.addAttribute("state",jdbc.queryForMap("SELECT * FROM system_state WHERE id=1"));return "maintenance";}}
