package com.ca.module.auction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.module.auction.dto.BidDTO;
import com.ca.module.auction.vo.BidVO;

import java.math.BigDecimal;

public interface AuctionService {
    BidVO placeBid(Long userId, Long productId, BigDecimal bidAmount);
    IPage<BidVO> getBidHistory(Long productId, int page, int size);
    BigDecimal getCurrentPrice(Long productId);
    void checkExpiredAuctions();
}
