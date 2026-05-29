package com.tripflow.ai.travelgram.review.ai.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripflow.ai.planner.plan.dao.PlanDao;
import com.tripflow.ai.planner.plan.dao.PlanDayDao;
import com.tripflow.ai.planner.plan.dao.PlanPlaceDao;
import com.tripflow.ai.planner.plan.dto.entity.Plan;
import com.tripflow.ai.planner.plan.dto.entity.PlanDay;
import com.tripflow.ai.planner.plan.dto.entity.PlanPlace;
import com.tripflow.ai.travelgram.review.ai.agent.PlanTitleGenerateAgent;
import com.tripflow.ai.travelgram.review.ai.agent.ReviewStyleGenerateAgent;
import com.tripflow.ai.travelgram.review.ai.assembler.ReviewInputJsonAssembler;
import com.tripflow.ai.travelgram.review.ai.dao.AiReviewDao;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiLog;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiStep;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewAnalysis;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewHashtag;
import com.tripflow.ai.travelgram.review.ai.dto.entity.AiReviewStyle;
import com.tripflow.ai.travelgram.review.ai.dto.response.AiReviewStyleResponse;
import com.tripflow.ai.travelgram.review.ai.dto.response.GeneratedStyleResponse;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;
import com.tripflow.ai.travelgram.review.dao.ReviewPostDao;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPost;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiReviewService {
    private final PlanDao planDao;
    private final PlanDayDao dayDao;
    private final PlanPlaceDao placeDao;

    private final ReviewPhotoDao photoDao;
    private final ReviewInputJsonAssembler builder;

    private final PlanTitleGenerateAgent planTitleGenerateAgent;

    private final AiReviewDao aiReviewDao;

    private final ReviewStyleGenerateAgent styleAgent; // 추가 주입
    private final ReviewPostDao reviewPostDao; // 추가 주입
    private final ObjectMapper objectMapper; // 추가 주입

    public ObjectNode createPlanInputJson(Long planId) {
        // 🟦 1) plan 전체 조회
        Plan plan = planDao.selectPlanById(planId);

        // 🟦 2) days 조회
        List<PlanDay> planDays = dayDao.selectPlanDaysByPlanId(planId);

        // 🟦 3) map<Long, List<PlanPlace>> 형태로 정리
        Map<Long, List<PlanPlace>> placesByDayId = new HashMap<>();

        for (PlanDay day : planDays) {
            List<PlanPlace> places = placeDao.selectPlanPlacesByPlanDayId(day.getId());
            placesByDayId.put(day.getId(), places);
        }

        // 🟦 4) builder 호출해서 JsonNode 생성
        return builder.build(plan, planDays, placesByDayId);

    }


    /**
     * [단건 처리] 
     * 특정 Plan을 조회했을 때, 완료된 여행인데 제목이 없다면 생성 후 업데이트
     */
    @Transactional
    public String ensurePlanTitle(Long planId) {
        Plan plan = planDao.selectPlanById(planId);
        
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found: " + planId);
        }

        // 조건: 여행이 끝났고(isEnded=true) AND 제목이 비어있음
        if (Boolean.TRUE.equals(plan.getIsEnded()) && 
           (plan.getTitle() == null || plan.getTitle().trim().isEmpty())) {
            
            log.info("📢 제목 없는 완료된 여행 발견. 제목 생성 시작 - planId: {}", planId);
            
            // 1. 기존 메서드 재활용하여 AI 제목 생성
            String newTitle = generatePlanTitle(planId);
            
            // 2. 따옴표 등 불필요한 문자 제거 (AI가 가끔 "제목" 형태로 줄 때가 있음)
            newTitle = newTitle.replace("\"", "").trim();

            // 3. DB 업데이트
            planDao.updatePlanTitleById(planId, newTitle);
            
            log.info("✅ 제목 생성 및 업데이트 완료: {}", newTitle);
            return newTitle;
        }

        return plan.getTitle();
    }
    /**
     * [일괄 처리] 
     * DB에 있는 '완료되었지만 제목 없는' 모든 Plan을 찾아서 일괄 업데이트
     * (스케줄러나 관리자 API에서 호출용)
     */
    @Transactional
    public int generateTitlesForMissingOnes() {
        // 1. 대상 조회
        List<Plan> targets = planDao.selectEndedPlansWithNoTitle();
        log.info("🔍 제목 생성 대상 Plan 개수: {}개", targets.size());

        int count = 0;
        for (Plan plan : targets) {
            try {
                // 2. AI 제목 생성
                String newTitle = generatePlanTitle(plan.getId());
                newTitle = newTitle.replace("\"", "").trim();

                // 3. 업데이트
                planDao.updatePlanTitleById(plan.getId(), newTitle);
                count++;
                
                // API Rate Limit 고려하여 약간의 텀을 줄 수도 있음 (선택사항)
                // Thread.sleep(500); 
                
            } catch (Exception e) {
                log.error("❌ planId={} 제목 생성 중 실패: {}", plan.getId(), e.getMessage());
                // 하나가 실패해도 나머지는 계속 진행
            }
        }
        
        log.info("🎉 총 {}개의 Plan 제목 업데이트 완료", count);
        return count;
    }
    public String generatePlanTitle(Long planId) {
        ObjectNode inputJson = createPlanInputJson(planId);

        // LLM에게 보내기 쉽게 String으로 변환
        String inputJsonString = inputJson.toPrettyString();
        // Title을 agent 통해 생성
        String title = planTitleGenerateAgent.generatePlanTitle(inputJsonString);

        return title;
    }

    /**
     * AI 리뷰 스타일 생성 및 저장 (메인 로직)
     */
    @Transactional
    public List<AiReviewStyleResponse> createAndSaveStyles(Long planId, Long reviewPostId) {
        long startedAt = System.nanoTime();
        try {
            // 0. 멱등성 가드: 이미 생성된 분석이 있으면 AI 재호출 없이 기존 결과 반환
            AiReviewAnalysis existing = aiReviewDao.selectLatestAnalysisByReviewPostId(reviewPostId);
            if (existing != null) {
                List<AiReviewStyle> existingStyles = aiReviewDao.selectAllStylesByAnalysisId(existing.getId());
                List<AiReviewStyleResponse> resultList = new ArrayList<>();
                for (AiReviewStyle s : existingStyles) {
                    List<AiReviewHashtag> tags = aiReviewDao.selectHashtagsByStyleId(s.getId());
                    resultList.add(new AiReviewStyleResponse(s, tags));
                }
                long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
                log.info("{}", ReviewAiLog.success(
                        ReviewAiStep.STYLE_GENERATION,
                        planId,
                        reviewPostId,
                        null,
                        null,
                        existing.getId(),
                        elapsedMs));
                return resultList;
            }

            // 1. 여행 데이터 JSON 생성 (기존 Builder 활용)
            ObjectNode inputNode = createPlanInputJson(planId);
            String inputJson = inputNode.toPrettyString();

            // 2. ReviewPost에서 Mood, Type 조회
            // (ReviewPostDao에 selectById가 있다고 가정하거나 추가 필요)
            ReviewPost post = reviewPostDao.selectReviewPostById(reviewPostId);
            if (post == null)
                throw new IllegalArgumentException("Review Post not found");

            String mood = post.getOverallMoods();
            String type = post.getTravelType();

            // 3. Agent 호출 (AI 생성)
            GeneratedStyleResponse aiResponse = styleAgent.generateStyles(inputJson, mood, type);

            // 4. 분석 이력 저장 (AiReviewAnalysis)
            // output_json은 나중에 디버깅용으로 AI 전체 응답을 저장
            String outputJsonString = "";
            try {
                outputJsonString = objectMapper.writeValueAsString(aiResponse);
            } catch (Exception e) {
            }

            AiReviewAnalysis analysis = AiReviewAnalysis.builder()
                    .reviewPostId(reviewPostId)
                    .createdAt(OffsetDateTime.now())
                    .inputJson(inputJson)
                    .outputJson(outputJsonString)
                    .build();

            aiReviewDao.insertAiReviewAnalysis(analysis); // id 생성됨

            List<AiReviewStyleResponse> resultList = new ArrayList<>();
            // 5. Save Styles & Hashtags
            for (GeneratedStyleResponse.StyleItem item : aiResponse.getStyles()) {
                // 💡 [추가] 캡션 문자열 내에 있는 해시태그(#단어) 제거 로직
                // #으로 시작하고 공백 전까지 이어지는 단어들을 모두 빈 문자열로 치환
                String cleanCaption = item.getCaption()
                        .replaceAll("#[\\w가-힣]+", "") // 해시태그 패턴 제거
                        .trim();
                // 5-1. Save Style
                AiReviewStyle style = AiReviewStyle.builder()
                        .reviewAnalysisId(analysis.getId())
                        .name(item.getToneName())
                        .toneCode(item.getToneCode())
                        .createdAt(OffsetDateTime.now())
                        .caption(cleanCaption) // Make sure this matches your DB column
                        .build();

                aiReviewDao.insertAiReviewStyle(style);

                // 5-2. Save Hashtags
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

                // 5-3. Add to Result List
                resultList.add(new AiReviewStyleResponse(style, savedHashtags));
            }

            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{}", ReviewAiLog.success(
                    ReviewAiStep.STYLE_GENERATION,
                    planId,
                    reviewPostId,
                    null,
                    null,
                    analysis.getId(),
                    elapsedMs));
            return resultList;
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("{}", ReviewAiLog.fail(
                    ReviewAiStep.STYLE_GENERATION,
                    planId,
                    reviewPostId,
                    null,
                    null,
                    elapsedMs,
                    e), e);
            throw new RuntimeException(e);
        }
    }
}
