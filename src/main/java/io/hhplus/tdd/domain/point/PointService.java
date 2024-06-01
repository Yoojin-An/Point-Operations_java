package io.hhplus.tdd.domain.point;

import io.hhplus.tdd.common.LockManager;
import io.hhplus.tdd.common.PointManager;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.UserPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;

@Service
public class PointService {

    private final LockManager lockManager;
    private final PointManager pointManager;

    @Autowired
    PointService(LockManager lockManager, PointManager pointManager) {
        this.lockManager = lockManager;
        this.pointManager = pointManager;
    }

    public Optional<UserPoint> findPoints(long userId) {
        return pointManager.findPoints(userId);
    }

    public Optional<List<PointHistory>> findHistory(long userId) {
        return pointManager.findHistory(userId);
    }

    public UserPoint chargePoints(long userId, long amountToCharge) {
        return lockManager.executeFunctionWithLock(userId, () -> {
            try {
                return pointManager.chargePoints(userId, amountToCharge);
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    public UserPoint usePoints(long userId, long amount) {
        return lockManager.executeFunctionWithLock(userId, () -> {
            try {
                return pointManager.usePoints(userId, amount);
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }
}
