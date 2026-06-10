package com.tripflow.ai.common.s3.exception.errorcode;

import org.springframework.http.HttpStatus;

import com.tripflow.ai.common.global.exception.errorcode.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements BaseErrorCode {

    // 502 Bad Gateway — 외부 스토리지(S3) 호출 실패
    STORAGE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "파일 업로드에 실패했습니다."),
    STORAGE_DOWNLOAD_FAILED(HttpStatus.BAD_GATEWAY, "파일 다운로드에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
