import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:5173';

test.describe('CAIP 端到端测试 — 核心用户旅程', () => {

  test('注册 → 登录 → 浏览 → 发布拍卖 → 出价 → 成交', async ({ page }) => {
    // 1. 访问首页（未登录）
    await page.goto(BASE);
    await expect(page.locator('text=校园二手拍卖')).toBeVisible();

    // 2. 注册新用户
    await page.click('text=注册');
    await page.fill('input[placeholder="学号/工号"]', '20210099');
    await page.fill('input[placeholder="邮箱"]', 'test99@example.com');
    await page.fill('input[placeholder*="密码"]', 'abc12345');
    await page.fill('input[placeholder="确认密码"]', 'abc12345');
    await page.click('text=注册');

    // 3. 管理员审核（模拟：直接DB修改status=ACTIVE，此处跳过）
    // 实际测试时需预先准备ACTIVE状态的测试账号

    // 4. 登录
    await page.goto(BASE + '/login');
    await page.fill('input[placeholder="学号/工号"]', '20210001');
    await page.fill('input[placeholder="密码"]', 'test1234');
    await page.click('button:has-text("登录")');
    await expect(page.locator('text=退出')).toBeVisible();

    // 5. 浏览首页商品
    await page.goto(BASE);
    await expect(page.locator('.product-grid')).toBeVisible();

    // 6. 发布拍卖商品
    await page.click('text=发布');
    await page.fill('input[placeholder="商品标题"]', 'E2E测试拍卖商品');
    await page.click('text=拍卖');
    // 起拍价
    const priceInput = page.locator('.el-input-number input').first();
    await priceInput.fill('10');
    await page.click('text=发布');

    // 7. 验证商品出现在列表
    await page.goto(BASE);
    await expect(page.locator('text=E2E测试拍卖商品')).toBeVisible();

    // 8. 进入商品详情
    await page.click('text=E2E测试拍卖商品');
    await expect(page.locator('text=当前价')).toBeVisible();
    await expect(page.locator('text=出价')).toBeVisible();

    // 9. 出价
    const bidInput = page.locator('.el-input-number input').first();
    await bidInput.fill('15');
    await page.click('text=出价');
    await expect(page.locator('text=出价成功')).toBeVisible();

    // 10. 验证出价记录出现
    await expect(page.locator('.bid-item')).toBeVisible();
  });

  test('一口价购买 → 支付 → 收货流程', async ({ page }) => {
    // 登录
    await page.goto(BASE + '/login');
    await page.fill('input[placeholder="学号/工号"]', '20210001');
    await page.fill('input[placeholder="密码"]', 'test1234');
    await page.click('button:has-text("登录")');

    // 搜索一口价商品
    await page.goto(BASE + '/search?type=SALE');
    await expect(page.locator('.product-card')).toBeVisible();

    // 点击第一个商品
    await page.locator('.product-card').first().click();
    await expect(page.locator('text=立即购买')).toBeVisible();

    // 购买
    await page.click('text=立即购买');

    // 去订单页
    await page.click('text=订单');
    await expect(page.locator('.order-card')).toBeVisible();

    // 支付
    await page.click('text=立即支付');
    await page.click('text=确认');

    // 切换到"已付款"
    await page.click('text=已付款');
  });

  test('IM实时聊天', async ({ page }) => {
    // 登录
    await page.goto(BASE + '/login');
    await page.fill('input[placeholder="学号/工号"]', '20210001');
    await page.fill('input[placeholder="密码"]', 'test1234');
    await page.click('button:has-text("登录")');

    // 进入商品详情，联系卖家
    await page.goto(BASE + '/product/1');
    await page.click('text=联系卖家');

    // 发送消息
    await page.fill('textarea', '你好，这个商品还在吗？');
    await page.press('textarea', 'Enter');

    // 回到消息列表
    await page.click('text=消息');
    await expect(page.locator('.conv-item')).toBeVisible();
  });

  test('管理员审核用户和商品', async ({ page }) => {
    // 以管理员登录
    await page.goto(BASE + '/login');
    await page.fill('input[placeholder="学号/工号"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin123');
    await page.click('button:has-text("登录")');

    // 进入管理后台
    await page.click('text=管理');
    await expect(page.locator('text=总用户数')).toBeVisible();

    // 打开用户管理
    await page.click('text=用户管理');
    await expect(page.locator('.el-table')).toBeVisible();

    // 审核用户（如有待审核）
    const approveBtn = page.locator('button:has-text("通过")');
    if (await approveBtn.count() > 0) {
      await approveBtn.first().click();
    }

    // 打开数据统计
    await page.click('text=数据统计');
    await expect(page.locator('canvas')).toBeVisible();
  });
});
