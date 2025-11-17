package com.deokhugam.backend.integration.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrSpaceResponse {
    public boolean IsErroredOnProcessing;

    // 문자열 단일/배열 모두 List<String> 으로 받기
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> ErrorMessage;

    public ParsedResult[] ParsedResults;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedResult {
        public String ParsedText;
    }

    // 메시지 헬퍼
    public String errorMessageAsString() {
        if (ErrorMessage == null || ErrorMessage.isEmpty()) return null;
        return String.join(" | ", ErrorMessage);
    }
}
