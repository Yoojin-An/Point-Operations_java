package io.hhplus.tdd.infrastructure;

import io.hhplus.tdd.common.PointHistoryTable;
import io.hhplus.tdd.domain.point.PointHistoryRepository;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("pointHistoryRepositoryImplement")
public class PointHistoryRepositoryImplement implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable = new PointHistoryTable();

    @Override
    public PointHistory insert(long userId, long amount, TransactionType type, long uptimeMillis) {
        return pointHistoryTable.insert(userId, amount, type, uptimeMillis);
    }

    @Override
    public Optional<List<PointHistory>> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
