export default {
  mounted(el, binding) {
    // 문자열 or 배열 둘 다 지원
    const pickFallback = () => {
      const v = binding.value;

      // 배열이면 랜덤 선택
      if (Array.isArray(v) && v.length > 0) {
        return v[Math.floor(Math.random() * v.length)];
      }

      // 문자열이면 그대로
      if (typeof v === "string" && v.trim()) return v;

      // 값 없으면 기본
      return "/fallback.png";
    };

    const onError = () => {
      el.removeEventListener("error", onError); // 무한 루프 방지
      el.src = pickFallback();                  // 여기서 랜덤으로 픽
    };

    el.addEventListener("error", onError);
    el.__imgFallback__ = onError;
  },

  unmounted(el) {
    if (el.__imgFallback__) {
      el.removeEventListener("error", el.__imgFallback__);
      delete el.__imgFallback__;
    }
  },
};
