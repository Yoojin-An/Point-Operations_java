package io.hhplus.tdd.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AlwaysFailingLock extends ReentrantLock {
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        return false;
    }
}
