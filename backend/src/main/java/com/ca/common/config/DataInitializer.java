package com.ca.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ca.module.user.entity.User;
import com.ca.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 更新管理员密码
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, "admin"));
        if (admin != null) {
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            log.info("管理员账号密码已更新: admin/admin123");
        }

        // 更新测试用户密码
        User test1 = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, "test001"));
        if (test1 != null) {
            test1.setPasswordHash(passwordEncoder.encode("test123"));
            userMapper.updateById(test1);
            log.info("测试用户密码已更新: test001/test123");
        }
        User test2 = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, "test002"));
        if (test2 != null) {
            test2.setPasswordHash(passwordEncoder.encode("test123"));
            userMapper.updateById(test2);
            log.info("测试用户密码已更新: test002/test123");
        }
    }
}
