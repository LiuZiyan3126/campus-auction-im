package com.ca.module.auction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.util.PageVO;
import com.ca.common.util.ResultVO;
import com.ca.module.auction.dto.BidDTO;
import com.ca.module.auction.service.AuctionService;
import com.ca.module.auction.vo.BidVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products/{productId}/bids")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResultVO<BidVO> placeBid(Authentication auth,
                                     @PathVariable Long productId,
                                     @Valid @RequestBody BidDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return ResultVO.ok("出价成功", auctionService.placeBid(userId, productId, dto.getBidAmount()));
    }

    @GetMapping
    public ResultVO<PageVO<BidVO>> getBidHistory(@PathVariable Long productId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        IPage<BidVO> result = auctionService.getBidHistory(productId, page, size);
        return ResultVO.ok(PageVO.of(result));
    }

    @GetMapping("/current-price")
    public ResultVO<BigDecimal> getCurrentPrice(@PathVariable Long productId) {
        return ResultVO.ok(auctionService.getCurrentPrice(productId));
    }
}
