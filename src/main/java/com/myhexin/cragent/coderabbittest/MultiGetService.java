package com.myhexin.cragent.coderabbittest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MultiGetService {

    private final String name;

    private final AtomicInteger nextThreadId = new AtomicInteger(1);

    private final int maximumPoolSize;

    public MultiGetService(String name, int maximumPoolSize) {
        this.name = name;
        this.maximumPoolSize = maximumPoolSize;
    }


    public <T, R> List<R> multiGet(List<T> parameterValueList, Function<T, R> queryFunction) {
        if (parameterValueList == null || parameterValueList.isEmpty()) {
            return new ArrayList<>();
        }
        ExecutorService executorService = buildExecutorService(parameterValueList.size());
        try {
            List<R> result = new ArrayList<>();
            List<Future<R>> futureList = new ArrayList<>();
            for (T parameterValue : parameterValueList) {
                futureList.add(executorService.submit(() -> queryFunction.apply(parameterValue)));
            }
            for (Future<R> future : futureList) {
                R item = future.get();
                if (item != null) {
                    result.add(item);
                }
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("fails to execute multiGet: `interrupt exception`. " + parameterValueList, e);
        } catch (Exception e) {
            throw new IllegalStateException("fails to execute multiGet: `" + e.getMessage() + "`" + parameterValueList, e);
        } finally {
            executorService.shutdown();
        }
    }

    private ExecutorService buildExecutorService(int expectedThreadPoolSize) {
        int threadPoolSize = Math.min(maximumPoolSize, expectedThreadPoolSize);
        return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("MultiGet-" + name + "-" + nextThreadId.getAndIncrement());
                    if (thread.getPriority() != Thread.NORM_PRIORITY) {
                        thread.setPriority(Thread.NORM_PRIORITY);
                    }
                    return thread;
                });
    }
}
