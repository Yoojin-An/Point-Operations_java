package io.hhplus.tdd.service;

import io.hhplus.tdd.domain.point.PointService;
import io.hhplus.tdd.domain.point.model.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class PointServiceIntegrationTest {

    private final PointService pointService;

    @Autowired
    public PointServiceIntegrationTest(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * 동일 유저에 대한 동시성 처리
     */
    @Test
    public void 동일_유저의_포인트_충전_사용_동시_요청_시_포인트_증감_상태_검증() throws Exception {
        // given: 1번 유저의 초기 잔고를 500000으로 설정
        long id = 1L;
        long initialPoint = pointService.chargePoints(id, 500000L).points();
        long amountToUse1 = 1000L; // 사용할 금액 1
        long amountToCharge1 = 3000L; // 충전할 금액 1
        long amountToCharge2 = 10000L; // 충전할 금액 2

        // when: 동시에 실행시킬 충전 2회, 사용 1회의 테스크 정의
        CompletableFuture<Void> chargeTask1 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id, amountToCharge1);
        });
        CompletableFuture<Void> useTask1 = CompletableFuture.runAsync(() -> {
            pointService.usePoints(id, amountToUse1);
        });
        CompletableFuture<Void> chargeTask2 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id, amountToCharge2);
        });

        CompletableFuture<Void> totalTasks = CompletableFuture.allOf(chargeTask1, chargeTask2, useTask1);
        totalTasks.join(); // chargeTask1, chargeTask2, useTask1 동시 실행
        Optional<UserPoint> result = pointService.findPoints(id);

        // then
        Assertions.assertEquals(initialPoint - amountToUse1 + amountToCharge1 + amountToCharge2, result.map(UserPoint::points).orElse(0L));
    }

    /**
     * 다수 유저의 다양한 요청에 대한 동시성 처리
     */
    @Test
    public void 동시에_다수_유저의_포인트_충전_사용_조회_요청_시_포인트_증감_상태_검증() throws Exception {
        // given: 3명의 유저 모두 초기 잔고 50000인 상태에서 각각 포인트 충전, 사용, 조회 요청
        long id1 = 1L; // 1번 유저 아이디
        long id2 = 2L; // 2번 유저 아이디
        long id3 = 3L; // 3번 유저 아이디
        long initialPoint = 50000L; // 초기 잔고
        long amountToUse = 1000L; // 각 유저가 1회 사용할 금액
        long amountToCharge = 5000L; // 각 유저가 1회 충전할 금액
        pointService.chargePoints(id1, initialPoint);
        pointService.chargePoints(id2, initialPoint);
        pointService.chargePoints(id3, initialPoint);

        // when: 동시에 실행시킬 테스크 정의
        CompletableFuture<Void> user1ChargeTask1 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id1, amountToCharge);
        });
        CompletableFuture<Void> user1ChargeTask2 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id1, amountToCharge);
        });
        CompletableFuture<Void> user1GetTask1 = CompletableFuture.runAsync(() -> {
            pointService.findPoints(id1);
        });
        CompletableFuture<Void> user2UseTask1 = CompletableFuture.runAsync(() -> {
            pointService.usePoints(id2, amountToUse);
        });
        CompletableFuture<Void> user2ChargeTask1 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id2, amountToCharge);
        });
        CompletableFuture<Void> user2ChargeTask2 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id2, amountToCharge);
        });
        CompletableFuture<Void> user3UseTask1 = CompletableFuture.runAsync(() -> {
            pointService.usePoints(id3, amountToUse);
        });
        CompletableFuture<Void> user3UseTask2 = CompletableFuture.runAsync(() -> {
            pointService.usePoints(id3, amountToUse);
        });
        CompletableFuture<Void> user3ChargeTask1 = CompletableFuture.runAsync(() -> {
            pointService.chargePoints(id3, amountToCharge);
        });

        CompletableFuture<Void> totalTasks = CompletableFuture.allOf(
                user1ChargeTask1, user1ChargeTask2, user1GetTask1,
                user2UseTask1, user2ChargeTask1, user2ChargeTask2,
                user3UseTask1, user3UseTask2, user3ChargeTask1
        );
        totalTasks.join(); // 모든 태스크 동시 실행

        Optional<UserPoint> user1Result = pointService.findPoints(id1);
        Optional<UserPoint> user2Result = pointService.findPoints(id2);
        Optional<UserPoint> user3Result = pointService.findPoints(id3);

        // then
        Assertions.assertEquals(initialPoint + amountToCharge + amountToCharge, user1Result.map(UserPoint::points).orElse(0L));
        Assertions.assertEquals(initialPoint - amountToUse + amountToCharge + amountToCharge, user2Result.map(UserPoint::points).orElse(0L));
        Assertions.assertEquals(initialPoint - amountToUse - amountToUse + amountToCharge, user3Result.map(UserPoint::points).orElse(0L));
    }
}