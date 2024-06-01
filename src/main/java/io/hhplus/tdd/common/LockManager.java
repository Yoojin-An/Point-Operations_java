package io.hhplus.tdd.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class LockManager {
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public <T> T executeFunctionWithLock(long userId, Supplier<T> function) throws RuntimeException {
        Lock lock = lockMap.computeIfAbsent(String.valueOf(userId), k -> new ReentrantLock());
        try {
            boolean acquired = lock.tryLock();
            if (!acquired) {
                throw new TimeoutException("지정된 락 획득 시도 시간을 초과했습니다.");
            }
            try {
                return function.get();
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
