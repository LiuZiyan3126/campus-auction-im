<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="footer-logo">CAIP</span>
        <p>校园二手拍卖与即时通讯平台</p>
      </div>
      <div class="footer-links">
        <div class="footer-col">
          <h4>功能</h4>
          <a href="/">首页</a>
          <a href="/search">发现好物</a>
          <a href="/product/create">发布商品</a>
        </div>
        <div class="footer-col">
          <h4>关于</h4>
          <a>第9组</a>
          <a>软件工程实验 2026</a>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; 2026 第9组 CAIP. All rights reserved.</span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
  margin-top: var(--space-3xl);
}
.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--space-2xl) var(--space-lg) var(--space-lg);
}
.footer-brand {
  text-align: center;
  margin-bottom: var(--space-xl);
}
.footer-logo {
  font-size: 1.4em;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.footer-brand p {
  color: var(--color-text-muted);
  font-size: .85em;
  margin-top: 4px;
}
.footer-links {
  display: flex;
  justify-content: center;
  gap: var(--space-3xl);
  margin-bottom: var(--space-xl);
}
.footer-col h4 {
  font-size: .8em;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--color-text-muted);
  margin-bottom: var(--space-sm);
}
.footer-col a {
  display: block;
  font-size: .875em;
  color: var(--color-text-secondary);
  padding: 4px 0;
  transition: color var(--transition-fast);
  text-decoration: none;
}
.footer-col a:hover { color: var(--color-primary); }
.footer-bottom {
  text-align: center;
  padding-top: var(--space-lg);
  border-top: 1px solid var(--color-border-light);
  color: var(--color-text-muted);
  font-size: .8em;
}
</style>
