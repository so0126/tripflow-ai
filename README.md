# TripFlow AI

서울 장소 데이터를 기반으로 여행 일정을 구성하고, 일정 정보를 활용해 AI 여행 리뷰를 생성하는 여행 관리 서비스입니다.

이 프로젝트는 기존 여행 리뷰 생성 기능이 일정/장소 데이터에 의존해 단독 검증이 어려웠던 문제를 해결하기 위해, 서울 지역 장소 데이터 구축과 리뷰 생성 입력 흐름 개선에 초점을 두고 있습니다.

---

## 1. Project Overview

TripFlow AI는 사용자가 여행 일정을 구성하고, 방문 장소와 일정 정보를 기반으로 AI 여행 리뷰를 생성할 수 있도록 설계된 여행 관리 서비스입니다.

### 주요 목표

- 서울 지역 장소 데이터를 기반으로 일정 생성 테스트 데이터 구성
- 일정/장소 정보를 활용한 AI 여행 리뷰 생성
- 장소 데이터 임베딩을 통한 추천/검색 구조 실험
- 프론트엔드와 백엔드를 분리한 풀스택 구조 구성

---

## 2. Tech Stack

### Backend

- Java 21
- Spring Boot
- Mybatis
- PostgreSQL
- OpenAI API
- Naver Local Search API

### Frontend

- Vue
- Vite
- Axios
- Naver Maps JavaScript API

### Infra / Tools

- Git / GitHub
- Gradle
- Postman
- MySQL Workbench

---

## 3. Main Features

### 여행 일정 생성

- 서울 지역 장소 데이터를 기반으로 일정 생성
- 지역, 카테고리, 일수 조건에 따라 장소 후보 조회
- 일정 생성 결과를 리뷰 생성 기능의 입력 데이터로 활용

### AI 여행 리뷰 생성

- 사용자의 여행 일정, 방문 장소, 감상 키워드를 기반으로 리뷰 생성
- 일정 정보가 부족할 경우를 고려한 입력 데이터 검증
- 생성된 리뷰 저장 및 조회 흐름 구성

### 서울 장소 데이터 구축

- Naver Local Search API를 활용한 서울 장소 데이터 수집
- 장소명, 카테고리, 주소, 지역 정보를 DB에 저장
- 일정 생성 테스트를 위한 seed 데이터 구성

### 장소 임베딩 백필

- 장소명, 카테고리, 지역, 설명을 기반으로 임베딩용 텍스트 생성
- embedding 값이 없는 장소 데이터를 대상으로 벡터 임베딩 생성
- 일반 서버 실행과 분리된 1회성 백필 실행 옵션 구성

---

## 4. Project Structure

```text
root
├── backend
│   ├── planner        # 여행 일정 생성 도메인
│   ├── travelgram     # 여행 리뷰/기록 도메인
│   ├── supporter      # AI 추천/보조 기능 도메인
│   ├── mypage         # 사용자 마이페이지 도메인
│   └── common         # 공통 설정, 예외, 외부 API 연동
│
├── frontend
│   ├── pages
│   ├── components
│   ├── api
│   └── assets
│
└── docs
```

---

## 5. Getting Started

### Backend

```bash
cd backend
./gradlew bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 6. Database Setup

### Seed Data

서울 장소 데이터는 로컬 개발 및 일정 생성 테스트를 위해 seed SQL로 제공합니다.

```bash
mysql -u root -p tripflow < backend/src/main/resources/db/seed/seoul_places_seed.sql
```

### Naver Local Place Backfill

Naver Local Search API를 통해 서울 장소 데이터를 수집하는 백필 기능을 제공합니다.

```bash
./gradlew bootRun --args='--place.backfill.naver=true'
```

### Place Embedding Backfill

embedding 값이 없는 장소 데이터를 대상으로 임베딩을 생성합니다.

```bash
./gradlew bootRun --args='--place.embedding.backfill=true'
```

---

## 7. Improvement Focus

기존 프로젝트에서는 여행 리뷰 생성 기능이 일정/장소 데이터에 의존하고 있어, 일정 생성이 정상적으로 동작하지 않으면 리뷰 생성 기능도 검증하기 어려운 문제가 있었습니다.

이를 해결하기 위해 전체 일정 추천 알고리즘을 고도화하기보다, 리뷰 생성 기능 검증에 필요한 최소 장소 데이터 흐름을 먼저 구축했습니다.

### 개선 방향

- 서울 지역으로 범위를 제한해 장소 데이터 구축 범위 축소
- TourAPI 대신 실제 지역 검색 의도에 가까운 Naver Local Search API 사용
- 장소 데이터를 일정 생성 테스트 데이터로 활용
- 일정/장소 정보를 AI 리뷰 생성 프롬프트에 반영
- 장소 임베딩 백필을 일반 서버 실행과 분리해 운영 안정성 확보

---

## 8. Commit Convention

이 프로젝트는 Conventional Commits 형식을 참고해 커밋 메시지를 작성합니다.

### Format

```text
type(scope): message
```

### Types

| Type | Description |
|---|---|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| refactor | 기능 변경 없는 코드 구조 개선 |
| docs | 문서 수정 |
| chore | 설정, 빌드, 패키지 등 기타 작업 |
| test | 테스트 코드 추가/수정 |
| wip | 작업 중 임시 저장 |

### Scopes

| Scope | Description |
|---|---|
| front | 프론트엔드 변경 |
| back | 백엔드 변경 |
| app | 프론트/백엔드 공통 또는 전체 흐름 변경 |
| readme | README 문서 변경 |

### Examples

```text
feat(back): add Seoul place seed data
feat(back): add Naver local place backfill
feat(back): add place embedding backfill
fix(back): handle missing place data in planner
docs(readme): update project setup guide
wip(back): save current place data changes
```

---

