package io.hhplus.tdd.domain.point.model;

import lombok.Getter;
import lombok.Setter;


public record UserPoint(
        long id,
        long points,
        long updateMillis
) {

//    public static UserPoint empty(long id) {
//        return new UserPoint(id, 0, System.currentTimeMillis());
//    }
}
