package io.hhplus.tdd.infrastructure;

import io.hhplus.tdd.common.UserPointTable;
import io.hhplus.tdd.domain.point.UserPointRepository;
import io.hhplus.tdd.domain.point.model.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Qualifier("userPointRepositoryImplement")
public class UserPointRepositoryImplement implements UserPointRepository {

    private final UserPointTable userPointTable = new UserPointTable();

    public Optional<UserPoint> selectById(long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint insertOrUpdate(long id, long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }
}
