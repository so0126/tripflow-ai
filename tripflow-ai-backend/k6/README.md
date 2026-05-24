# k6 부하 테스트

스레드풀/배치 등 견고성 작업의 **Before/After 측정**용. k6는 Go로 만든 단독 바이너리이고 스크립트는 JS다(npm 불필요).

## 설치 (Windows)

```powershell
winget install k6 --source winget
# 또는: choco install k6
# 또는 도커: docker run --rm -i grafana/k6 run - <k6/smoke.js
```

설치 확인: `k6 version`

## 실행

```powershell
# 1) 스모크: k6 ↔ 서버 연결 확인 (서버를 먼저 띄운 뒤)
k6 run k6/smoke.js

# 2) BASE_URL 지정
k6 run -e BASE_URL=http://localhost:8080 k6/smoke.js

# 3) Week 1 부하 (월요일 TODO 채운 뒤)
k6 run -e PLAN_ID=1 k6/photo-upload-load.js
```

## 파일

| 파일 | 용도 |
|------|------|
| `lib/options.js` | 공통 옵션(스테이지·thresholds) 프리셋 |
| `smoke.js` | 연결 확인용 (바로 실행 가능) |
| `photo-upload-load.js` | Week 1 사진 업로드 부하 — **뼈대**, 월요일에 TODO 채움 |
| `fixtures/` | 테스트용 이미지 등 (직접 추가) |

## 측정 포인트 (Week 1)

같은 프로파일로 **리팩토링 전/후** 각각 실행해 비교:
- `http_req_duration` p95 / max
- `http_req_failed` 실패율
- 동시 부하 중 서버 스레드 수 (앱 로그 / JVM 스레드 덤프)

> 현재 모든 엔드포인트가 `permitAll`이라 인증 토큰은 필요 없다.
