package com.example.home_api.web;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {
    private final ReportApiService service;

    public ReportApiController(ReportApiService service) {
        this.service = service;
    }

    @GetMapping("/groups")
    List<ReportApiService.GroupTarget> groups() {
        return service.groups();
    }

    @GetMapping("/groups/{groupId}/line-connection")
    ResponseEntity<ReportApiService.LineConnection> lineConnection(@PathVariable String groupId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.lineConnection(groupId));
    }

    @GetMapping("/groups/{groupId}/category-expenses")
    ReportApiService.MonthlyCategoryReport categoryExpenses(
            @PathVariable String groupId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth target = normalize(year, month);
        return service.monthlyCategoryReport(groupId, target.getYear(), target.getMonthValue());
    }

    private YearMonth normalize(Integer year, Integer month) {
        if (year == null && month == null) return YearMonth.now();
        if (year == null || month == null || year < 2000 || year > 2100 || month < 1 || month > 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "yearとmonthを正しく指定してください");
        return YearMonth.of(year, month);
    }
}
