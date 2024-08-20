package com.mmc.juc;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class MmcTaskExecutor<T, R> {
    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private ForkJoinPool forkJoinPool;
    private RateLimiter rateLimiter; // 添加令牌桶成员变量
    private MmcTaskListener taskListener;
    private AtomicInteger completedTasks = new AtomicInteger(); // 添加一个原子整数以跟踪已完成的任务数量

    private MmcTaskExecutor(Builder<T, R> builder) {
        this.taskSource = builder.taskSource;
        this.taskProcessor = builder.taskProcessor;
        this.taskMerger = builder.taskMerger;
        this.threshold = builder.threshold;
        this.forkJoinPool = builder.forkJoinPool;
        this.rateLimiter = builder.rateLimiter;
        this.taskListener = builder.taskListener;

    }

    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }

    // 同步执行并返回结果
    public R execute() {

        long startTime = System.currentTimeMillis();
        completedTasks.set(0); // 重置已完成任务的计数器

        MmcTask<T, R> mmcTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, 0, taskSource.size(),
                rateLimiter, taskListener, completedTasks);

        // 调用onTasksSubmitted方法
        taskListener.onTasksSubmitted(taskSource.size(), System.currentTimeMillis());

        R result = forkJoinPool.invoke(mmcTask);

        // 调用onTasksCompleted方法
        long elapsedTime = System.currentTimeMillis() - startTime;
        taskListener.onTasksCompleted(elapsedTime, System.currentTimeMillis());

        return result;
    }

    // 异步执行
    public void commit() {
        commit(null);
    }

    // 异步执行并获取结果
    public void commit(MmcTaskCallback<R> callback) {

        long startTime = System.currentTimeMillis();
        completedTasks.set(0); // 重置已完成任务的计数器

        MmcTask<T, R> mmcTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, 0, taskSource.size(),
                rateLimiter, taskListener, completedTasks);

        // 调用onTasksSubmitted方法
        taskListener.onTasksSubmitted(taskSource.size(), System.currentTimeMillis());

        forkJoinPool.submit(() -> {

            R result = mmcTask.invoke();

            // 调用onTasksCompleted方法
            long elapsedTime = System.currentTimeMillis() - startTime;
            taskListener.onTasksCompleted(elapsedTime, System.currentTimeMillis());

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
        private RateLimiter rateLimiter = new TokenBucket(10, 1); // 默认使用TokenBucket
        private MmcTaskListener taskListener = new DefaultMmcTaskListener(); // 默认使用DefaultMmcTaskListener

        public Builder<T, R> taskSource(List<T> taskSource) {
            this.taskSource = taskSource;
            return this;
        }

        public Builder<T, R> taskListener(MmcTaskListener taskListener) {
            this.taskListener = taskListener;
            return this;
        }

        public Builder<T, R> rateLimiter(long capacity, long tokensPerSecond) {
            this.rateLimiter = new TokenBucket(capacity, tokensPerSecond);
            return this;
        }

        public Builder<T, R> rateLimiter(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
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

        public Builder<T, R> forkJoinPoolConcurrency(int concurrency) {
            this.forkJoinPool = new ForkJoinPool(concurrency);
            return this;
        }

        public MmcTaskExecutor<T, R> build() {
            return new MmcTaskExecutor<>(this);
        }
    }
}