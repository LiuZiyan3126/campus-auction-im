package com.ca.module.admin.controller;

import com.ca.common.util.ResultVO;
import com.ca.module.admin.service.AdminService;
import com.ca.module.admin.vo.StatsOverviewVO;
import com.ca.module.admin.vo.TrendDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public ResultVO<StatsOverviewVO> getOverview() {
        return ResultVO.ok(adminService.getOverview());
    }

    @GetMapping("/trend")
    public ResultVO<List<TrendDataVO>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return ResultVO.ok(adminService.getTrend(days));
    }
}
