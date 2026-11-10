# 수행평가 조건 최종 검토 — 급식 CRUD 부분 구현

검토 기준: 사용자가 제공한 수행평가 안내. 검토일: 2026-11-10. 베이스: `49f18c9`.

## 이번 범위에서 확인한 필수 요건

| 조건 | 결과 | 근거 |
| --- | --- | --- |
| Spring Boot 4.1.1 / Java 21 이상 | 충족 | 기존 build.gradle 유지, Java 21 빌드 성공 |
| H2와 더미 SQL | 충족 | application.yml 유지, data.sql 급식 5개, 시퀀스 재설정 |
| 빌드 가능 | 확인 | clean test bootJar 성공 |
| REST의 POST/GET/PUT/DELETE | 충족 | MealController 5개 엔드포인트 |
| 표준 상태 코드 / 공통 JSON | 충족 | 팀 ApiResponse/ErrorResponse 재사용, 201/200/400/404/405/409/415 검증 |
| Controller–Service–Repository | 충족 | domain/meal 내부 계층 분리, Controller에서 Entity/Repository 참조 없음 |
| Service 인터페이스 / 구현체 분리 | 충족 | MealService / MealServiceImpl |
| JPA 엔티티 | 충족 | Meal, 날짜·구분 고유 제약, 팀 BaseTimeEntity 상속 |
| Query Method/JPQL/QueryDSL 중 1개 이상 | 충족 | findByDate, existsByDateAndMealType, existsByDateAndMealTypeAndIdNot |
| 요청·응답 DTO, 엔티티 비노출 | 충족 | MealCreateRequest, MealUpdateRequest, MealResponse, MealPageResponse |
| 자동화 테스트 3건 이상 | 충족 | 추가 30건 + 기존 팀 10건 = 40건 성공 |
| Controller + Service/Repository 테스트 | 충족 | MockMvc Controller 단위 테스트, Mockito Service 단위 테스트 |
| 정상 + 실패·예외 검증 | 충족 | 400/404/409, 저장 제약 충돌, 누락 리소스, 잘못된 페이지 등 |
| JUnit / AssertJ / MockMvc | 충족 | 세 도구 모두 실제 단언에 사용 |
| Global / Domain 구조 및 공통 재사용 | 충족 | global과 domain/meal 분리, global→domain import 없음 |
| 일관된 이름 | 충족 | Controller / Service / ServiceImpl / Repository / Request / Response 접미사 |

여기서 '충족'은 현재 급식 CRUD 구현에 대한 기술적 확인입니다. 팀 전체의 다른 도메인이나 최종 배점을 확인했다는 의미는 아닙니다.

## 최신 공통 베이스와의 차이

- ApiResponse, ErrorResponse, BaseTimeEntity, BusinessException, 설정·의존성 버전은 그대로 재사용했습니다.
- ErrorCode의 급식 구역에 MEAL_NOT_FOUND, DUPLICATE_MEAL을 추가했습니다.
- 공통 ErrorCode에 UNSUPPORTED_MEDIA_TYPE을 추가하고, GlobalExceptionHandler에서 415로 응답하도록 연결했습니다. 기존에는 일반 예외에 들어가 500이 될 수 있었습니다.
- 경로·쿼리의 @Positive/@Min/@Max 실패를 400으로 처리하는 HandlerMethodValidationException 처리를 추가했습니다.
- 405 응답에 Spring이 제공하는 Allow 헤더를 보존했습니다.
- GlobalExceptionHandler를 새로 복제하거나 도메인에 별도 Advice를 만들지 않았습니다.

## 추가 기능 30점 항목 — 만점 충족을 확정할 수 없음

현재 Swagger/OpenAPI로 5개 API 명세·예시·입력 제약을 제공하고, Bean Validation으로 필수·길이·범위 검증을 실제 적용했습니다. DB 고유 제약과 사전 조회를 함께 사용하고, 데이터 조회에 페이징을 적용했습니다.

다만 안내의 추가 기능 배점은 **핵심 문제 해결에 2개 이상의 라이브러리/프레임워크를 실질적으로 활용한 근거**를 요구합니다. 필수 입력 검증에 쓰인 Bean Validation이 별도 추가 기술로 인정될지는 확정할 수 없습니다. QueryDSL은 기존 베이스에 설치되어 있지만 이번 CRUD에서는 사용하지 않으므로 이번 성과로 계산하지 않습니다.

팀 통합 시 기존 전체 구현의 QueryDSL 기간별 순위 기능을 이식하고 집계 쿼리·정렬·필터·검증 결과를 근거로 제시하는 것이 후속 과제입니다. 이번에는 사용자가 선택한 CRUD 범위를 유지했습니다. 단순 설치를 실제 활용으로 보고하거나 측정하지 않은 성능 향상을 주장하지 마세요.

## 아직 별도로 완료해야 하는 항목

| 항목 | 상태 |
| --- | --- |
| 급식 리뷰 3개 + 순위 1개 API | 이번 부분 이식에 미포함. 기존 전체 구현에 보존 |
| 실습실 예약·분실물 도메인 | 팀원 구현·통합 후 전체 검증 필요 |
| 개인별 1주차 2건 / 2주차 2건 유효 커밋 | 기능 구현과 테스트·문서를 별도 작업 단위로 기록. 2주차에도 실제 작업 2건 이상이 필요하며 개인별 GitHub 이력을 보고서에 첨부해야 함 |
| 개인 보고서 PDF / 기여도 / 실행 화면 | 작성·제출 필요. 이 문서는 검토 자료이며 제출 보고서가 아님 |
| 실행 조치의 보고서 기재 | README에 정리함. 본인 제출 보고서에 반영 필요 |
| 제출 시 Public 저장소 | 공개 전환 작업은 수행하지 않음. 제출 전 실제 상태 확인 필요 |
| 팀 구성 시트의 팀명·주제·저장소 URL | 외부 시트를 수정하지 않음. 본인 확인 필요 |
| 질의 발표 | 3계층·DTO·트랜잭션·중복 제약·테스트 구분을 설명할 수 있도록 준비 |

## 실제 검증 근거

- `MealControllerTest`: 정상 등록, 빈 메뉴, 잘못된 날짜·페이지·ID, 깨진 JSON, 없는 급식, 중복 급식, HTTP 메서드와 Content-Type, PUT 필수 필드.
- `MealServiceTest`: 정상 생성·수정·삭제, 중복 생성·수정, 없는 급식, DB 고유 제약 예외 변환, 잘못된 페이징 차단.
- `MealIntegrationTest`: 실제 H2 신규 ID, 수정 후 재조회, 삭제 후 404, 날짜 필터와 페이지 경계, 중복 요청 시 데이터 유지, 고유 제약, OpenAPI 작업 5개.
- 기존 팀 공통 테스트 10건도 함께 통과했습니다.
- 테스트 결과: 총 40건, 실패 0, 오류 0, 건너뜀 0. XML/HTML 결과는 build 아래에서 재생성할 수 있습니다.

서비스의 중복 사전 조회는 이해하기 쉬운 409 응답을 만들고, DB 고유 제약은 사전 조회를 동시에 통과하는 경쟁에서도 중복 저장을 방지합니다. 이번 범위에는 동시 부하 테스트를 포함하지 않았으므로 운영 환경의 처리량·지연 시간은 검증하지 않았습니다.
