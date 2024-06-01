package io.hhplus.tdd.common;

import io.hhplus.tdd.domain.point.PointHistoryRepository;
import io.hhplus.tdd.domain.point.UserPointRepository;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.point.model.TransactionType;
import io.hhplus.tdd.domain.point.model.UserPoint;
import io.hhplus.tdd.infrastructure.PointHistoryRepositoryImplement;
import io.hhplus.tdd.infrastructure.UserPointRepositoryImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PointManager {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Autowired
    public PointManager(@Qualifier("userPointRepositoryImplement") UserPointRepository userPointRepository,
                        @Qualifier("pointHistoryRepositoryImplement") PointHistoryRepository pointHistoryRepository) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    private void validateId(long userId) {
        if ( userId <= 0 ) {
            throw new IllegalArgumentException("아이디가 유효하지 않습니다.");
        }
    }

    private void validateAmount(long amount, TransactionType transactionType) {
        String action = switch (transactionType) {
            case CHARGE -> "충전";
            case USE -> "사용";
        };

        if ( amount <= 0 ) {
            throw new IllegalArgumentException(action + "할 포인트는 양수여야 합니다.");
        }
    }

    private void checkBalanceSufficient(long userId, long amountToUse) {
        Optional<UserPoint> currentBalance = userPointRepository.selectById(userId);

        if ( currentBalance.isEmpty() ) {
            throw new IllegalArgumentException(userId + "번 유저의 정보가 없습니다.");
        }

        if ( amountToUse > currentBalance.get().points() ) {
            throw new IllegalArgumentException("잔고가 부족합니다.");
        }
    }

    public Optional<UserPoint> findPoints(long userId) {
        validateId(userId);

        Optional<UserPoint> userPoint = userPointRepository.selectById(userId);
        if (userPoint.isEmpty()) {
            throw new IllegalArgumentException(userId + "번 유저의 정보가 없습니다.");
        }

        return userPoint;
    }

    public Optional<List<PointHistory>> findHistory(long userId) {
        validateId(userId);

        Optional<List<PointHistory>> listOfPointHistory = pointHistoryRepository.selectAllByUserId(userId);
        if ( listOfPointHistory.isEmpty() ) {
            throw new IllegalArgumentException(userId + "번 유저의 정보가 없습니다.");
        }

        return listOfPointHistory;
    }

    public UserPoint chargePoints(long userId, long amountToCharge) {
        validateId(userId);
        validateAmount(amountToCharge, TransactionType.CHARGE);

        Optional<UserPoint> currentUserPoint = userPointRepository.selectById(userId);
        long currentAmount = currentUserPoint.map(UserPoint::points).orElse(0L); // userId에 대한 point 정보 없으면 0으로 초기화

        UserPoint updatedBalance = userPointRepository.insertOrUpdate(userId, currentAmount + amountToCharge);
        pointHistoryRepository.insert(userId, updatedBalance.points(), TransactionType.CHARGE, updatedBalance.updateMillis());

        return updatedBalance;
    }

    public UserPoint usePoints(long userId, long amountToUse) {
        validateId(userId);
        validateAmount(amountToUse, TransactionType.USE);
        checkBalanceSufficient(userId, amountToUse);

       Optional<UserPoint> currentUserPoint = userPointRepository.selectById(userId);
        if (currentUserPoint.isEmpty()) {
            throw new IllegalArgumentException(userId + "번 유저의 정보가 없습니다.");
        }
        long currentAmount = currentUserPoint.get().points();

        UserPoint updatedBalance = userPointRepository.insertOrUpdate(userId, currentAmount - amountToUse);
        pointHistoryRepository.insert(updatedBalance.id(), updatedBalance.points(), TransactionType.USE, updatedBalance.updateMillis());

        return updatedBalance;
    }
}
