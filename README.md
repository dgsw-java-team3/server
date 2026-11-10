# 오늘의 학교 — 급식 CRUD

팀 공통 베이스 `49f18c9`(BaseTimeEntity 추가)를 기반으로 **급식 CRUD 5개와 해당 테스트**를 구현했습니다. 리뷰·순위는 후속 구현 범위입니다.

## 실행

Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.8.2, H2를 사용합니다. 추가 계정·환경 변수·DB 설치는 필요 없습니다. 최초 빌드에는 의존성 다운로드를 위한 인터넷이 필요합니다.

```bash
# macOS에서 Java 21을 선택해야 하는 경우
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

./gradlew clean test bootJar
./gradlew bootRun
# 또는
java -jar build/libs/todayschool-0.0.1-SNAPSHOT.jar
```

Windows에서는 `gradlew.bat`을 사용합니다.

- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs
- 급식 목록: http://localhost:8080/api/meals
- H2 콘솔: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:todayschool`, 사용자 `sa`, 비밀번호 빈 값.
- `src/main/resources/data.sql`에 급식 5개를 제공합니다. 서버를 재시작하면 데이터가 초기화됩니다.
- 공통 BaseTimeEntity의 `LocalDateTime`을 그대로 사용하므로 생성·수정 시각에는 UTC 오프셋이 포함되지 않습니다.

## Global / Domain 구조

```text
com.todayschool
├── global
│   ├── config       # 팀 QueryDSL·OpenAPI 설정 재사용
│   ├── entity       # 팀 BaseTimeEntity 재사용
│   ├── exception    # 공통 예외 처리 + 급식 오류 코드
│   └── response     # 팀 ApiResponse·ErrorResponse 재사용
├── domain
│   └── meal
│       ├── controller
│       ├── dto
│       ├── entity
│       ├── repository
│       └── service  # MealService / MealServiceImpl
└── TodayschoolApplication
```

의존 방향은 Controller → Service → Repository입니다. Controller는 엔티티나 Repository를 참조하지 않으며 HTTP 매핑·검증·DTO 응답만 처리합니다. Service에서 트랜잭션, 중복 검사, DTO 변환을 담당합니다. global은 domain을 import하지 않습니다. QueryDSL 순위 구현은 이번 작업에 이식하지 않았고, JPA 필수 쿼리 요건은 Query Method로 충족합니다.

## API 5개

| Method | 경로 | 기능 | 성공 |
| --- | --- | --- | --- |
| POST | `/api/meals` | 등록 | 201 + Location |
| GET | `/api/meals?date=2026-11-10&page=0&size=20` | 목록 / 날짜 필터 | 200 |
| GET | `/api/meals/{mealId}` | 상세 | 200 |
| PUT | `/api/meals/{mealId}` | 날짜·구분·메뉴 전체 수정 | 200 |
| DELETE | `/api/meals/{mealId}` | 삭제 | 200 |

POST / PUT 본문:

```json
{"date":"2026-11-12","mealType":"LUNCH","menu":"쌀밥, 미역국, 제육볶음, 김치"}
```

- 세 필드 모두 필수. `mealType`은 `BREAKFAST`, `LUNCH`, `DINNER`.
- 메뉴는 공백 제외 최소 1자, 최대 1,000자.
- 날짜와 구분의 조합은 유일합니다. 중복 생성·수정은 `409 DUPLICATE_MEAL`입니다.
- 목록은 날짜 내림차순·ID 오름차순. `date` 생략 시 전체 목록.
- `page`는 0부터, `size`는 1~100. 목록 data는 content/page/size/totalElements/totalPages.
- 존재하지 않는 ID는 `404 MEAL_NOT_FOUND`, 잘못된 입력은 `400 INVALID_INPUT_VALUE`.
- 잘못된 HTTP 메서드는 405, 지원하지 않는 Content-Type은 415.

공통 성공 응답은 `{"success":true,"data":...,"message":"요청이 성공했습니다."}`입니다. 삭제는 `data:null`, `message:"급식이 삭제되었습니다."`입니다. 실패는 팀 규격인 `{"success":false,"error":{"code":"...","message":"..."}}`를 그대로 사용합니다.

## 검증

2026-11-10, Java 21에서 `./gradlew clean test bootJar --offline --no-daemon` 성공. `--offline`은 이번 실행 환경에 의존성이 이미 캐시되어 있어 사용했으며, 최초 실행 시에는 빼야 합니다.

| 테스트 | 건수 | 검증 |
| --- | ---: | --- |
| MealControllerTest | 14 | 201/Location, DTO 검증, 날짜·ID·페이지 오류, 404/409/405/415 |
| MealServiceTest | 9 | 생성·수정·삭제, 중복·없는 대상·DB 제약 예외·조회 범위 |
| MealIntegrationTest | 7 | 실제 H2 CRUD, Query Method, 페이징, 무결성, OpenAPI 5개 작업 |
| 기존 팀 테스트 | 10 | BaseTimeEntity 2건, 공통 예외 처리 7건, 컨텍스트 시작 1건 |
| 합계 | **40** | **실패 0, 오류 0, 건너뜀 0** |

HTML 결과는 `build/reports/tests/test/index.html`, XML은 `build/test-results/test/`입니다. HTTP 시연 예시는 [meal-crud.http](docs/meal-crud.http)를 참고하세요.

## 최종 검토 및 남은 범위

평가 기준별 구현 근거와 남은 제출 항목은 [최종 검토](docs/assessment-review.md)에 정리했습니다. 이 작업본은 선택한 CRUD 5개까지만 포함합니다. 로그인·관리자 권한은 구현하지 않았고 모든 API는 인증 없이 호출됩니다. 실제 운영 시 인증·권한 처리가 필요합니다. 향후 리뷰를 합칠 때는 급식 삭제와 리뷰 FK 관계를 함께 처리해야 합니다.
