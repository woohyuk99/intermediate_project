package com.deokhugam.backend.support;

public final class IsbnUtils { // ISBN 처리를 담당하는 유틸 클래스 선언(상속/인스턴스화 방지)
    private IsbnUtils() {} // 외부 인스턴스화를 막기 위한 private 생성자

    public static String normalizeOrNull(String raw) { // ISBN 원문을 정규화하거나 null을 반환하는 정적 메서드 선언
        if (raw == null) return null; // 입력이 null이면 그대로 null 반환
        String digitsOnly = raw.replaceAll("-", "") // 하이픈을 모두 제거
                .trim(); // 앞뒤 공백을 제거
        return digitsOnly.isEmpty() ? null : digitsOnly; // 비어있으면 null, 아니면 정규화된 문자열 반환
    }
}
