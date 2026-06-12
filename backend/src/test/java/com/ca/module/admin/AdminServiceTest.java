package com.ca.module.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ca.common.exception.BusinessException;
import com.ca.module.admin.service.impl.AdminServiceImpl;
import com.ca.module.order.entity.Order;
import com.ca.module.order.mapper.OrderMapper;
import com.ca.module.product.entity.Category;
import com.ca.module.product.entity.Product;
import com.ca.module.product.mapper.CategoryMapper;
import com.ca.module.product.mapper.ProductImageMapper;
import com.ca.module.product.mapper.ProductMapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserMapper userMapper;
    @Mock ProductMapper productMapper;
    @Mock ProductImageMapper imageMapper;
    @Mock CategoryMapper categoryMapper;
    @Mock OrderMapper orderMapper;
    @InjectMocks AdminServiceImpl adminService;

    private User pendingUser;
    private User activeUser;

    @BeforeEach
    void setUp() {
        pendingUser = new User();
        pendingUser.setId(1L);
        pendingUser.setStudentId("20210001");
        pendingUser.setUsername("测试用户");
        pendingUser.setStatus("PENDING");
        pendingUser.setRole("USER");

        activeUser = new User();
        activeUser.setId(2L);
        activeUser.setStatus("ACTIVE");
        activeUser.setRole("USER");
    }

    // ===== 用户审核测试 =====

    @Test
    void testVerifyUser_Approve_ShouldActivate() {
        when(userMapper.selectById(1L)).thenReturn(pendingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        adminService.verifyUser(1L, "APPROVE");

        assertThat(pendingUser.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void testVerifyUser_NonPending_ShouldThrowException() {
        when(userMapper.selectById(2L)).thenReturn(activeUser);

        assertThatThrownBy(() -> adminService.verifyUser(2L, "APPROVE"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("非待审核状态");
    }

    // ===== 封禁测试 =====

    @Test
    void testBanUser_Ban_ShouldSetBanned() {
        when(userMapper.selectById(2L)).thenReturn(activeUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        adminService.banUser(2L, "BAN");

        assertThat(activeUser.getStatus()).isEqualTo("BANNED");
    }

    @Test
    void testBanUser_Admin_ShouldThrowException() {
        activeUser.setRole("ADMIN");
        when(userMapper.selectById(2L)).thenReturn(activeUser);

        assertThatThrownBy(() -> adminService.banUser(2L, "BAN"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能封禁管理员");
    }

    // ===== 分类管理测试 =====

    @Test
    void testDeleteCategory_WithProducts_ShouldThrowException() {
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertThatThrownBy(() -> adminService.deleteCategory(1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法删除");
    }

    @Test
    void testDeleteCategory_Empty_ShouldSucceed() {
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(1)).thenReturn(1);

        adminService.deleteCategory(1);

        verify(categoryMapper).deleteById(1);
    }

    // ===== 统计测试 =====

    @Test
    void testGetOverview_ShouldReturnStats() {
        when(userMapper.selectCount(any())).thenReturn(100L);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);
        when(productMapper.selectCount(any())).thenReturn(200L);
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(80L, 40L);
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        var result = adminService.getOverview();

        assertThat(result.getTotalUsers()).isEqualTo(100);
        assertThat(result.getActiveUsers()).isEqualTo(50);
    }

    @Test
    void testGetTrend_7Days_ShouldReturn7Items() {
        lenient().when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        lenient().when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        var result = adminService.getTrend(7);

        assertThat(result).hasSize(7);
        assertThat(result.get(0).getDate()).isNotNull();
    }
}
