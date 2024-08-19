package com.mmc.juc;

public interface MmcTaskCallback<R> {
    void onComplete(R result);
}