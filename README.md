# 📚 덕후감 - 독서의 즐거움을 공유하는 커뮤니티

![logo](./docs/logo.png)

### 관련 자료

> 배치 스케줄링 및 집계는 별도 저장소: [deokhugam-batch](https://github.com/sb5-deokhugam-team8/deokhugam-batch)
>

> 협업 문서: [Notion 팀 문서 바로가기](https://www.notion.so/5-8-2a0911ad8de0805d9c81fee4d4337223?pvs=21)
>

> 발표자료 PDF: [8팀_덕후감_발표자료.pdf](./docs/8팀_덕후감_발표자료.pdf)
>

---

# **🔍** 프로젝트 개요

덕후감(Deokhugam)은 **책 읽는 즐거움을 공유하고,** 인증/인가, 사용자/도서/리뷰/댓글/알림 등 **지식과 감상을 나누는 책 덕후들의 커뮤니티 플랫폼**입니다.

- **프로젝트 기간**: 2025.10.20 ~ 2025.11.07
- **목표**: 백엔드 서버와 배치 서버의 분리 구현을 통한 안전성 및 성능 확보
- **주요 특징:**
    - PostgreSQL + Spring Data JPA 기반 안정적 데이터 관리
    - Spring Batch를 이용한 대시보드 순위 관리
    - WebClient를 이용한 2개의 OpenAPI 연동
    - CI/CD를 이용한 자동 배포 구현
    - Docker 컨테이너 기반 멀티 서버 구성
    - AWS ECS, ECR, RDS, S3를 통한 클라우드 배포

---

# 👥 팀원 소개

| 팀장 | 팀원 | 팀원 | 팀원 | 팀원 |
| --- | --- | --- | --- | --- |
| <img src="https://avatars.githubusercontent.com/u/142771763?s=400&u=0d09371b5614ae44a221c553da1e66263ae1562d&v=4" width="120" /> | <img src="https://avatars.githubusercontent.com/u/104526249?v=4" width="120" /> | <img src="https://avatars.githubusercontent.com/u/112528806?v=4" width="120" /> | <img src="https://avatars.githubusercontent.com/u/217701870?v=4" width="120" /> | <img src="https://avatars.githubusercontent.com/u/96003021?v=4" width="120" /> |
| [**박지성**](https://github.com/jisung403) | [**정수진**](https://github.com/5ranke) | [**김지은**](https://github.com/j2eun0922) | [**차규환**](https://github.com/chagyuhwan) | [**변우혁**](https://github.com/woohyuk99) |

---

# 📌주요 기능

| 영역 | 기능 |
| --- | --- |
| 사용자 | 회원가입 · 로그인 · 수정 · 탈퇴 |
| 도서 | OCR ISBN · NAVER API 조회 · 등록/수정/삭제 · 목록/상세 |
| 리뷰 | 등록 · 수정 · 삭제 · 상세/목록 조회 |
| 댓글 | 등록 · 수정 · 삭제 · 상세/목록 조회 |
| 알림 | 등록 · 읽음 처리 · 전체 읽기 · 목록 조회 |
| 대시보드 | 인기 도서 · 인기 리뷰 · 파워 유저 순위 |

[Swagger 링크](http://sprint-project-1196140422.ap-northeast-2.elb.amazonaws.com/sb/deokhugam/api/swagger-ui/index.html#/)

---

# **🧩 팀원별 담당 기능**

### 박지성

- 시스템 아키텍처 설계
- 썸네일 이미지를 S3에 저장하여 DB에 url만 저장되도록 구현
- ISBN을 입력해서 Naver API를 통해 책의 세부내용을 불러옴
- ISBN 이미지를 등록해서 OCR API를 통해 책의 ISBN 정보를 불러옴
- 도서, 댓글 파트 구현

### 정수진

- 배포 아키텍처 설계
- GitHub Actions 통한 자동 배포(CI/CD) 파이프라인 구축
- Dockerfile을 각각 서버에 구현함으로써 각각의 애플리케이션 실행 환경 정의
- S3, RDS, ECR, ECS를 이용하여 배포 구현
- 리뷰 파트 구현

### 김지은

- 배치 서버 개발 리드
- 스케줄러, job을 이용하여 오전 9시에 점수 및 순위가 자동으로 계산되도록 구현
- 사용자 관리 및 대시보드 구현

### 차규환

- 배치 서버 개발
- 스케줄링 기반 알림 삭제 로직 구현
- 알림 파트 구현

### 변우혁

- 발표 자료 준비

---

# **⚙️ 기술 스택**

![stack](./docs/Stack.png)

---

# 🖥️ 시스템 아키텍처

![arcitact](./docs/Arcitact.png)

---

# 📚 ERD

![erd](./docs/ERD.png)

---

# **📁** 프로젝트 구조

### 백엔드 서버

```markdown
backend/                           # 덕후감 백엔드 루트 디렉토리
├── .github/                       # GitHub 설정 및 협업/배포 관련 구성
│   ├── ISSUE_TEMPLATE/            # 이슈 템플릿 (버그/기능요청 등)
│   ├── PULL_REQUEST_TEMPLATE/     # PR 템플릿
│   └── workflows/                 # GitHub Actions CI/CD 워크플로우 정의
├── Dockerfile                     # Docker 이미지 빌드 설정
├── config/                        # 스프링 및 전역 환경 설정
├── controller/                    # REST API 엔드포인트 (@RestController)
├── dto/                           # 계층 간 데이터 전달용 DTO 모듈
│   ├── book/                      # 도서 관련 DTO
│   │   └── naver/                 # 네이버 API 연동 DTO
│   ├── comment/                   # 댓글 DTO
│   ├── cursor/                    # 커서 기반 페이지네이션 DTO
│   ├── dashboard/                 # 대시보드/통계 DTO
│   ├── notification/              # 알림 DTO
│   ├── review/                    # 리뷰 DTO
│   └── user/                      # 사용자/인증 DTO
├── entity/                        # JPA 엔티티 모듈
│   └── base/                      # 공통 엔티티(BaseEntity)
├── exception/                     # 예외 및 에러 코드 관리
│   ├── book/                      # 도서 도메인 예외
│   └── user/                      # 사용자 도메인 예외
├── integration/                   # 외부 서비스 연동 API 모듈
│   ├── naver/                     # 네이버 도서 API 연동
│   └── ocr/                       # OCR(이미지 텍스트 변환) API 연동
├── mapper/                        # DTO ↔ Entity 변환 MapStruct 매퍼
├── repository/                    # JPA/QueryDSL 기반 Repository 인터페이스
│   └── query/                     # 페이지네이션 커스텀 쿼리(QueryDSL)
├── service/                       # 서비스 계층 (비즈니스 로직)
├── storage/                       # 스토리지 추상화 (파일/이미지 등)
│   └── impl/                      # AWS S3 등 실제 스토리지 구현체
├── support/                       # 공용 유틸/도우미/지원 클래스
└───────────resources/             # 스프링 리소스 폴더
            ├── application.yaml   # 공통 설정 파일 (프로필 분기 포함)
            ├── application-dev.yaml   # 개발 환경 설정
            └── application-prod.yaml  # 운영(프로덕션) 설정
```

---

# 브랜치 전략

- feature/기능(기능 개발) → develop(병합) → main(배포)
- 초기 세팅을 위해 Entity, Docker 브랜치추가

---

# 협업 방식

- PR, Issue 템플릿을 만들어 공통적으로 사용함
- Projects를 통해 팀원들의 진행 사항 파악
- PR은 단순 오류가 아니면 다른 팀원 1명 이상의 코드리뷰를 받아 Merge
- 노션을 통한 문서화
- 디스코드를 이용한 매일 오전 / 오후 회의

---

# 커밋 컨벤션

| 커밋 유형 | 설명 | 예시 |
| --- | --- | --- |
| **feat** | 새로운 기능 추가 | `feat: 사용자 로그인 기능을 추가했습니다.` |
| **fix** | 버그 수정 | `fix: 암호 확인 오류를 수정했습니다.` |
| **docs** | 문서 수정 | `docs: 리드미 파일에서 설치관련 문서를 수정했습니다.` |
| **style** | 코드 스타일 수정 (기능 변화 없음) | `style: 코드 표준에 따라 코드를 스타일을 수정했습니다.` |
| **refactor** | 코드 리팩토링 (기능 변화 없음) | `refactor: Member Service에서 중복된 코드를 삭제했습니다.` |
| **test** | 테스트 코드 추가 또는 수정 | `test: 사용자 인증 단위 테스트를 추가했습니다.` |
| **chore** | 빌드 업무, 패키지 매니저 설정 등 기타 변경 | `chore: 배포용 빌드 스크립트를 업데이트했습니다.` |
| **perf** | 성능 개선 | `perf: 사용자 조회 쿼리 최적화를 통해 성능을 개선했습니다. 10s => 1s` |
| **ci** | CI 설정 파일 및 스크립트 수정 | `ci: Github Action 구성을 수정했습니다.` |
| **build** | 빌드 시스템 또는 외부 종속성 수정 | `build: JPA dependency를 추가했습니다.` |
| **revert** | 이전 커밋 되돌리기 | `revert: revert "feat: 사용자 로그인 기능을 추가했습니다."` |
