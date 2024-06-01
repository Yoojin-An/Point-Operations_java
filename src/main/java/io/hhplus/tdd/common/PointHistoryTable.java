package io.hhplus.tdd.common;


import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 해당 Table 클래스는 변경하지 않고 공개된 API 만을 사용해 데이터를 제어합니다.
 */
@Component
public class PointHistoryTable {
    private final List<PointHistory> table = new ArrayList<>();
    private long cursor = 1;

    public PointHistory insert(long userId, long amount, TransactionType type, long updateMillis) {
        throttle();
        PointHistory pointHistory = new PointHistory(cursor++, userId, amount, type, updateMillis);
        table.add(pointHistory);
        return pointHistory;
    }

    public Optional<List<PointHistory>> selectAllByUserId(long userId) {
        List<PointHistory> userPointHistory = table.stream()
                .filter(pointHistory -> pointHistory.userId() == userId)
                .collect(Collectors.toList());
        return userPointHistory.isEmpty() ? Optional.empty() : Optional.of(userPointHistory);
    }

    private void throttle() {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 300L));
        } catch (InterruptedException ignored) {

        }
    }
}
