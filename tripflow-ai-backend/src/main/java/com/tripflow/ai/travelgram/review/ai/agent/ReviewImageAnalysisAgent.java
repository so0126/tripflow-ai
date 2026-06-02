package com.tripflow.ai.travelgram.review.ai.agent;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import com.tripflow.ai.travelgram.review.dto.response.PhotoAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReviewImageAnalysisAgent {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  public ReviewImageAnalysisAgent(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
    this.chatClient = chatClientBuilder.build();
    this.objectMapper = objectMapper;
  }

  // ======================================================
  // 1단계: 개별 사진 요약 (Vision AI)
  // ======================================================
  public String analyzeReviewImage(String contentType, byte[] bytes) {
    // 1. 시스템 프롬프트: 여행스타그램 리뷰어 페르소나 부여
    SystemMessage systemMessage = new SystemMessage(
        """
            목표: 사용자 사진을 분석해 정확한 사실 기반 한국어 한 문장 요약을 생성한다.

              ### 작업 내용
              - 명확히 보이는 사람 수도 문장에 포함한다.
              - 사진에 명확히 드러나지 않는 정보는 절대 추측하지 않는다.
              - 명확히 보이는 물체, 사람의 행동, 풍경을 묘사한다.
              - 감정·의도·관계 추론 금지

              ### 출력 양식
              한국어로 된 한 문장
            }
                          """);

    // 2. 미디어(이미지) 객체 생성
    Resource resource = new ByteArrayResource(bytes);
    Media media = Media.builder()
        .mimeType(MimeType.valueOf(contentType))
        .data(resource)
        .build();

    // 3. 사용자 메시지 (이미지 포함)
    UserMessage userMessage = UserMessage.builder()
        .text("사용자의 사진입니다.")
        .media(media)
        .build();

    // 4. LLM 호출
    // 실패(외부 API 오류 등)는 삼키지 않고 호출자(service)로 전파 → service가 status=FAILED로 기록
    return chatClient.prompt()
        .messages(systemMessage, userMessage)
        .call()
        .content();
  }

  // ======================================================
  // 2단계: 전체 여행 분석 (Text AI)
  // ======================================================
  public PhotoAnalysisResult analyzeTripContext(List<String> summaries) {
    // 리스트를 하나의 문자열로 합침
    String combinedSummaries = String.join("\n- ", summaries);

    SystemMessage systemMessage = new SystemMessage(
        """
            목표: 사용자 메세지에 제공되는 사진 요약 리스트를 기반으로 overallMood와 travelType을 작성해야 한다.

            ## 판단 규칙
            1. travelType 결정
               - 여러 사람이 언급되거나 복수 표현(예: "둘", "함께")이 반복되면 GROUP.
               - 대부분 풍경이거나 한 명만 언급되면 SOLO.
               - 정보가 모호하거나 서로 충돌하면 UNCLEAR.

            2. overallMood 결정
               - 전체 사진이 주는 분위기를 한국어 한 문장으로 표현합니다.
               - 예: "조용하고 여유로운 자연 감성", "활기 넘치는 도시 산책 분위기"

            ## 출력 양식
            - overallMood는 사진 전체 분위기의 요약을 한국어로 작성합니다.
            - travelType은 영어 (SOLO, GROUP, UNCLEAR) ENUM 값으로만 출력합니다.
            - JSON으로 출력합니다
            {
            "overallMood":"string",
            "travelType":"SOLO|GROUP|UNCLEAR"
            }
            """);

    UserMessage userMessage = new UserMessage(
        combinedSummaries);

    // 1. LLM에게 응답 받기 (아직은 String 상태)
    // 실패(외부 API 오류·JSON 파싱 실패)는 삼키지 않고 호출자(service)로 전파한다.
    String jsonResponse = chatClient.prompt()
        .messages(systemMessage, userMessage)
        .call()
        .content();

    // 2. [중요] 마크다운 코드 블록 제거 (```json ... ```)
    // LLM이 친절하게 코드 블록을 씌워줄 때가 있는데, 파싱 에러나니 벗겨야 함
    if (jsonResponse.startsWith("```")) {
      jsonResponse = jsonResponse.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "");
    }

    // 3. ObjectMapper로 String -> Object 변환 (핵심!)
    // readValue(JSON문자열, 변환할클래스.class)
    try {
      return objectMapper.readValue(jsonResponse, PhotoAnalysisResult.class);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Trip context analysis JSON 파싱 실패", e);
    }
  }
}
