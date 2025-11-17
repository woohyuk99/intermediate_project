package com.deokhugam.backend.integration.naver;

import com.deokhugam.backend.dto.book.NaverBookDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.util.Objects;

@Component
public class NaverBookClientImpl implements NaverBookClient {

    private final WebClient naverWebClient;
    private final WebClient genericWebClient;
    private final String clientId;
    private final String clientSecret;
    private final boolean downloadThumbnail;

    public NaverBookClientImpl(
            @Qualifier("naverWebClient") WebClient naverWebClient,
            @Qualifier("genericWebClient") WebClient genericWebClient,
            @Value("${app.external.naver.client-id}") String clientId,
            @Value("${app.external.naver.client-secret}") String clientSecret,
            @Value("${app.external.naver.download-thumbnail:true}") boolean downloadThumbnail
    ) {
        this.naverWebClient = Objects.requireNonNull(naverWebClient);
        this.genericWebClient = Objects.requireNonNull(genericWebClient);
        this.clientId = Objects.requireNonNull(clientId);
        this.clientSecret = Objects.requireNonNull(clientSecret);
        this.downloadThumbnail = downloadThumbnail;
    }

    @Override
    public NaverBookDto findByIsbn(String rawIsbn) {
        String isbn = normalizeIsbn(rawIsbn);
        if (isbn == null) {
            throw new IllegalArgumentException("유효하지 않은 ISBN입니다.");
        }

        // 중복 변수 제거: body는 여기서 한 번만 대입
        NaverSearchResponse body = naverWebClient.get()
                .uri(uri -> uri.path("/book.json")
                        .queryParam("query", isbn)
                        .queryParam("display", 1)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(NaverSearchResponse.class)
                .block();

        if (body == null || body.items == null || body.items.length == 0) {
            throw new IllegalStateException("해당 ISBN의 도서가 없습니다.");
        }

        NaverBookItem item = body.items[0];
        String title = stripTags(item.title);
        String author = item.author;
        String description = stripTags(item.description);
        String publisher = item.publisher;
        LocalDate publishedDate = parseDate(item.pubdate);

        byte[] thumbnailImage = null;
        if (downloadThumbnail && item.image != null && !item.image.isBlank()) {
            try {
                thumbnailImage = genericWebClient.get()
                        .uri(item.image)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
            } catch (Exception ignored) {

            }
        }

        return new NaverBookDto(title, author, description, publisher, publishedDate, isbn, thumbnailImage);
    }

    private String normalizeIsbn(String raw) {
        if (raw == null) return null;
        String d = raw.replace("-", "").trim();
        return d.isEmpty() ? null : d;
    }

    private String stripTags(String s) {
        if (s == null) return null;
        return s.replaceAll("<.*?>", "");
    }

    private LocalDate parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.length() != 8) return null;
            int y = Integer.parseInt(yyyymmdd.substring(0, 4));
            int m = Integer.parseInt(yyyymmdd.substring(4, 6));
            int d = Integer.parseInt(yyyymmdd.substring(6, 8));
            return LocalDate.of(y, m, d);
        } catch (Exception e) {
            return null;
        }
    }
}
