package com.tripflow.ai.travelgram.review.exception.errorcode;

import org.springframework.http.HttpStatus;

import com.tripflow.ai.common.global.exception.errorcode.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    // 400 Bad Request — 요청 자체가 잘못됨
    INVALID_PHOTO_FILE(HttpStatus.BAD_REQUEST, "업로드된 사진 파일이 비어 있습니다."),

    // 404 Not Found — 대상 리소스 없음
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 게시글을 찾을 수 없습니다."),
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "재분석할 사진을 찾을 수 없습니다."),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 계획을 찾을 수 없습니다."),

    // 409 Conflict — 리소스는 있으나 현재 상태가 요청을 허용하지 않음
    PHOTO_NOT_REANALYZABLE(HttpStatus.CONFLICT, "FAILED 상태의 사진만 재분석할 수 있습니다."),

    // 502 Bad Gateway — 외부 의존성(OpenAI) 호출 실패
    TRIP_CONTEXT_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "여행 맥락 분석에 실패했습니다."),
    STYLE_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "리뷰 스타일 생성에 실패했습니다.");

    // 참고: 사진 분석/요약(PHOTO_ANALYSIS)과 멀티파트 바이트 읽기 실패는 HTTP 응답에 붙지 않는다.
    //       @Async 분석은 별도 스레드라 핸들러가 못 받고, 바이트 읽기 실패도 사진은 이미 저장된 상태다.
    //       둘 다 ReviewPhoto.status=FAILED로 기록되고 프론트가 폴링→재분석(S3 원본 재사용)으로 복구한다.

    private final HttpStatus status;
    private final String message;
}
