package com.myhexin.cragent.coderabbittest;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private AtomicInteger current = new AtomicInteger();

    private final int sampleRate;

    private final int checkValue;

    public Counter(int sampleRate, int checkValue) {
        this.sampleRate = sampleRate;
        this.checkValue = checkValue;
    }

    public boolean checkAndInc() {
        // 原子获取v
        int v = current.getAndAccumulate(1, (pre, up) -> {
            int next = pre + up;
            if (next >= sampleRate) {
                return 0;
            }
            return next;
        });
        return v == checkValue;
    }
}
