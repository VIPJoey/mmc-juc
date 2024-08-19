package com.mmc.juc;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class MmcTaskExecutor<T, R> {
    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private ForkJoinPool forkJoinPool;

    private MmcTaskExecutor(Builder<T, R> builder) {
        this.taskSource = builder.taskSource;
        this.taskProcessor = builder.taskProcessor;
        this.taskMerger = builder.taskMerger;
        this.threshold = builder.threshold;
        this.forkJoinPool = builder.forkJoinPool;
    }

    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }

    // 同步执行并返回结果
    public R execute() {
        MmcTask<T, R> mmcTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, 0, taskSource.size());
        return forkJoinPool.invoke(mmcTask);
    }

    // 异步执行
    public void commit() {
        commit(null);
    }

    // 异步执行并获取结果
    public void commit(MmcTaskCallback<R> callback) {
        MmcTask<T, R> mmcTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, 0, taskSource.size());
        forkJoinPool.submit(() -> {
            R result = mmcTask.invoke();
            if (callback != null) {
                callback.onComplete(result);
            }
        });
    }

    public static class Builder<T, R> {
        private List<T> taskSource;
        private MmcTaskProcessor<T, R> taskProcessor;
        private MmcTaskMerger<R> taskMerger;
        private int threshold = 10;
        private ForkJoinPool forkJoinPool = new ForkJoinPool();

        public Builder<T, R> taskSource(List<T> taskSource) {
            this.taskSource = taskSource;
            return this;
        }

        public Builder<T, R> taskProcessor(MmcTaskProcessor<T, R> taskProcessor) {
            this.taskProcessor = taskProcessor;
            return this;
        }

        public Builder<T, R> taskMerger(MmcTaskMerger<R> taskMerger) {
            this.taskMerger = taskMerger;
            return this;
        }

        public Builder<T, R> threshold(int threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder<T, R> forkJoinPool(ForkJoinPool forkJoinPool) {
            this.forkJoinPool = forkJoinPool;
            return this;
        }

        public MmcTaskExecutor<T, R> build() {
            return new MmcTaskExecutor<>(this);
        }
    }
}