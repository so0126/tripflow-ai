# TripFlow AI

## 1. 프로젝트 소개

TripFlow AI는 기존 **AI Travel Manager** 프로젝트를 기반으로, 여행 일정 생성과 Travelgram 리뷰 생성 흐름을 백엔드 관점에서 개선하는 개인 고도화 프로젝트입니다.

### Tech Stack

Vue 3.5, Java 21, Spring Boot 3.4, Spring AI 1.0.0, MyBatis, PostgreSQL, AWS S3, Naver Local Search API

> 기존 프로젝트에는 추가 외부 API와 기능이 포함되어 있으나, 이 README에서는 개인 개선 범위와 직접 관련 있는 기술을 중심으로 정리합니다.

---

## 2. 작업 목표

### 1. 리뷰 입력 데이터 기반 정리

travel_place 테이블을 Naver Local로 채우고 임베딩 백필, hotel_seed 데모로 생성하기, 환경변수 정리
- 임베딩 백필, 스케줄링

### 2. 리뷰 Draft 생성 고도화

중복 Draft 생성 방지

### 3. 사진 비동기 분석 고도화

상태 컬럼 추가, 사진 무한 폴링 해결, 사진 재분석 api, 새로고침 시 사진 불러오기, 비동기 풀 executor 생성

---

## 3. 진행 방식

작업은 Claude Code, Codex 등 AI 협업 도구를 활용해 작은 단위로 진행합니다.

작업 중인 내용은 `docs` 하위 문서에 기록하고, 완료된 내용은 README에 핵심만 반영합니다.  
세부 작업 기록에는 현재 코드 흐름, 변경 파일, 검증 내용, 다음 작업을 남깁니다.

---

## 4. 커밋 메시지 컨벤션

커밋 메시지는 다음 형식을 사용합니다.

```text
type(scope): message
```

### Type

| Type | Description |
| --- | --- |
| feat | 기능 추가 |
| fix | 버그 수정 |
| refactor | 코드 구조 개선 |
| docs | 문서 수정 |
| test | 테스트 추가 또는 수정 |
| chore | 설정, 빌드, 기타 작업 |

### Scope

| Scope | Description |
| --- | --- |
| back | 백엔드 |
| front | 프론트엔드 |
| docs | 문서 |
