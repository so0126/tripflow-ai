import http from 'k6/http';
import { check } from 'k6';
import { smokeOptions } from './lib/options.js';

// k6 ↔ 서버 도달성 확인용 스모크 테스트.
// 실행: k6 run k6/smoke.js   (또는 BASE_URL 지정: k6 run -e BASE_URL=http://localhost:8080 k6/smoke.js)

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 공개 엔드포인트(permitAll). 상태코드는 환경에 따라 다를 수 있으므로
// "전송 자체가 성공했는지(status !== 0)"만 본다. 0 = 연결 실패.
const PATH = __ENV.SMOKE_PATH || '/';

export const options = smokeOptions;

export default function () {
  const res = http.get(`${BASE_URL}${PATH}`);
  check(res, {
    'server reachable (status != 0)': (r) => r.status !== 0,
  });
}
