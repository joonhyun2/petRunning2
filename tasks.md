# Tasks

구현 작업 목록. 상태: `[ ]` 미완료 / `[x]` 완료

요건: `requirements.md`
디자인 스펙: `run_design/spec/`

---

## 0. 프로젝트 셋업

- [x] `build.gradle` 의존성 추가 (Compose, Room, Navigation, Hilt, DataStore, Play Services Location)
- [x] `libs.versions.toml` 버전 통일
- [x] 패키지 구조 확정 (`ui/`, `data/`, `di/`)
- [x] 디자인 토큰 정의
  - [x] 색상 토큰 → `Color.kt`
  - [x] 타입 스케일 → `Type.kt` (display-xl ~ micro)
  - [x] `MaterialTheme` 래핑
- [ ] 공통 리소스: Noto Sans KR 폰트 등록

---

## 1. 공통 컴포넌트

스펙: `run_design/spec/design_system/components.md`

- [x] `AppLogo` — 새싹 로고
- [x] `BottomNavigation` (`BottomNavBar.kt`) — 홈/꾸미기/통계/프로필 4탭
- [x] `GrowthProgressBar` — 레벨 / 트랙 / 퍼센트 3열 레이아웃
- [x] `PrimaryButton` / `SecondaryButton` (`Buttons.kt`)
- [ ] `StatBadge` — 경험치/크레딧/페이스/위치 상태 pill 배지
- [x] `StatCard` — 아이콘 + 숫자 + 라벨 + 단위 세로 카드
- [x] `PetCharacter` — 펫 이미지 + 장착 아이템 오버레이

---

## 2. 데이터 레이어

### 2-1. 도메인 모델 / Entity

실제 구현: Character 대신 **Dog** 명칭 사용

- [x] `DogEntity` (table: `dog`) — 이름, 레벨, currentXp, maxXp, credit
- [x] `RunRecordEntity` (table: `run_record`) — distanceKm, elapsedSeconds, paceSecPerKm, xpGained, timestamp
  - ※ 칼로리, 경로 좌표는 미포함 (추후 추가 과제)
- [x] `OwnedItemEntity` (table: `owned_items`) — itemId, equipped
  - Badge / DecorationItem 별도 entity 없음 — 아이템 소유로 통합 관리
- [ ] 경로 좌표(RouteCoordinate) 저장 구조 추가
- [ ] 칼로리 필드 RunRecordEntity에 추가

### 2-2. DAO

- [x] `DogDao` — getDog (Flow), getDogOnce, saveDog
- [x] `RunRecordDao` — getAllRecords, getTodayRecords, insertRecord
- [x] `OwnedItemDao` — getOwnedItemIds, getEquippedItemId, insertOwnedItem, unequipAll, equipItem
- [x] `AppDatabase` (Room, version 3, `petrunning.db`) + Hilt 모듈

### 2-3. Repository

- [x] `DogRepository` — 펫 상태 조회/업데이트, 레벨업 처리
- [x] `RunRepository` — 러닝 기록 저장/조회 (RunRecordDao 래핑)
  - ※ 주간/월간 집계는 HistoryViewModel에서 처리
- [x] `ItemRepository` — 아이템 소유/장착 관리

### 2-4. 보상 계산

- [x] 달린 거리 기준 경험치/크레딧 계산 (RunningViewModel 내 구현)
- [x] 레벨업 임계값(maxXp) 기준 레벨업 처리 (DogRepository 내 구현)

---

## 3. GPS / 러닝 측정

- [x] `LocationDataSource` — FusedLocationProviderClient + callbackFlow로 GPS 수신 (3초 주기, 최소 1.5초)
  - ※ Foreground Service 미구현 — 앱이 백그라운드 전환 시 위치 수신 중단될 수 있음
- [x] 위치 권한 요청 플로우 (Fine Location + Coarse Location, MainActivity에서 처리)
  - [ ] Android 10+ Background Location 별도 권한 요청 (미구현)
- [x] 거리 누적, 시간 카운트, 페이스 계산, 일시정지/재개 (RunningViewModel 내 구현)
  - ※ 별도 `RunTracker` 클래스 없음 — ViewModel에 통합
- [ ] Wake Lock 유지 (화면 꺼짐 대비)
- [ ] Foreground Service 전환 (백그라운드 측정 유지)
- [x] GPS 연결 상태 판별 (연결됨 / 연결 중 / 실패)

---

## 4. 화면 구현

### 4-1. 홈 화면

스펙: `run_design/spec/screens/home/`

- [x] `HomeScreen` 레이아웃 (Header + Content + BottomNavigation)
- [x] 펫 이미지(pet.png) + `GrowthProgressBar`
- [x] 오늘 러닝 요약 카드 (뛴 시간 / 뛴 거리 / 평균 페이스)
- [x] `달리기 시작` 버튼 (권한 요청 → 카운트다운으로 이동)
- [x] `HomeViewModel` — 오늘 러닝 요약 + 펫 상태 로드
- [x] 오늘 데이터 없을 때 빈 상태 처리 (값 `0` 표시)

### 4-2. 카운트다운 화면

- [x] `CountdownScreen` — 3, 2, 1 애니메이션 후 Running으로 자동 전환
- [x] 하단 네비게이션 없음

### 4-3. Running 화면

스펙: `run_design/spec/screens/running/`

- [x] `RunningScreen` 레이아웃 (하단 네비 없음)
- [x] `GpsBadge` — 위치 연결 상태 3가지 (연결됨 / 연결 중 / 실패)
- [x] 페이스(크게), 거리, 시간 표시
- [x] 실시간 경험치/크레딧 배지 표시
- [x] 일시정지 / 종료 버튼
- [x] `RunningViewModel` — LocationDataSource 구독, 보상 실시간 계산
- [ ] GPS 권한 없을 때 에러 화면 처리

### 4-4. 결과(Result) 화면

스펙: `run_design/spec/screens/end_run/`

- [x] `ResultScreen` 레이아웃 (하단 네비 없음)
- [x] 총 거리, 시간, 페이스 요약 표시
- [x] 획득 경험치 / 크레딧 표시
- [x] 완료 / 공유 버튼
- [x] `ResultViewModel` — 러닝 결과 저장, 보상 펫에 반영
- [x] 저장 중 완료 버튼 비활성 처리
- [x] 공유 기능 (Android Share Intent)
- [ ] 칼로리 표시 (RunRecordEntity에 칼로리 필드 추가 후)
- [ ] 경로 지도 표시 (경로 좌표 저장 구현 후)

### 4-5. 통계 화면

스펙: `run_design/spec/screens/statics/`

- [x] `StaticsScreen` 파일 생성
- [x] `HistoryViewModel` — 러닝 기록 조회
- [ ] 주간/월간 기간 토글
- [ ] 총 거리 / 달린 횟수 / 평균 페이스 요약 카드
- [ ] 요일별 거리 막대 차트
- [ ] 페이스 추이 선 차트
- [ ] 최고 페이스 / 크레딧 / 경험치 요약 카드
- [ ] 데이터 없을 때 빈 상태 처리
- [ ] 기간 변경 시 로딩 상태

### 4-6. 꾸미기 화면

스펙: `run_design/spec/screens/decoration/`

- [x] `DecorationScreen` 파일 생성
- [x] `DecorationViewModel` — 아이템 목록/장착 상태
- [ ] 펫 미리보기 실시간 반영
- [ ] 카테고리 탭 (얼굴/헤어/옷/테마)
- [ ] 아이템 그리드 (선택/잠금 상태)
- [ ] 잠금 아이템 구매 안내 다이얼로그
- [ ] 초기화 / 적용 버튼
- [ ] 아이템 로드/적용 실패 에러 처리

### 4-7. 프로필 화면

스펙: `run_design/spec/screens/profile/`

- [x] `ProfileScreen` 파일 생성
- [ ] 펫 이름 / 레벨 / 진행률 표시
- [ ] 획득 배지 목록 + 빈 상태 처리
- [ ] 메뉴 (내 아이템 / 알림 설정 / 문의하기 / 로그아웃)
- [ ] 로그아웃 확인 다이얼로그
- [ ] `ProfileViewModel` — 프로필/배지 데이터 로드

---

## 5. 네비게이션

스펙: `requirements.md` 네비게이션 섹션

- [x] `NavHost` 설정 — 7개 route (home, countdown, running, result, decoration, statics, profile)
- [x] 하단 네비게이션 연동 (Running, Countdown, Result에서 숨김)
- [x] Running → Result 전환 시 측정 데이터 전달 (distanceKm, elapsedSeconds, paceSecPerKm)
- [x] Result → 홈 완료 후 백스택 클리어
- [ ] 통계 화면에서 기록 상세 → Result 화면 진입 (이미 route 연결됨, UI 구현 필요)

---

## 6. 마무리

- [ ] 디바이스별 반응형 확인 (360dp / 394dp / 430dp)
- [ ] 화면 회전 시 상태 보존 확인
- [ ] 다크 모드 미지원 명시 (라이트 모드 고정)
- [ ] 최소 버전(API 24) 에뮬레이터 동작 확인
- [ ] 실기기 GPS 측정 동작 확인
- [ ] Foreground Service + Wake Lock 구현 후 백그라운드 측정 검증
