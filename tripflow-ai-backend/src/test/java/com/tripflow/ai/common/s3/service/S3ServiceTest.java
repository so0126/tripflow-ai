package com.tripflow.ai.common.s3.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

/**
 * S3Service.downloadFile 검증.
 * - fileUrl에서 key를 deleteFile과 동일한 규칙(.com/ 이후)으로 추출하는가
 * - amazonS3.getObject로 받은 내용을 byte[]로 정확히 돌려주는가
 *
 * NOTE: 현재 downloadFile은 미구현(UnsupportedOperationException)이라 이 테스트는 red다.
 */
public class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private static final String BUCKET = "my-bucket";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // @Value로 주입되는 bucket은 단위 테스트에선 비어 있으므로 직접 세팅
        ReflectionTestUtils.setField(s3Service, "bucket", BUCKET);
    }

    @Test
    @DisplayName("fileUrl에서 key를 추출해 S3에서 내용을 byte[]로 다운로드한다")
    public void downloadFile_extractsKeyAndReturnsBytes() {
        // given
        String fileUrl = "https://my-bucket.s3.ap-northeast-2.amazonaws.com/reviewPhotos/abc.jpg";
        String expectedKey = "reviewPhotos/abc.jpg";
        byte[] expectedBytes = "image-binary".getBytes(StandardCharsets.UTF_8);

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(expectedBytes));
        when(amazonS3.getObject(BUCKET, expectedKey)).thenReturn(s3Object);

        // when
        byte[] result = s3Service.downloadFile(fileUrl);

        // then: 내용이 그대로 + 올바른 bucket/key로 조회
        assertArrayEquals(expectedBytes, result);
        verify(amazonS3).getObject(BUCKET, expectedKey);
    }
}
