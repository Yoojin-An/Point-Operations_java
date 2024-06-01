package io.hhplus.tdd.domain.point;

import io.hhplus.tdd.domain.point.model.UserPoint;

import java.util.Optional;

public interface UserPointRepository {

    Optional<UserPoint> selectById(long id);

    UserPoint insertOrUpdate(long id, long amount);
}
