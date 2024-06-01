package io.hhplus.tdd.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@SpringBootTest
public class LockManagerTest {

    @Autowired
    private LockManager lockManager;

    @Test
    public void 락_획득_시_파라미터로_받은_함수를_실행시킨다() throws Exception {
        // given
        long userId = 1L;
        String result = "Success";
        Supplier<String> function = () -> result;

        // when
        String actualResult = lockManager.executeFunctionWithLock(userId, function);

        // then
        Assertions.assertEquals(result, actualResult);
    }

    @Test
    public void 락_획득_실패_시_RuntimeException을_던진다() throws Exception {
        // given
        long userId = 1L;
        String result = "Fail";
        Supplier<String> function = () -> result;

        // userId에 무조건 락 획득을 실패하는 ReentrantLock 매핑
        ConcurrentHashMap<String, ReentrantLock> lockMap = getLockMap(lockManager);
        lockMap.put(Long.toString(userId), new AlwaysFailingLock());

        // when
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            lockManager.executeFunctionWithLock(userId, function);
        });

        // then
        Assertions.assertEquals("지정된 락 획득 시도 시간을 초과했습니다.", exception.getMessage());
    }
    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, ReentrantLock> getLockMap(LockManager lockManager) throws Exception {
        Field lockMapField = LockManager.class.getDeclaredField("lockMap");
        lockMapField.setAccessible(true);
        return (ConcurrentHashMap<String, ReentrantLock>) lockMapField.get(lockManager);
    }

    @Test
    public void 동시에_여러_요청이_들어오면_각_요청에_대해_락을_획득_후_순차적으로_처리한다() throws Exception {
        // given
        long userId = 1L;
        Supplier<String> function = () -> "Success";
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<String>> results = new CopyOnWriteArrayList<>();

        // 10개의 task를 병렬로 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            int taskNumber = i;
            Future<String> futureResult = executorService.submit(() -> {
                latch.countDown();  // 스레드 시작을 알림
                latch.await();      // 모든 스레드가 시작할 때까지 대기
                System.out.println("Task " + taskNumber + " start");
                String taskResult = lockManager.executeFunctionWithLock(userId, function);
                System.out.println("Task " + taskNumber + " end");
                return taskResult;
            });
            results.add(futureResult);
        }

        // then
        for (Future<String> result : results) {
            Assertions.assertEquals("Success", result.get());
        }

        executorService.shutdown();
    }
}