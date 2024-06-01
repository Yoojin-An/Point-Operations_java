package io.hhplus.tdd.controller;

import io.hhplus.tdd.domain.point.PointService;
import io.hhplus.tdd.domain.point.dto.PointRequest;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.UserPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/points")
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @Autowired
    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * 특정 유저의 포인트 조회
     */
    @GetMapping("{id}")
    public Optional<UserPoint> findPoints(
            @PathVariable long id
    ) {
        Optional<UserPoint> userPoint = pointService.findPoints(id);
        userPoint.ifPresent(point -> logger.info(String.format("id %d번 유저가 잔고(%dp)를 조회했습니다.", id, point.points())));
        return userPoint;
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역 조회
     */
    @GetMapping("{id}/history")
    public Optional<List<PointHistory>> findHistory(
            @PathVariable long id
    ) {
        Optional<List<PointHistory>> pointHistory = pointService.findHistory(id);
        pointHistory.ifPresent(points -> logger.info(String.format("id %d번 유저가 포인트 충전/사용 내역을 조회했습니다. :: %s", id, points.toString())));
        return pointHistory;
    }

    /**
     * 특정 유저의 포인트 충전
     */
    @PatchMapping("{id}/charge")
    public UserPoint chargePoints(
            @PathVariable long id,
            @RequestBody PointRequest pointRequest
    ) {
        UserPoint userPoint = pointService.chargePoints(id, pointRequest.getAmount());
        logger.info(String.format("id %d번 유저가 %d포인트를 충전했습니다.", id, pointRequest.getAmount()));
        return userPoint;
    }

    /**
     * 특정 유저의 포인트 사용
     */
    @PatchMapping("{id}/use")
    public UserPoint usePoints(
            @PathVariable long id,
            @RequestBody PointRequest pointRequest
    ) {
        UserPoint userPoint = pointService.usePoints(id, pointRequest.getAmount());
        logger.info(String.format("id %d번 유저가 %d포인트를 사용했습니다.", id, pointRequest.getAmount()));
        return userPoint;
    }
}
