<template>
    <div class="logout-wrapper">
        <div class="logout-card">
            <div class="spinner"></div>

            <div class="icon">👋</div>

            <h2>로그아웃 중입니다</h2>
            <p>안전하게 로그아웃하고 있어요</p>
        </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import router from '@/router'
import { useAuthStore } from '@/store/authStore'

const authStore = useAuthStore()

onMounted(async () => {
    authStore.logout()

    // 감정선용 딜레이
    await new Promise(r => setTimeout(r, 700))

    router.replace('/')
})
</script>

<style scoped>
.logout-wrapper {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fffaf3;
}

.logout-card {
    background: #ffffff;
    padding: 2.5rem 3rem;
    border-radius: 1.25rem;
    text-align: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    animation: fadeUp 0.4s ease;
}

.icon {
    font-size: 2rem;
    margin-bottom: 0.5rem;
}

h2 {
    font-size: 1.2rem;
    font-weight: 600;
    color: #1b3b6f;
    margin-bottom: 0.25rem;
}

p {
    font-size: 0.9rem;
    color: #777;
}

/* 스피너 */
.spinner {
    width: 36px;
    height: 36px;
    margin: 0 auto 1rem;
    border: 3px solid rgba(255, 145, 77, 0.25);
    border-top-color: #ff914d;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

/* 애니메이션 */
@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

@keyframes fadeUp {
    from {
        opacity: 0;
        transform: translateY(8px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
