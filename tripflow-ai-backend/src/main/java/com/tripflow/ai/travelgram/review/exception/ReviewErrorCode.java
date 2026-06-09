package com.tripflow.ai.travelgram.review.exception;

import org.springframework.http.HttpStatus;

import com.tripflow.ai.common.global.exception.errorcode.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    // 400
    REVIEW_PHOTO_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 사진 파일이 비어있습니다."),
    REVIEW_PHOTO_REANALYZE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FAILED 상태의 사진만 재분석할 수 있습니다."),

    // 404
    REVIEW_PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사진을 찾을 수 없습니다."),
    REVIEW_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰 포스트를 찾을 수 없습니다."),

    // 500
    REVIEW_PHOTO_S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 업로드에 실패했습니다."),
    REVIEW_PHOTO_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일을 읽는 데 실패했습니다."),
    REVIEW_AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 사진 분석에 실패했습니다."),
    REVIEW_AI_INVALID_SUMMARY(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 결과가 유효하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
