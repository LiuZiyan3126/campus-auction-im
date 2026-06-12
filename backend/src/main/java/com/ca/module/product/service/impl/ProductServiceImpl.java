package com.ca.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.product.dto.ProductCreateDTO;
import com.ca.module.product.dto.ProductUpdateDTO;
import com.ca.module.product.entity.*;
import com.ca.module.product.mapper.*;
import com.ca.module.product.service.ProductService;
import com.ca.module.product.vo.ProductCardVO;
import com.ca.module.product.vo.ProductVO;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final ProductFavoriteMapper favoriteMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ProductVO create(Long sellerId, ProductCreateDTO dto, List<String> imageUrls) {
        User seller = userMapper.selectById(sellerId);
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setType(dto.getType());
        product.setViewCount(0);

        if ("AUCTION".equals(dto.getType())) {
            product.setStartPrice(dto.getStartPrice());
            product.setCurrentPrice(dto.getStartPrice());
            product.setAuctionStart(LocalDateTime.now());
            product.setAuctionEnd(LocalDateTime.now().plusHours(dto.getAuctionDuration() != null ? dto.getAuctionDuration() : 24));
        } else {
            product.setFixedPrice(dto.getFixedPrice());
        }

        // 信誉分>=80自动通过，否则等待审核
        if (seller.getReputationScore() != null && seller.getReputationScore() >= 80) {
            product.setStatus("ACTIVE");
        } else {
            product.setStatus("PENDING");
        }
        productMapper.insert(product);

        // 保存图片
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage img = new ProductImage();
            img.setProductId(product.getId());
            img.setImageUrl(imageUrls.get(i));
            img.setSortOrder(i);
            imageMapper.insert(img);
        }

        log.info("商品发布成功: productId={}, type={}", product.getId(), dto.getType());
        return getById(product.getId());
    }

    @Override
    public ProductVO getById(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw BusinessException.notFound("商品不存在");

        // 浏览量+1
        product.setViewCount(product.getViewCount() + 1);
        productMapper.updateById(product);

        User seller = userMapper.selectById(product.getSellerId());
        Category category = categoryMapper.selectById(product.getCategoryId());

        List<String> images = imageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSortOrder))
                .stream().map(ProductImage::getImageUrl).collect(Collectors.toList());

        String winnerName = null;
        if (product.getWinnerId() != null) {
            User winner = userMapper.selectById(product.getWinnerId());
            if (winner != null) winnerName = winner.getUsername();
        }

        return ProductVO.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .sellerName(seller != null ? seller.getUsername() : null)
                .sellerAvatar(seller != null ? seller.getAvatar() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .type(product.getType())
                .startPrice(product.getStartPrice())
                .fixedPrice(product.getFixedPrice())
                .currentPrice(product.getCurrentPrice())
                .status(product.getStatus())
                .auctionStart(product.getAuctionStart())
                .auctionEnd(product.getAuctionEnd())
                .winnerId(product.getWinnerId())
                .winnerName(winnerName)
                .viewCount(product.getViewCount())
                .images(images)
                .createdAt(product.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ProductVO update(Long userId, Long productId, ProductUpdateDTO dto) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw BusinessException.notFound("商品不存在");
        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能编辑自己的商品");
        }
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) product.setCategoryId(dto.getCategoryId());
        if (dto.getStartPrice() != null) product.setStartPrice(dto.getStartPrice());
        if (dto.getFixedPrice() != null) product.setFixedPrice(dto.getFixedPrice());
        productMapper.updateById(product);
        return getById(productId);
    }

    @Override
    public IPage<ProductCardVO> list(int page, int size, String sort) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "ACTIVE");
        applySort(wrapper, sort);
        Page<Product> pager = new Page<>(page, size);
        IPage<Product> result = productMapper.selectPage(pager, wrapper);
        return result.convert(this::toCardVO);
    }

    @Override
    public IPage<ProductCardVO> search(String q, Integer categoryId, Integer minPrice,
                                        Integer maxPrice, String type, String sort, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "ACTIVE");

        if (q != null && !q.isBlank()) {
            wrapper.and(w -> w.like(Product::getTitle, q).or().like(Product::getDescription, q));
        }
        if (categoryId != null) wrapper.eq(Product::getCategoryId, categoryId);
        if (type != null) wrapper.eq(Product::getType, type);
        if (minPrice != null) {
            wrapper.and(w -> w.ge(Product::getCurrentPrice, BigDecimal.valueOf(minPrice))
                    .or().ge(Product::getFixedPrice, BigDecimal.valueOf(minPrice)));
        }
        if (maxPrice != null) {
            wrapper.and(w -> w.le(Product::getCurrentPrice, BigDecimal.valueOf(maxPrice))
                    .or().le(Product::getFixedPrice, BigDecimal.valueOf(maxPrice)));
        }
        applySort(wrapper, sort);

        Page<Product> pager = new Page<>(page, size);
        IPage<Product> result = productMapper.selectPage(pager, wrapper);
        return result.convert(this::toCardVO);
    }

    @Override
    public boolean toggleFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId);
        ProductFavorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            ProductFavorite fav = new ProductFavorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            return true;
        }
    }

    @Override
    public IPage<ProductCardVO> myFavorites(Long userId, int page, int size) {
        LambdaQueryWrapper<ProductFavorite> favWrapper = new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreatedAt);
        Page<ProductFavorite> favPage = new Page<>(page, size);
        IPage<ProductFavorite> favResult = favoriteMapper.selectPage(favPage, favWrapper);

        return favResult.convert(fav -> {
            Product p = productMapper.selectById(fav.getProductId());
            return p != null ? toCardVO(p) : null;
        });
    }

    private ProductCardVO toCardVO(Product p) {
        User seller = userMapper.selectById(p.getSellerId());
        ProductImage cover = imageMapper.selectOne(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, p.getId())
                .eq(ProductImage::getSortOrder, 0));
        return ProductCardVO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .type(p.getType())
                .fixedPrice(p.getFixedPrice())
                .startPrice(p.getStartPrice())
                .currentPrice(p.getCurrentPrice())
                .coverImage(cover != null ? cover.getImageUrl() : null)
                .viewCount(p.getViewCount())
                .status(p.getStatus())
                .sellerName(seller != null ? seller.getUsername() : null)
                .sellerAvatar(seller != null ? seller.getAvatar() : null)
                .createdAt(p.getCreatedAt())
                .auctionEnd(p.getAuctionEnd())
                .build();
    }

    private void applySort(LambdaQueryWrapper<Product> wrapper, String sort) {
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getCurrentPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getCurrentPrice);
        } else if ("view_count".equals(sort)) {
            wrapper.orderByDesc(Product::getViewCount);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt); // 默认最新
        }
    }
}
