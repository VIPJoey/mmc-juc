package com.mmc.juc;

public interface MmcTaskMerger<R> {
    R merge(R leftResult, R rightResult);
}