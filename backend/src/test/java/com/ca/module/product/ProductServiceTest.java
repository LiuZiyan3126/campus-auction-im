package com.ca.module.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.common.exception.BusinessException;
import com.ca.module.product.dto.ProductCreateDTO;
import com.ca.module.product.dto.ProductUpdateDTO;
import com.ca.module.product.entity.Product;
import com.ca.module.product.entity.ProductFavorite;
import com.ca.module.product.mapper.*;
import com.ca.module.product.service.impl.ProductServiceImpl;
import com.ca.module.product.vo.ProductCardVO;
import com.ca.module.product.vo.ProductVO;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductMapper productMapper;
    @Mock ProductImageMapper imageMapper;
    @Mock ProductFavoriteMapper favoriteMapper;
    @Mock CategoryMapper categoryMapper;
    @Mock UserMapper userMapper;
    @InjectMocks ProductServiceImpl productService;

    // ===== 发布商品测试 =====

    @Test
    void testCreate_HighReputationSeller_ShouldAutoApprove() {
        User seller = new User();
        seller.setId(1L);
        seller.setReputationScore(90);
        when(userMapper.selectById(1L)).thenReturn(seller);
        when(productMapper.insert(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });
        when(productMapper.selectById(100L)).thenReturn(null); // will throw on getById but we test create only

        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setTitle("测试商品");
        dto.setType("AUCTION");
        dto.setStartPrice(new BigDecimal("10"));
        dto.setAuctionDuration(24);
        dto.setCategoryId(1);

        var result = productService.create(1L, dto, Collections.emptyList());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().getTitle()).isEqualTo("测试商品");
    }

    @Test
    void testCreate_LowReputationSeller_ShouldStayPending() {
        User seller = new User();
        seller.setId(1L);
        seller.setReputationScore(50);
        when(userMapper.selectById(1L)).thenReturn(seller);
        when(productMapper.insert(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(100L);
            return 1;
        });

        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setTitle("待审核商品");
        dto.setType("SALE");
        dto.setFixedPrice(new BigDecimal("50"));
        dto.setCategoryId(1);

        productService.create(1L, dto, Collections.emptyList());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
    }

    // ===== 收藏切换测试 =====

    @Test
    void testToggleFavorite_NotFavorited_ShouldInsertAndReturnTrue() {
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(favoriteMapper.insert(any(ProductFavorite.class))).thenReturn(1);

        boolean result = productService.toggleFavorite(1L, 100L);

        assertThat(result).isTrue();
        verify(favoriteMapper).insert(any(ProductFavorite.class));
    }

    @Test
    void testToggleFavorite_AlreadyFavorited_ShouldDeleteAndReturnFalse() {
        ProductFavorite existing = new ProductFavorite();
        existing.setId(1L);
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        boolean result = productService.toggleFavorite(1L, 100L);

        assertThat(result).isFalse();
        verify(favoriteMapper).deleteById(1L);
    }

    // ===== 商品编辑权限测试 =====

    @Test
    void testUpdate_NotOwner_ShouldThrowException() {
        Product product = new Product();
        product.setId(1L);
        product.setSellerId(2L);
        when(productMapper.selectById(1L)).thenReturn(product);

        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setTitle("别人的商品");

        assertThatThrownBy(() -> productService.update(1L, 1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能编辑自己的商品");
    }

    // ===== 搜索测试 =====

    @Test
    void testSearch_EmptyResult_ShouldReturnEmptyPage() {
        when(productMapper.selectPage(any(), any(LambdaQueryWrapper.class)))
                .thenReturn(mock(IPage.class));

        var result = productService.search("不存在的商品", null, null, null, null, "created_at", 1, 20);

        assertThat(result.getRecords()).isEmpty();
    }
}
