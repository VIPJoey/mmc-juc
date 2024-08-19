package com.mmc.juc;

import java.util.List;

public interface MmcTaskProcessor<T, R> {
    R process(List<T> taskSource);
}