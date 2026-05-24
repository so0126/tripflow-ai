// k6 공통 옵션 프리셋 — 각 스크립트에서 import 해서 사용한다.
// (k6는 Node가 아니라 자체 JS 런타임으로 .js를 직접 실행한다. npm 설치 불필요.)

// 스모크: k6 ↔ 서버 연결과 스크립트 동작만 빠르게 확인
export const smokeOptions = {
  vus: 1,
  iterations: 5,
  thresholds: {
    checks: ['rate>0.99'],              // check 통과율 99% 이상
    http_req_duration: ['p(95)<1000'],  // p95 1초 미만
  },
};

// Week 1 동시성 부하 프로파일 — 사진 업로드 경로용.
// 스레드풀 Before/After 비교 시 동일 프로파일로 두 번 돌려서 측정한다.
export const loadOptions = {
  stages: [
    { duration: '30s', target: 20 },  // 20 VU까지 점증
    { duration: '1m',  target: 20 },  // 유지
    { duration: '30s', target: 50 },  // 50 VU로 급증 (스레드풀 포화 관찰)
    { duration: '1m',  target: 50 },
    { duration: '30s', target: 0 },   // 감속
  ],
  thresholds: {
    http_req_failed:   ['rate<0.05'],   // 실패율 5% 미만
    http_req_duration: ['p(95)<3000'],  // p95 3초 미만 (After 목표는 더 낮게 조정)
  },
};
