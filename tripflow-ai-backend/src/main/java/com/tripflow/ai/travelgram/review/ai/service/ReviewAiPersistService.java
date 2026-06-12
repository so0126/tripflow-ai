package com.tripflow.ai.travelgram.review.ai.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripflow.ai.travelgram.review.ai.agent.AiResult;
import com.tripflow.ai.travelgram.review.ai.dao.AiReviewDao;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewAnalysis;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewHashtag;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewStyle;
import com.tripflow.ai.travelgram.review.ai.dto.response.AiReviewStyleResponse;
import com.tripflow.ai.travelgram.review.ai.dto.response.GeneratedStyleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewAiPersistService {

    private final AiReviewDao aiReviewDao;
    private final ObjectMapper objectMapper;

    /**
     * AI 응답을 DB에 저장하는 쓰기 묶음.
     * analysis → style × N → hashtag × M 순서로 insert하며,
     * 중간에 실패하면 세 테이블 모두 롤백된다.
     */
    @Transactional
    public List<AiReviewStyleResponse> persistStyleResults(
            Long reviewPostId,
            String inputJson,
            AiResult<GeneratedStyleResponse> aiResult) {

        GeneratedStyleResponse aiResponse = aiResult.content();

        String outputJsonString = "";
        try {
            outputJsonString = objectMapper.writeValueAsString(aiResponse);
        } catch (Exception e) {
            log.warn("outputJson 직렬화 실패 (분석 이력 저장은 계속) reviewPostId={}", reviewPostId, e);
        }

        AiReviewAnalysis analysis = AiReviewAnalysis.builder()
                .reviewPostId(reviewPostId)
                .createdAt(OffsetDateTime.now())
                .inputJson(inputJson)
                .outputJson(outputJsonString)
                .build();

        aiReviewDao.insertAiReviewAnalysis(analysis);

        List<AiReviewStyleResponse> resultList = new ArrayList<>();
        for (GeneratedStyleResponse.StyleItem item : aiResponse.getStyles()) {
            String cleanCaption = item.getCaption()
                    .replaceAll("#[\\w가-힣]+", "")
                    .trim();

            AiReviewStyle style = AiReviewStyle.builder()
                    .reviewAnalysisId(analysis.getId())
                    .name(item.getToneName())
                    .toneCode(item.getToneCode())
                    .createdAt(OffsetDateTime.now())
                    .caption(cleanCaption)
                    .build();

            aiReviewDao.insertAiReviewStyle(style);

            List<AiReviewHashtag> savedHashtags = new ArrayList<>();
            for (String tagName : item.getHashtags()) {
                String cleanTagName = tagName.replace("#", "");
                AiReviewHashtag tag = AiReviewHashtag.builder()
                        .reviewStyleId(style.getId())
                        .name(cleanTagName)
                        .createdAt(OffsetDateTime.now())
                        .build();
                aiReviewDao.insertAiReviewHashtag(tag);
                savedHashtags.add(tag);
            }

            resultList.add(new AiReviewStyleResponse(style, savedHashtags));
        }

        return resultList;
    }
}
