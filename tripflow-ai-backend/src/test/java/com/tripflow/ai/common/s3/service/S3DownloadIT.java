package com.tripflow.ai.common.s3.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.tripflow.ai.common.s3.config.AwsS3Config;

/**
 * 실제 S3에 붙어서 downloadFile이 동작하는지 검증하는 통합 테스트.
 *
 * 단위 테스트(S3ServiceTest)는 AmazonS3를 mock하므로 "키 추출 + 바이트 변환" 로직만 본다.
 * 이 테스트는 실 자격증명(local 프로파일)으로 실 버킷에 붙어
 * 업로드→다운로드 라운드트립으로 실제 연결까지 증명한다.
 *
 * - 외부 연동/네트워크/비용이 있으므로 @Tag("integration")으로 분리.
 *   기본 `./gradlew test`에서는 제외되고, `./gradlew integrationTest`로만 실행된다.
 * - 컨텍스트는 S3 관련 빈 2개만 로드(OpenAI/DB 등은 띄우지 않음).
 */
@Tag("integration")
@ActiveProfiles("local")
@SpringBootTest(classes = { AwsS3Config.class, S3Service.class })
class S3DownloadIT {

    @Autowired
    private S3Service s3Service;

    @Test
    @DisplayName("실 S3 라운드트립: 업로드한 바이트를 downloadFile로 그대로 되받는다")
    void downloadFile_realS3_roundTrip() {
        byte[] original = "tripflow-s3-roundtrip".getBytes(StandardCharsets.UTF_8);
        String storedName = "reviewPhotos/it-" + UUID.randomUUID() + ".jpg";

        String url = s3Service.uploadFile(original, storedName, "image/jpeg");
        try {
            byte[] downloaded = s3Service.downloadFile(url);
            assertArrayEquals(original, downloaded);
        } finally {
            // 테스트 잔여물 정리 (실패해도 로그만 남김)
            s3Service.deleteFile(url);
        }
    }
}
