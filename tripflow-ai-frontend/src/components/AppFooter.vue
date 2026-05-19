<template>
  <footer class="footer-fms">
    <div class="footer-container">
      <div class="footer-brand">
        <img src="@/assets/img/logo.png" alt="Find My Seoul" width="40" height="40" />
        <span class="footer-title">Find My Seoul</span>
      </div>


      <div class="footer-divider"></div>

      <p class="footer-text">
        © 2025 Find My Seoul · All rights reserved.  
        <br />
        Designed with ❤️ in Seoul.
      </p>
    </div>
  </footer>
</template>

<script setup>
  
</script>

<style scoped>
  .footer-fms {
  background-color: #1B3B6F; /* 진한 남색 */
  color: #fff;
  padding: 2.5rem 1rem;
  text-align: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.08);
}

.footer-container {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1rem;
}

.footer-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1.25rem;
  margin-bottom: 1.25rem;
}

.footer-link {
  color: #ffb347; /* 오렌지 포인트 */
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s ease, text-decoration 0.3s ease;

  &:hover {
    color: #ffa733;
    text-decoration: underline;
  }
}

.footer-divider {
  width: 80%;
  height: 1px;
  background-color: rgba(255, 255, 255, 0.15);
  margin: 1rem 0;
}

.footer-text {
  font-size: 0.85rem;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.8);
}
</style>