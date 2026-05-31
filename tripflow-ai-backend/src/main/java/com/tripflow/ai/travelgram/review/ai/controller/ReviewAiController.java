package com.tripflow.ai.travelgram.review.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tripflow.ai.travelgram.review.ai.agent.TrendSearchAgent;
import com.tripflow.ai.travelgram.review.ai.service.ReviewAiService;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/review")
public class ReviewAiController {

    private final TrendSearchAgent trendSearchAgent;
    private final ReviewAiService reviewAiService;

    @GetMapping("/trend")
    public String getTrend(@RequestParam("keyword") String keyword) {
        return trendSearchAgent.generateTrend(keyword);
    }

    @GetMapping("/{planId}/input")
    public ResponseEntity<ObjectNode> getInput(@PathVariable("planId") Long planId) {
        ObjectNode json = reviewAiService.createPlanInputJson(planId);
        return ResponseEntity.ok(json);
    }

    @GetMapping("{planId}/title")
    public ResponseEntity<String> getPlanTitle(@PathVariable("planId") Long planId) {
        String title = reviewAiService.ensurePlanTitle(planId);
        return ResponseEntity.ok(title);
    }

    // 관리자용 일괄 제목 업데이트 API
    @PostMapping("/batch/generate-titles")
    public ResponseEntity<String> batchGenerateTitles() {
        int count = reviewAiService.generateTitlesForMissingOnes();
        return ResponseEntity.ok(count + "개의 여행 계획 제목이 생성되었습니다.");
    }

    @PostMapping("/generate-styles")
    public ResponseEntity<?> generateStyles(
        @RequestParam("planId") Long planId,
        @RequestParam("reviewPostId") Long reviewPostId) {

        // 생성 및 저장 수행
        var styles = reviewAiService.createAndSaveStyles(planId, reviewPostId);

        return ResponseEntity.ok(styles);
    }

}
