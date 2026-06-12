package com.ca.module.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ca.module.admin.vo.StatsOverviewVO;
import com.ca.module.admin.vo.TrendDataVO;
import com.ca.module.product.entity.Category;
import com.ca.module.product.vo.ProductVO;
import com.ca.module.user.vo.UserVO;

import java.util.List;

public interface AdminService {
    // 用户管理
    IPage<UserVO> listUsers(int page, int size, String status, String keyword);
    void verifyUser(Long userId, String action);
    void banUser(Long userId, String action);

    // 商品管理
    IPage<ProductVO> listProducts(int page, int size, String status);
    void auditProduct(Long productId, String action);

    // 分类管理
    List<Category> listCategories();
    Category createCategory(Category category);
    Category updateCategory(Integer id, Category category);
    void deleteCategory(Integer id);

    // 统计
    StatsOverviewVO getOverview();
    List<TrendDataVO> getTrend(int days);
}
