package com.deokhugam.backend.config; // 설정 패키지

import org.springframework.beans.factory.annotation.Value; // 값 주입 임포트
import org.springframework.context.annotation.Bean; // @Bean 임포트
import org.springframework.context.annotation.Configuration; // @Configuration 임포트
import org.springframework.http.client.reactive.ReactorClientHttpConnector; // Netty 커넥터 임포트
import org.springframework.web.reactive.function.client.ExchangeStrategies; // 코드 변환 전략 임포트
import org.springframework.web.reactive.function.client.WebClient; // WebClient 임포트
import reactor.netty.http.client.HttpClient; // Netty HttpClient 임포트

import java.time.Duration; // Duration 임포트

@Configuration // 구성 클래스
public class ExternalApiConfig { // 외부 API용 WebClient 설정 시작

    @Bean // 공통 HttpClient 빈
    public HttpClient httpClient() { // Netty HttpClient 생성
        return HttpClient.create() // 기본 클라이언트 생성
                .responseTimeout(Duration.ofSeconds(10)); // 응답 타임아웃 10초
    } // httpClient 끝

    @Bean(name = "naverWebClient") // 이름을 지정해 Qualifier로 주입 가능하게 함
    public WebClient naverWebClient( // 네이버 전용 WebClient 빈
                                     HttpClient httpClient, // Netty HttpClient 주입
                                     @Value("${app.external.naver.base-url}") String baseUrl // 베이스 URL 주입
    ) { // 메서드 시작
        return WebClient.builder() // WebClient 빌더 시작
                .baseUrl(baseUrl) // 베이스 URL 적용
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Netty 커넥터 적용
                .exchangeStrategies(ExchangeStrategies.builder() // 메시지 전략 구성
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB 버퍼
                        .build())
                .build(); // WebClient 인스턴스 생성
    } // naverWebClient 끝

    @Bean(name = "ocrWebClient") // OCR 전용 WebClient 이름을 명시적으로 등록
    public WebClient ocrWebClient( // OCR API 호출에 사용할 WebClient 빈 생성
                                   HttpClient httpClient, // 위에서 만든 HttpClient를 주입
                                   @Value("${app.external.ocr.base-url}") String ocrBaseUrl // 설정에서 베이스 URL 주입
    ) {
        return WebClient.builder() // WebClient 빌더 시작
                .baseUrl(ocrBaseUrl) // 베이스 URL 지정(예: https://api.ocr.space)
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Netty 커넥터 적용
                .exchangeStrategies(ExchangeStrategies.builder() // 메시지 전략(버퍼 크기 등) 구성
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 최대 5MB 메모리 버퍼
                        .build())
                .build(); // WebClient 인스턴스 생성
    }

    @Bean(name = "genericWebClient") // 이름 지정
    public WebClient genericWebClient(HttpClient httpClient) { // 절대 URL 요청용 WebClient 빈
        return WebClient.builder() // 빌더 시작
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Netty 커넥터 적용
                .exchangeStrategies(ExchangeStrategies.builder() // 메시지 전략
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5MB 버퍼
                        .build())
                .build(); // WebClient 인스턴스 생성
    } // genericWebClient 끝
} // 클래스 끝
