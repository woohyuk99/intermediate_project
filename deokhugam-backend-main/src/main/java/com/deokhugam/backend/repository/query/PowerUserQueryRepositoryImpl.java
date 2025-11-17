package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePowerUserDto;
import com.deokhugam.backend.dto.dashboard.PowerUserDto;
import com.deokhugam.backend.dto.dashboard.PowerUserQuery;
import com.deokhugam.backend.entity.PowerUser;
import com.deokhugam.backend.mapper.PowerUserMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import static com.deokhugam.backend.entity.QPowerUser.powerUser;

@Repository
@RequiredArgsConstructor
public class PowerUserQueryRepositoryImpl implements PowerUserQueryRepository {

    private final JPAQueryFactory queryFactory; // QueryDSL JPA 쿼리 팩토리 의존성 보관
    private final PowerUserMapper powerUserMapper; // 엔티티↔DTO 매핑을 위한 매퍼 의존성 보관

    @Override
    public CursorPageResponsePowerUserDto findByPowerUserIdWithCursor(PowerUserQuery query) {
        OrderSpecifier<?>[] order = "desc".equalsIgnoreCase(query.direction())
                ? new OrderSpecifier[]{powerUser.score.desc(), powerUser.createdAt.desc(), powerUser.id.desc()}
                : new OrderSpecifier[]{powerUser.score.asc(), powerUser.createdAt.asc(), powerUser.id.asc()};

        List<PowerUser> rows = queryFactory // 실제 데이터 조회 시작
                .selectFrom(powerUser) // books 테이블을 선택
                .where(powerUser.period.eq(query.period()),
                        buildWhere(query)) // 위에서 만든 동적 조건 적용
                .orderBy(order) // 정렬 지정자 적용
                .limit(query.limit() + 1L) // 다음 페이지 여부 판단을 위해 1건 더 조회
                .fetch(); // 리스트로 결과 수집

        boolean hasNext = rows.size() > query.limit(); // 조회 결과가 size보다 크면 다음 페이지가 존재
        if (hasNext) {
            rows.remove(rows.size() - 1);
        } // 마지막 1건은 미리보기 용이므로 제거

        Long totalCount = queryFactory // 총 개수 조회(커서 조건 제외, 검색/삭제 조건만 반영)
                .select(powerUser.count()) // count(*) 선택
                .from(powerUser)
                .where(powerUser.period.eq(query.period()))
                .fetchOne(); // 단일 결과 수신

        long totalElements = (totalCount != null) ? totalCount : 0L; // 널 가드 후 기본값 처리

        List<PowerUserDto> content = rows.stream() // 엔티티 리스트를 스트림으로 변환
                .map(powerUserMapper::toDto) // 각 엔티티를 DTO로 매핑
                .toList(); // 리스트로 수집

        String nextCursor = null; // 다음 커서 id 초기값
        Instant nextAfter = null; // 다음 after(createdAt) 초기값
        if (hasNext && !rows.isEmpty()) { // 다음 페이지가 있고 결과가 비어있지 않다면
            PowerUser last = rows.get(rows.size() - 1); // 마지막 요소를 취득
            nextCursor = last.getId().toString(); // 다음 커서로 마지막 id를 사용
            nextAfter = last.getCreatedAt(); // 다음 after로 마지막 createdAt을 사용
        }

        return new CursorPageResponsePowerUserDto( // 커서 페이지 응답 DTO 생성
                content, // 현재 페이지 콘텐츠
                nextCursor, // 다음 커서
                nextAfter, // 다음 after
                query.limit(), // 요청 크기
                totalElements, // 전체 개수
                hasNext // 다음 페이지 존재 여부
        ); // 응답 DTO 반환
    }

    private BooleanBuilder buildWhere(PowerUserQuery query) {
        BooleanBuilder where = new BooleanBuilder();

        if (query.after() != null && query.cursor() != null) {
            boolean isDesc = "desc".equalsIgnoreCase(query.direction());

            if (isDesc) {
                where.and(powerUser.createdAt.lt(query.after())
                                .or(powerUser.createdAt.eq(query.after()).and(powerUser.id.lt(query.cursor()))));
            } else {
                where.and(
                        powerUser.createdAt.gt(query.after())
                                .or(powerUser.createdAt.eq(query.after()).and(powerUser.id.gt(query.cursor()))));
            }
        }

        return where;
    }
}
