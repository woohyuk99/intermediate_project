package com.deokhugam.backend.integration.ocr; // OCR 연동 구현 패키지

import org.springframework.beans.factory.annotation.Qualifier; // 특정 이름의 빈 주입을 위한 임포트
import org.springframework.beans.factory.annotation.Value; // 설정값 주입 임포트
import org.springframework.http.MediaType; // 미디어 타입 지정 임포트
import org.springframework.stereotype.Component; // 스프링 컴포넌트 임포트
import org.springframework.util.LinkedMultiValueMap; // x-www-form-urlencoded 폼 데이터 맵
import org.springframework.util.MultiValueMap; // 멀티밸류맵 타입
import org.springframework.web.reactive.function.BodyInserters; // 바디 인서터
import org.springframework.web.reactive.function.client.WebClient; // WebClient
import org.springframework.web.multipart.MultipartFile; // 업로드 파일 타입

import java.util.Base64; // base64 인코딩 유틸

@Component // 스프링 빈으로 등록
public class OcrSpaceClient implements OcrClient { // OCR.space 클라이언트 구현 클래스 시작

    private final WebClient ocrWebClient; // OCR API 호출용 WebClient
    private final String apiKey; // OCR.space API 키
    private final String language; // 인식 언어 (예: "kor", "eng", "kor,eng")
    private final int ocrEngine; // OCR 엔진 버전(1 또는 2)

    public OcrSpaceClient( // 생성자
                           @Qualifier("ocrWebClient") WebClient ocrWebClient, // ExternalApiConfig에서 만든 ocrWebClient 빈
                           @Value("${app.external.ocr.api-key}") String apiKey, // application.yml에서 주입
                           @Value("${app.external.ocr.language:kor}") String language, // 기본 kor
                           @Value("${app.external.ocr.ocr-engine:2}") int ocrEngine // 기본 2
    ) {
        this.ocrWebClient = ocrWebClient; // 필드에 대입
        this.apiKey = apiKey; // 필드에 대입
        this.language = language; // 필드에 대입
        this.ocrEngine = ocrEngine; // 필드에 대입
    }

    @Override // 인터페이스 구현
    public String recognizeText(MultipartFile image) { // 업로드 이미지에서 텍스트를 OCR로 인식
        if (image == null || image.isEmpty()) { // 파일 유효성 검사
            throw new IllegalArgumentException("이미지 파일은 필수입니다."); // 400 범주 예외 유도
        }

        final byte[] bytes; // 이미지 바이트를 담을 변수
        try {
            bytes = image.getBytes(); // MultipartFile에서 바이트 추출
        } catch (Exception e) {
            throw new IllegalStateException("이미지 바이트를 읽는 중 오류가 발생했습니다.", e); // 내부 오류로 처리
        }

        // 멀티파트 전송 대신 base64 전송으로 변경
        // data URI 스킴을 사용하면 OCR.space 호환성/안정성이 좋음
        String mime = (image.getContentType() != null) ? image.getContentType() : "application/octet-stream"; // MIME 추정
        String base64 = Base64.getEncoder().encodeToString(bytes); // 바이트 → base64 문자열
        String dataUri = "data:" + mime + ";base64," + base64; // data URI 조합

        // application/x-www-form-urlencoded 폼 구성
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>(); // 키-값 폼 데이터
        form.add("apikey", apiKey); // API 키
        form.add("language", language); // 언어(예: "kor" 또는 "kor,eng")
        form.add("isOverlayRequired", "false"); // 오버레이 필요 없음
        form.add("OCREngine", Integer.toString(ocrEngine)); // 엔진 버전
        form.add("base64Image", dataUri); // 인코딩된 이미지 본문

        // OCR.space 호출 (POST /parse/image)
        OcrSpaceResponse response = ocrWebClient.post() // POST 요청 시작
                .uri("/parse/image") // 엔드포인트
                .contentType(MediaType.APPLICATION_FORM_URLENCODED) // x-www-form-urlencoded
                .body(BodyInserters.fromFormData(form)) // 폼 데이터 바디로 삽입
                .retrieve() // 응답 수신
                .bodyToMono(OcrSpaceResponse.class) // 응답 바디를 DTO로 역직렬화
                .block(); // 동기 블록 (서비스 계층에서 동기 사용 방식 유지)

        // 응답 검증
        if (response == null) { // 응답 자체가 null
            throw new IllegalStateException("OCR 서비스 응답이 없습니다."); // 서버 내부 처리 예외
        }
        if (response.IsErroredOnProcessing) { // 제공자 측 처리 오류
            String msg = response.errorMessageAsString(); // 사람이 읽기 쉬운 에러 메시지로 변환
            if (msg == null || msg.isBlank()) msg = "알 수 없는 오류"; // 기본 메시지 보정
            // 이 예외는 상위 계층(GlobalExceptionHandler 등)에서 5xx 계열로 매핑하는 것을 권장
            throw new IllegalStateException("OCR 서비스 오류: " + msg);
        }
        if (response.ParsedResults == null || response.ParsedResults.length == 0) { // 파싱 결과 없음
            throw new IllegalStateException("OCR 결과가 비어 있습니다."); // 후속 단계에서 400 변환 가능
        }

        // ParsedResults들의 텍스트를 합쳐서 단일 문자열로 반환
        StringBuilder combined = new StringBuilder(); // 문자열 누적
        for (OcrSpaceResponse.ParsedResult pr : response.ParsedResults) { // 각 결과 순회
            if (pr != null && pr.ParsedText != null) { // null 가드
                combined.append(pr.ParsedText).append('\n'); // 한 줄 추가
            }
        }
        return combined.toString().trim(); // 공백 정리 후 반환
    }
}
