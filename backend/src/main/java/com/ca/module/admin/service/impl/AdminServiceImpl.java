package com.ca.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.admin.service.AdminService;
import com.ca.module.admin.vo.StatsOverviewVO;
import com.ca.module.admin.vo.TrendDataVO;
import com.ca.module.product.entity.Category;
import com.ca.module.product.entity.Product;
import com.ca.module.product.entity.ProductImage;
import com.ca.module.product.mapper.CategoryMapper;
import com.ca.module.product.mapper.ProductImageMapper;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.product.vo.ProductVO;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import com.ca.module.user.vo.UserVO;
import com.ca.module.order.entity.Order;
import com.ca.module.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final CategoryMapper categoryMapper;
    private final OrderMapper orderMapper;

    // === 用户管理 ===

    @Override
    public IPage<UserVO> listUsers(int page, int size, String status, String keyword) {
        Page<User> pager = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) wrapper.eq(User::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getStudentId, keyword).or().like(User::getUsername, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(pager, wrapper).convert(this::toUserVO);
    }

    @Override
    @Transactional
    public void verifyUser(Long userId, String action) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if (!"PENDING".equals(user.getStatus())) throw BusinessException.badRequest("用户非待审核状态");
        if ("APPROVE".equals(action)) {
            user.setStatus("ACTIVE");
        } else {
            user.setStatus("PENDING"); // 驳回保持PENDING
        }
        userMapper.updateById(user);
        log.info("用户审核: userId={}, action={}", userId, action);
    }

    @Override
    @Transactional
    public void banUser(Long userId, String action) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BusinessException.notFound("用户不存在");
        if ("ADMIN".equals(user.getRole())) throw BusinessException.badRequest("不能封禁管理员");
        if ("BAN".equals(action)) {
            user.setStatus("BANNED");
        } else {
            user.setStatus("ACTIVE");
        }
        userMapper.updateById(user);
        log.info("用户封禁操作: userId={}, action={}", userId, action);
    }

    // === 商品管理 ===

    @Override
    public IPage<ProductVO> listProducts(int page, int size, String status) {
        Page<Product> pager = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(pager, wrapper).convert(this::toProductVO);
    }

    @Override
    @Transactional
    public void auditProduct(Long productId, String action) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw BusinessException.notFound("商品不存在");
        if ("APPROVE".equals(action)) {
            product.setStatus("ACTIVE");
        } else {
            product.setStatus("BANNED");
        }
        productMapper.updateById(product);
        log.info("商品审核: productId={}, action={}", productId, action);
    }

    // === 分类管理 ===

    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getParentId));
    }

    @Override
    public Category createCategory(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Integer id, Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return categoryMapper.selectById(id);
    }

    @Override
    public void deleteCategory(Integer id) {
        long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) throw BusinessException.badRequest("该分类下有 " + count + " 件商品，无法删除");
        categoryMapper.deleteById(id);
    }

    // === 统计 ===

    @Override
    public StatsOverviewVO getOverview() {
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "ACTIVE"));
        long totalProducts = productMapper.selectCount(null);
        long activeProducts = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, "ACTIVE"));
        LocalDate today = LocalDate.now();
        long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, today.atStartOfDay()));
        long totalAuction = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getType, "AUCTION"));
        long soldAuction = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getType, "AUCTION").eq(Product::getStatus, "SOLD"));
        double rate = totalAuction > 0 ? (double) soldAuction / totalAuction * 100 : 0;

        return StatsOverviewVO.builder()
                .totalUsers(totalUsers).activeUsers(activeUsers)
                .totalProducts(totalProducts).activeProducts(activeProducts)
                .todayOrders(todayOrders).todayAmount(BigDecimal.ZERO)
                .auctionSuccessRate(Math.round(rate * 10) / 10.0)
                .build();
    }

    @Override
    public List<TrendDataVO> getTrend(int days) {
        List<TrendDataVO> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .between(User::getCreatedAt, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
            long newOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .between(Order::getCreatedAt, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
            list.add(TrendDataVO.builder().date(date.toString()).newUsers(newUsers).newOrders(newOrders).totalAmount(0).build());
        }
        return list;
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId()).studentId(user.getStudentId()).username(user.getUsername())
                .avatar(user.getAvatar()).email(user.getEmail()).phone(user.getPhone())
                .role(user.getRole()).reputationScore(user.getReputationScore())
                .status(user.getStatus()).createdAt(user.getCreatedAt()).build();
    }

    private ProductVO toProductVO(Product p) {
        User seller = userMapper.selectById(p.getSellerId());
        List<String> images = imageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, p.getId()).orderByAsc(ProductImage::getSortOrder))
                .stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        return ProductVO.builder()
                .id(p.getId()).sellerId(p.getSellerId())
                .sellerName(seller != null ? seller.getUsername() : null)
                .title(p.getTitle()).description(p.getDescription())
                .categoryId(p.getCategoryId()).type(p.getType())
                .startPrice(p.getStartPrice()).fixedPrice(p.getFixedPrice())
                .currentPrice(p.getCurrentPrice()).status(p.getStatus())
                .auctionStart(p.getAuctionStart()).auctionEnd(p.getAuctionEnd())
                .viewCount(p.getViewCount()).images(images).createdAt(p.getCreatedAt()).build();
    }
}
