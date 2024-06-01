package io.hhplus.tdd.domain.point;

import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;

import java.util.List;
import java.util.Optional;

public interface PointHistoryRepository {
    PointHistory insert(long userId, long amount, TransactionType type, long uptimeMillis);

    Optional<List<PointHistory>> selectAllByUserId(long userId);
}
