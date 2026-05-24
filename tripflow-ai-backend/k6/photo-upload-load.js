import http from 'k6/http';
import { check, sleep } from 'k6';
import { loadOptions } from './lib/options.js';

// ====================================================================
// Week 1 — 사진 업로드 → 비동기 AI 분석 경로 부하 테스트 (스레드풀 Before/After)
// 현재 SecurityConfig가 anyRequest().permitAll() → 인증 토큰 불필요.
// 이 파일은 "설정/뼈대"다. 월요일에 TODO 부분을 채워서 사용한다.
// ====================================================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PLAN_ID  = __ENV.PLAN_ID  || '1';   // 존재하는 plan id로 교체

export const options = loadOptions;

// TODO(월): 테스트용 이미지를 k6/fixtures/sample.jpg 에 두고 주석 해제
// const IMG = open('./fixtures/sample.jpg', 'b');

// setup(): 부하 시작 전 1회 실행 → 리뷰 생성으로 photoGroupId 확보
export function setup() {
  const res = http.post(`${BASE_URL}/reviews/create?planId=${PLAN_ID}`);
  check(res, { 'create review 200': (r) => r.status === 200 });
  return { photoGroupId: res.json('photoGroupId') };
}

export default function (data) {
  // TODO(월): 멀티파트 업로드 본문 구성 후 주석 해제
  // const payload = {
  //   photoGroupId: `${data.photoGroupId}`,
  //   startOrderIndex: '0',
  //   files: http.file(IMG, 'sample.jpg', 'image/jpeg'),
  // };
  // const res = http.post(`${BASE_URL}/reviews/photos/upload`, payload);
  // check(res, { 'upload 200': (r) => r.status === 200 });

  sleep(1);
}
