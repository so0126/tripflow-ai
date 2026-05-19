# TripFlow AI

서울 장소 데이터를 기반으로 나들이 일정을 생성하고, 해당 일정 정보를 활용해 AI 리뷰 생성 기능을 고도화하기 위한 백엔드 중심 프로젝트입니다.

기존 프로젝트는 TourAPI 기반으로 장소 데이터를 활용해 일정을 생성하는 구조였으나, 개인 고도화 과정에서는 서울 지역 장소 데이터를 더 유연하게 확보하고 리뷰 생성 기능의 입력 데이터로 활용하기 위해 **Naver Local Search API 기반 장소 데이터 수집 구조**로 전환했습니다.

현재 개선의 핵심은 새로운 기능을 많이 추가하는 것이 아니라, **AI 리뷰 생성 기능을 고도화하기 위한 장소 데이터 흐름을 안정화하는 것**입니다.

---

## 1. Project Goal

TripFlow AI의 개인 고도화 목표는 다음과 같습니다.

```text
서울 장소 데이터 수집
        ↓
장소 데이터 저장 및 정리
        ↓
장소 임베딩 백필
        ↓
기존 일정 생성 흐름에 활용
        ↓
일정 기반 AI 리뷰 생성 고도화
```

리뷰 생성 기능은 일정과 장소 데이터가 있어야 의미 있는 결과를 만들 수 있습니다.  
따라서 본 프로젝트에서는 리뷰 생성 기능을 바로 수정하기 전에, 먼저 리뷰 생성의 기반이 되는 장소 데이터 흐름을 정리했습니다.

---

## 2. Current Improvement Focus

현재 개인 고도화에서 집중하고 있는 부분은 다음과 같습니다.

### 2-1. TourAPI 기반 장소 데이터 흐름 개선

기존 일정 생성 흐름은 TourAPI 기반 장소 데이터를 활용하는 구조였습니다.

하지만 리뷰 생성 기능을 고도화하려면 장소명, 카테고리, 주소, 지역 정보 등 리뷰 프롬프트에 활용할 수 있는 장소 데이터가 안정적으로 필요했습니다.

이를 위해 장소 데이터 수집 방식을 **Naver Local Search API 기반 구조**로 변경했습니다.

### 개선 목표

- 서울 지역 장소 데이터를 더 유연하게 수집
- 일정 생성과 리뷰 생성에서 사용할 수 있는 장소 데이터 확보
- 장소명, 카테고리, 주소 등 리뷰 생성에 필요한 데이터 정리
- 외부 API 기반 장소 수집 결과를 DB에 저장할 수 있는 구조 구성

---

### 2-2. Naver Local Search 기반 장소 데이터 수집

Naver Local Search API를 활용해 서울 지역 장소 데이터를 수집하고, 일정 생성 및 리뷰 생성 흐름에서 사용할 수 있도록 저장합니다.

### 주요 작업

- Naver Local Search API 기반 장소 수집 로직 추가
- 서울 지역 장소 데이터 저장 구조 정리
- 장소명, 카테고리, 주소, 좌표 등 장소 기본 정보 저장
- 중복 장소 저장을 방지하기 위한 기준 정리
- 로컬 개발 및 테스트용 seed 데이터 구성

### 실행 예시

```bash
cd tripflow-ai-backend
./gradlew bootRun --args='--place.backfill.naver=true'
```

---

### 2-3. 장소 임베딩 백필

장소 데이터를 향후 검색, 추천, 리뷰 생성 품질 개선에 활용할 수 있도록 임베딩 백필 구조를 추가했습니다.

임베딩 백필은 모든 서버 실행 시 자동으로 수행되는 작업이 아니라, 필요할 때 별도로 실행하는 일회성 작업으로 분리했습니다.

### 주요 작업

- embedding 값이 없는 장소 데이터 조회
- 장소명, 카테고리, 주소, 설명 정보를 기반으로 임베딩 텍스트 생성
- OpenAI Embedding API를 활용한 임베딩 생성
- 생성된 embedding 값을 DB에 저장
- 일반 서버 실행과 백필 작업 분리

### 실행 예시

```bash
cd tripflow-ai-backend
./gradlew bootRun --args='--place.embedding.backfill=true'
```

---

## 3. Why This Matters

AI 리뷰 생성 기능은 단순히 OpenAI API를 호출하는 것만으로는 충분하지 않습니다.

리뷰가 자연스럽게 생성되려면 다음 데이터가 필요합니다.

- 사용자가 어떤 장소를 방문했는지
- 장소가 어떤 카테고리인지
- 장소가 어느 지역에 있는지
- 일정 안에서 장소들이 어떤 흐름으로 구성되어 있는지
- 사용자의 감상 키워드와 장소 정보가 어떻게 연결되는지

따라서 현재 개선 작업은 리뷰 생성 기능을 직접 수정하기 전 단계로, **리뷰 생성에 필요한 입력 데이터의 품질을 높이는 작업**입니다.

---

## 4. Review Generation Improvement Plan

리뷰 생성 기능은 아직 본격적으로 고도화하지 않았으며, 다음 단계의 핵심 개선 대상입니다.

앞으로는 현재 정리한 장소 데이터와 기존 일정 생성 결과를 기반으로, AI 리뷰 생성 기능을 다음 방향으로 개선할 예정입니다.

### 4-1. 리뷰 생성 입력 데이터 정리

리뷰 생성 요청 시 일정 ID만 받는 것이 아니라, 일정에 포함된 장소 정보를 함께 조회해 프롬프트에 반영할 수 있도록 개선합니다.

### 개선 방향

- 일정 ID 기준으로 일정 상세 정보 조회
- 일정에 포함된 장소 목록 조회
- 장소명, 카테고리, 지역 정보를 리뷰 생성 입력값으로 구성
- 감상 키워드와 장소 정보를 함께 프롬프트에 반영

---

### 4-2. 프롬프트 템플릿 개선

기존 리뷰 생성 로직이 단순한 요청 문자열에 가깝다면, 앞으로는 일정과 장소 데이터를 구조화해 프롬프트를 구성할 예정입니다.

### 개선 방향

- 일정 요약 정보 포함
- 방문 장소 목록 포함
- 장소 카테고리 반영
- 사용자의 감상 키워드 반영
- 리뷰 톤과 길이 조정 옵션 추가 검토

---

### 4-3. 리뷰 생성 예외 처리 강화

리뷰 생성 기능은 외부 AI API에 의존하기 때문에 실패 가능성을 고려해야 합니다.

### 개선 방향

- 존재하지 않는 일정 ID 요청 처리
- 일정에 장소가 없는 경우 예외 처리
- AI 응답 실패 시 예외 처리
- 리뷰 저장 실패 시 처리 흐름 정리
- 사용자에게 실패 원인을 전달할 수 있는 응답 구조 개선

---

### 4-4. 리뷰 생성 성능 테스트

AI 리뷰 생성은 외부 API 호출이 포함되기 때문에 일반 CRUD API보다 응답 시간이 길어질 수 있습니다.

향후 리뷰 생성 기능을 개선하면서 다음 항목을 중심으로 성능을 측정할 예정입니다.

### 측정 대상

- 일정 상세 조회 시간
- 장소 목록 조회 시간
- 프롬프트 생성 시간
- OpenAI API 호출 시간
- 리뷰 저장 시간
- 전체 리뷰 생성 요청 처리 시간

---

## 5. Main Backend Flow

```text
Naver Local Search API
        ↓
Place Backfill Runner
        ↓
Place DB
        ↓
Place Embedding Backfill
        ↓
Planner
        ↓
Travelgram Review Generation
```

현재까지는 장소 데이터 수집과 임베딩 백필을 중심으로 개선했으며, 이후 단계에서 Travelgram 리뷰 생성 기능을 본격적으로 개선할 예정입니다.

---

## 6. Tech Stack

### Backend

- Java 21
- Spring Boot
- MyBatis
- MySQL
- OpenAI API
- Naver Local Search API
- Gradle

### Frontend

- Vue
- Vite
- Axios
- Naver Maps JavaScript API

---

## 7. Project Structure

```text
tripflow-ai
├── tripflow-ai-backend
│   ├── planner        # 일정 생성 도메인
│   ├── travelgram     # 리뷰 생성 및 여행 기록 도메인
│   ├── place          # 장소 데이터 수집 및 임베딩 도메인
│   ├── mypage         # 사용자 마이페이지 도메인
│   └── common         # 공통 설정 및 예외 처리
│
├── tripflow-ai-frontend
│   ├── pages
│   ├── components
│   ├── api
│   └── assets
│
└── README.md
```

---

## 8. Getting Started

### Backend 실행

```bash
cd tripflow-ai-backend
./gradlew bootRun
```

### Frontend 실행

```bash
cd tripflow-ai-frontend
npm install
npm run dev
```

---

## 9. Environment Variables

외부 API 연동을 위해 다음 환경 변수가 필요합니다.

```env
OPENAI_API_KEY=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
NAVER_MAP_CLIENT_ID=
```

API Key는 GitHub에 커밋하지 않고, 로컬 환경 변수 또는 IDE 실행 설정을 통해 주입합니다.

---

## 10. Database Setup

### Seed Data 적용

```bash
mysql -u root -p tripflow < tripflow-ai-backend/src/main/resources/db/seed/seoul_places_seed.sql
```

### Naver 장소 데이터 백필

```bash
cd tripflow-ai-backend
./gradlew bootRun --args='--place.backfill.naver=true'
```

### 장소 임베딩 백필

```bash
cd tripflow-ai-backend
./gradlew bootRun --args='--place.embedding.backfill=true'
```

---

## 11. Improvement Summary

현재까지의 개인 고도화 작업은 다음과 같습니다.

| 구분 | 내용 |
| --- | --- |
| 장소 데이터 수집 | TourAPI 중심 흐름에서 Naver Local Search 기반 장소 수집 구조로 변경 |
| 장소 데이터 저장 | 서울 지역 장소 데이터를 일정 생성 및 리뷰 생성에 활용할 수 있도록 저장 |
| Seed 데이터 | 로컬 개발과 테스트를 위한 서울 장소 seed 데이터 구성 |
| 중복 처리 | 동일 장소가 반복 저장되지 않도록 기준 정리 |
| 임베딩 백필 | 장소 데이터를 기반으로 embedding 값을 생성하는 백필 구조 추가 |
| 실행 분리 | 장소 수집과 임베딩 생성을 일반 서버 실행과 분리 |

---

## 12. Next Steps

다음 개선 단계는 Travelgram 리뷰 생성 기능입니다.

### 예정 작업

- 일정 ID 기반 리뷰 생성 입력 데이터 구성
- 일정에 포함된 장소 목록을 프롬프트에 반영
- 장소 카테고리와 지역 정보를 활용한 리뷰 품질 개선
- 리뷰 생성 요청 검증 로직 추가
- AI 응답 실패 예외 처리
- 리뷰 저장 흐름 정리
- 리뷰 생성 API 성능 측정
- 필요 시 비동기 처리 구조 검토

---

## 13. Commit Convention

커밋 메시지는 다음 형식을 사용합니다.

```text
type(scope): message
```

### Types

| Type | Description |
| --- | --- |
| feat | 기능 추가 |
| fix | 버그 수정 |
| refactor | 코드 구조 개선 |
| docs | 문서 수정 |
| test | 테스트 추가/수정 |
| chore | 설정 및 기타 작업 |

### Examples

```bash
feat(back): Naver Local Search 기반 장소 백필 추가
feat(back): 장소 임베딩 백필 기능 추가
fix(back): 장소 저장 시 중복 저장 방지
refactor(back): 장소 import 흐름 단순화
docs(readme): README 개선 목표 수정
test(back): 리뷰 생성 성능 테스트 추가
```

---

## 14. Portfolio Focus

이 프로젝트에서 강조하고 싶은 백엔드 개선 포인트는 다음과 같습니다.

- 기존 외부 공공 API 기반 장소 흐름을 Naver Local Search 기반으로 변경한 경험
- 리뷰 생성 기능을 고도화하기 위해 필요한 장소 데이터 흐름을 먼저 정리한 경험
- 외부 API를 활용한 데이터 수집과 DB 저장 흐름 구현
- 장소 데이터 임베딩 백필 구조 추가
- 일반 서버 실행과 백필성 작업을 분리한 경험
- 향후 AI 리뷰 생성 기능의 입력 검증, 예외 처리, 성능 테스트로 확장 가능한 구조 마련

본 프로젝트는 기능 수를 늘리는 것보다, **AI 리뷰 생성 기능을 백엔드 관점에서 더 깊게 개선하기 위한 데이터 기반 작업**에 초점을 맞추고 있습니다.
