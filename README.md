# Point-Operations_java
### 요구사항
* 서비스
    * 포인트 충전, 사용, 조회
        * 잔고가 부족할 경우 포인트 사용 실패
* 동시성 처리
    * 동시에 여러 건의 포인트 충전 및 이용 요청이 들어올 경우 순차적 처리 필요</br></br>
###
###
### API
* PATCH '/point/{id}/charge': 포인트 충전
* PATCH '/point/{id}/use': 포인트 사용
* GET '/point/{id}': 포인트 조회
* GET '/point/{id}/histories': 포인트 내역 조회</br></br>
###
###
### 패키지 구조
* common
    * LockManager: userId 기준 Lock 관리 컴포넌트
    * PointManager: PointService의 도메인 로직을 처리하는 컴포넌트
    * UserPointTable: UserPoint 입력, 수정, 조회 결과를 내부적으로 저장하는 컴포넌트
    * PointHistoryTable: PointHistory 입력, 조회 결과를 내부적으로 저장하는 컴포넌트
* controller
    * PointController: Point 충전, 사용, 조회 HTTP 요청 처리
* domain.point
    * PointService: 동시성 제어를 포함한 포인트 관련 비즈니스 로직 처리
    * UserPointRepository: UserPointTable에 대한 인터페이스
    * PointHistoryRepository: PointHistoryTable에 대한 인터페이스
    * model
        * UserPoint: 유저별 id, 현재 포인트, 업데이트 시간을 표현하는 데이터 클래스
        * PointHistory: 유저별 내역 id, 유저 id, 충전/사용 타입, 잔고, 업데이트 시간을 표현하는 데이터 클래스
        * TransactionType: 포인트 충전/사용에 대한 enum 클래스
* infrastructure
    * UserPointRepositoryImplement: UserPoint 인프라 제공</br>(UserPointTable을 사용한 UserPointRepository 구현체)
    * PointHistoryRepositoryImplement: PointHistory 인프라 제공</br>(PointHistoryTable을 사용한 PointHistoryRepository 구현체)</br></br>
###
###
### 동시성 제어
* ConcurrentHashMap 기반의 Reentrant Lock 사용
    * Reentrant Lock vs Synchronized, Semaphore
        * Synchronized는 특정 블록이나 메소드에 대해 한 스레드만 접근할 수 있으며 구현이 간단하지만, 락 획득에 대한 비공정성 정책으로 인해 성능 저하 및 데드락 발생 가능
        * Semaphore는 signal을 통해 제한된 수의 스레드만 동시 접근을 허용하며 재진입 불가
    * ConcurrentHashMap vs HashTable
        * Multi-thread 환경에 최적화된 Map 구현체
            * 읽기 작업에는 lock을 사용하지 않아 여러 thread에서 동시에 접근해도 성능 보장
            * 쓰기 작업에 lock 사용
    * Reentrant Lock
        * '재진입성'이라는 의미의 'Reentrant'에서 알 수 있듯이 획득한 lock에 대해 재진입 가능
        * 즉, 동일 스레드가 연속적으로 여러 번 lock 획득 가능
        * 포인트 충전 후 바로 사용하는 등 한 명의 유저에 대한 연속적인 요청 처리 시 lock 재진입성은 필수적인 요소임</br></br>
###    
###
### 테스트 시나리오
* 단위 테스트
    * 특정 유저의 포인트 조회
        * 성공: 존재할 수 있는 아이디 입력 시 반환된 포인트 값과 기대값의 일치 상태 검증
            * 충전/사용 이력 없는 경우
            * 충전/사용 이력 있는 경우
        * 실패: 존재할 수 없는 아이디 입력 시 IllegalArgumentException 예외 처리 행위 검증
    * 특정 유저의 포인트 충전/사용 내역 조회
        * 성공: 존재할 수 있는 아이디 입력 시 반환된 포인트 충전/사용 내역과 기대값의 일치 상태 검증
            * 충전/사용 이력 없는 경우
            * 충전/사용 이력 있는 경우
        * 실패: 존재할 수 없는 아이디 입력 시 IllegalArgumentException 예외 처리 행위 검증
    * 특정 유저의 포인트 충전
        * 성공: 존재할 수 있는 아이디 및 양수 포인트 값 입력 시 포인트 증가 상태 검증
        * 실패1: 존재할 수 없는 아이디 입력 시 InterruptException 예외 처리 행위 검증
        * 실패2: 0 이하의 포인트 입력 시
            * InterruptException 예외 처리 행위 검증
            * 음수 포인트 값 입력 전 후의 일치 상태 검증
    * 특정 유저의 포인트 사용
        * 성공: 존재할 수 있는 아이디 및 잔고 이하의 양수 포인트 입력 시 포인트 감소 상태 검증
        * 실패1: 존재할 수 없는 아이디 입력 시 InterruptException 예외 처리 행위 검증
        * 실패2: 잔고 이상의 포인트 입력 시
            * InterruptException 예외 처리 행위 검증
            * 잔고 이상의 포인트 값 입력 전 후의 일치 상태 검증
        * 실패3: 음수 포인트 입력 시
            * InterruptException 예외 처리 행위 검증
            * 음수 포인트 값 입력 전 후의 일치 상태 검증
* 통합 테스트
    * 동시에 1명의 유저에 대한 포인트 충전, 사용 요청 발생 시 포인트 증감 상태 검증
    * 동시에 다수의 유저에 대한 포인트 충전, 사용, 내역 조회 요청 발생 시 포인트 상태 검증</br></br>
###
###
### 회고
