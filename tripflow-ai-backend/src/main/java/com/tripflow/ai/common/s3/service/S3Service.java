package com.tripflow.ai.common.s3.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.tripflow.ai.common.global.exception.BusinessException;
import com.tripflow.ai.common.s3.exception.errorcode.StorageErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 바이트 배열을 S3에 업로드한다.
     * S3Service는 멀티파트/스트림 읽기를 모른다 — 입력을 byte[]로 받아 S3 호출만 책임진다.
     * 따라서 여기서 잡는 실패는 S3 호출 실패(AmazonServiceException/SdkClientException)뿐이고,
     * 입력 바이트를 읽다 나는 IOException은 호출자(읽는 쪽)의 책임으로 분리된다.
     */
    public String uploadFile(byte[] bytes, String storedName, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            metadata.setContentType(contentType);

            InputStream inputStream = new ByteArrayInputStream(bytes);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, storedName, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);

            return amazonS3.getUrl(bucket, storedName).toString();
        } catch (AmazonClientException e) {
            log.error("S3 upload failed: {}", storedName, e);
            throw new BusinessException(StorageErrorCode.STORAGE_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String fileUrl) {
    try {
        // fileUrl에서 key(storedName)만 추출하는 로직 필요
        // 예: https://bucket.s3.region.amazonaws.com/reviewPhotos/abc.jpg 
        // -> reviewPhotos/abc.jpg 추출
        String splitStr = ".com/";
        String fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());

        amazonS3.deleteObject(bucket, fileName);
    } catch (Exception e) {
        // 삭제 실패는 로그만 남기고 넘어가는 경우가 많음 (시스템 장애로 번지지 않게)
        log.error("Error deleting file from S3: " + fileUrl, e);
    }
}

    /**
     * S3에 저장된 파일을 바이트 배열로 다운로드한다. (사진 재분석 시 원본 재사용)
     * key 추출은 deleteFile과 동일한 규칙(.com/ 이후)을 재사용한다.
     */
    public byte[] downloadFile(String fileUrl) {
        String splitStr = ".com/";
        String key = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length());

        // try-with-resources로 S3Object(및 내부 스트림)를 반드시 닫는다 (커넥션 누수 방지)
        try (S3Object s3Object = amazonS3.getObject(bucket, key)) {
            return s3Object.getObjectContent().readAllBytes();
        } catch (IOException | AmazonClientException e) {
            log.error("S3 download failed: {}", fileUrl, e);
            throw new BusinessException(StorageErrorCode.STORAGE_DOWNLOAD_FAILED);
        }
    }
}
