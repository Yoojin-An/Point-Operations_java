package io.hhplus.tdd.common;

import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import io.hhplus.tdd.domain.point.model.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
class PointManagerTest {

    @Autowired
    private PointManager pointManager;


    /**
     * 포인트 조회 테스트
     */
    @Test
    void 포인트_사용시_잔고가_부족하면_RuntimeException을_던진다() {
        // given: 잔고가 부족한 상황
        long id = 1L;
        long amountToUse = 5000L;

        // when & then: 초기 포인트 1000인 상황에서 5000 포인트 사용 시 RuntimeException 예외 발생 및 예외 메세지 검증
        pointManager.chargePoints(id, 1000L);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.usePoints(id, amountToUse);
        });
        Assertions.assertEquals("잔고가 부족합니다.", exception.getMessage());
    }

    @Test
    void 포인트_충전_또는_사용_이력이_있는_경우_포인트_조회에_성공한다() {
        // given
        long id = 2L;

        // when: 2번의 충전 이력이 있음을 가정
        pointManager.chargePoints(id, 10000L);
        pointManager.chargePoints(id, 20000L);
        Optional<UserPoint> userPoint = pointManager.findPoints(id);

        // then: 현재 포인트 조회 결과가 이전 2번 충전한 결과와 같음을 검증
        Assertions.assertEquals(30000L, userPoint.get().points());
    }

    @Test
    void 포인트_충전_이력이_없는_경우_조회에_실패한다() {
        // given: 유효한 아이디
        long id = 1L;

        // when: 한 번도 포인트를 충전한 적이 없는 아이디로 포인트 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointManager.findPoints(id);
        });
        Assertions.assertEquals(id + "번 유저의 정보가 없습니다.", exception.getMessage());
    }

    @Test
    void 존재할_수_없는_아이디에_대한_포인트_조회는_실패한다() {
        // given: 유효하지 않은 아이디
        long id = 0L;

        // when & then: 유효하지 않은 아이디로 포인트 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointManager.findPoints(id);
        });
        Assertions.assertEquals("아이디가 유효하지 않습니다.", exception.getMessage());
    }

    /**
     * 포인트 내역 조회 테스트
     */
    @Test
    void 포인트_충전_또는_사용_이력이_있는_경우_포인트_내역_조회에_성공한다() {
        // given: 유효한 아이디
        long id = 5L;

        // when: 3번 충전, 1번 사용의 이력이 있음을 가정
        pointManager.chargePoints(id, 10000L);
        pointManager.chargePoints(id, 20000L);
        pointManager.usePoints(id, 20000L);
        pointManager.chargePoints(id, 30000L);
        Optional<List<PointHistory>> pointHistoryOptional = pointManager.findHistory(id);

        // then: Optional이 비어있지 않은지 확인
        Assertions.assertTrue(pointHistoryOptional.isPresent());

        // Optional에서 값 추출
        List<PointHistory> pointHistory = pointHistoryOptional.get();

        // then: 잔고, 충전/사용 횟수, 충전/사용 타입 검증
        Assertions.assertEquals(10000L, pointHistory.get(0).amount());
        Assertions.assertEquals(4, pointHistory.size());
        Assertions.assertEquals(TransactionType.USE, pointHistory.get(2).type());
    }

    @Test
    void 포인트_충전_이력이_없는_경우_포인트_내역_조회에_실패한다() {
        // given: 유효한 아이디
        long id = 4L;

        // when: 한 번도 포인트를 충전한 적이 없는 아이디로 포인트 내역 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointManager.findHistory(id);
        });
        Assertions.assertEquals(id + "번 유저의 정보가 없습니다.", exception.getMessage());
    }

    @Test
    void 존재할_수_없는_아이디에_대한_포인트_내역_조회는_실패한다() {
        // given: 유효하지 않은 아이디
        long id = -1L;

        // when & then: 유효하지 않은 아이디로 포인트 내역 조회한 결과
        // IllegalArgumentException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pointManager.findHistory(id);
        });
        Assertions.assertEquals("아이디가 유효하지 않습니다.", exception.getMessage());
    }

    /**
     * 포인트 충전 테스트
     */
    @Test
    void 포인트_충전에_성공한다() {
        // given: 유효한 아이디
        long id = 6L;

        // when: 포인트 충전
        UserPoint userPoint = pointManager.chargePoints(id, 10000L);

        // then: 충전 예상 결과값이 저장된 현재 포인트와 같은지 검증
        Assertions.assertEquals(10000L, userPoint.points());
    }

    @Test
    void 충전할_포인트가_음수이거나_0이면_충전에_실패한다() {
        // given: 유효한 아이디와 유효하지 않은 충전 포인트
        long id = 7L;
        long amountToCharge = -500L;

        // when: 1000 포인트 충전에는 성공 후 -500 포인트 충전으로 예외 발생
        pointManager.chargePoints(7L, 1000L);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.chargePoints(id, amountToCharge);
        });

        // then: RuntimeException 발생 메세지 및 예외 발생 전후 포인트 일치 상태 검증
        Assertions.assertEquals("충전할 포인트는 양수여야 합니다.", exception.getMessage());
        Assertions.assertEquals(1000L, pointManager.findPoints(7L).get().points());
    }

    @Test
    void 존재할_수_없는_아이디에_대한_포인트_충전은_실패한다() {
        // given: 유효하지 않은 아이디와 유효한 포인트
        long id = -1L;
        long amount = 10000L;

        // when & then: 유효하지 않은 아이디로 포인트 조회한 결과
        // RuntimeException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.chargePoints(id, amount);
        });
        Assertions.assertEquals("아이디가 유효하지 않습니다.", exception.getMessage());
    }

    /**
     * 포인트 사용 테스트
     */
    @Test
    void 포인트_사용에_성공한다() {
        // given: 유효한 아이디와 유효한 포인트
        long id = 8L;
        long amountToUse = 5000L;

        // when: 잔고에 100000 포인트가 있음을 가정하고 5000 포인트 사용
        pointManager.chargePoints(id, 100000L);
        UserPoint userPoint = pointManager.usePoints(id, amountToUse);

        // then: 잔고에서 사용한 포인트를 뺀 값이 저장된 현재 포인트와 같은지 검증
        Assertions.assertEquals(95000L, userPoint.points());
    }

    @Test
    void 잔고_부족으로_포인트_사용에_실패한다() {
        // given: 유효한 아이디와 사용할 포인트
        long id = 9L;
        long amountToUse = 1000000L;

        // when: 잔고가 10000 포인트 있음을 가정
        pointManager.chargePoints(id, 10000L);

        // then: 잔고 이상의 포인트를 사용하여 RuntimeException 발생
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.usePoints(id, amountToUse);
        });
        // 예외 메세지 및 예외 발생 전후 포인트 일치 상태 검증
        Assertions.assertEquals("잔고가 부족합니다.", exception.getMessage());
        Assertions.assertEquals(10000L, pointManager.findPoints(9L).get().points());
    }

    @Test
    void 사용할_포인트가_0이거나_음수이면_포인트_사용에_실패한다() {
        // given: 유효한 아이디와 유효하지 않은 포인트
        long id = 10L;
        long amountToUse = 0L;

        // when & then: 유효하지 않은 포인트 사용을 시도한 결과
        // RuntimeException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.usePoints(id, amountToUse);
        });
        Assertions.assertEquals("사용할 포인트는 양수여야 합니다.", exception.getMessage());
    }

    @Test
    void 존재할_수_없는_아이디에_대한_포인트_사용은_실패한다() {
        // given: 유효하지 않은 아이디와 유효한 포인트
        long inValidId = -1L;
        long amountToUse = 10000L;

        // when & then: 유효하지 않은 아이디의 포인트 사용을 시도한 결과
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            pointManager.chargePoints(inValidId, amountToUse);
        });
        // RuntimeException이 발생하고 예외 메세지가 예상한 바와 같음을 검증
        Assertions.assertEquals("아이디가 유효하지 않습니다.", exception.getMessage());
    }
}
