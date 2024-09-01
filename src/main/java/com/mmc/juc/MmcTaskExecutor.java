package com.mmc.juc;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import lombok.Setter;

@Setter
public class MmcTaskExecutor<T, R> implements TaskExecutor<T, R> {

    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private ForkJoinPool forkJoinPool;
    private RateLimiter rateLimiter; // 添加令牌桶成员变量
    private MmcTaskListener taskListener;
    private String taskName;

    private MmcTaskExecutor(Builder<T, R> builder) {
        this.taskSource = builder.taskSource;
        this.taskProcessor = builder.taskProcessor;
        this.taskMerger = builder.taskMerger;
        this.threshold = builder.threshold;
        this.forkJoinPool = builder.forkJoinPool;
        this.rateLimiter = builder.rateLimiter;
        this.taskListener = builder.taskListener;
        this.taskName = builder.taskName;
    }

    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }

    // 检查并设置MmcTask的参数
    private void checkAndSet(MmcTask<T, R> mmcTask) {

        // 检查MmcTask的构造参数，如果为空，则使用MmcTaskExecutor的参数
        List<T> taskSource = mmcTask.getTaskSource() != null ? mmcTask.getTaskSource() : this.taskSource;
        MmcTaskProcessor<T, R> taskProcessor = mmcTask.getTaskProcessor() != null ? mmcTask.getTaskProcessor() : this.taskProcessor;
        MmcTaskMerger<R> taskMerger = mmcTask.getTaskMerger() != null ? mmcTask.getTaskMerger() : this.taskMerger;
        RateLimiter rateLimiter = mmcTask.getRateLimiter() != null ? mmcTask.getRateLimiter() : this.rateLimiter;
        MmcTaskListener taskListener = mmcTask.getTaskListener() != null ? mmcTask.getTaskListener() : this.taskListener;
        String taskName = mmcTask.getTaskName() != null ? mmcTask.getTaskName() : this.taskName;
        int threshold = mmcTask.getThreshold() > 0 ? mmcTask.getThreshold() : this.threshold;

        // 如果参数为空，则抛出异常
        Objects.requireNonNull(taskSource, "TaskSource cannot be null.");
        Objects.requireNonNull(taskProcessor, "TaskProcessor cannot be null.");
        Objects.requireNonNull(taskMerger, "TaskMerger cannot be null.");
        Objects.requireNonNull(rateLimiter, "RateLimiter cannot be null.");
        Objects.requireNonNull(taskListener, "TaskListener cannot be null.");
        Objects.requireNonNull(taskName, "TaskName cannot be null.");

        // 将最终参数赋值给MmcTask
        TaskRuntime taskRuntime = new TaskRuntime(mmcTask.getTaskName(), mmcTask.getTaskSource().size());
        mmcTask.setTaskSource(taskSource);
        mmcTask.setTaskProcessor(taskProcessor);
        mmcTask.setTaskMerger(taskMerger);
        mmcTask.setRateLimiter(rateLimiter);
        mmcTask.setTaskListener(taskListener);
        mmcTask.setTaskName(taskName);
        mmcTask.setStart(0);
        mmcTask.setEnd(taskSource.size());
        mmcTask.setTaskRuntime(taskRuntime);
        mmcTask.setThreshold(threshold);
    }

    // 同步执行并返回结果
    @Override
    public R execute() {

        MmcTask<T, R> mmcTask = new MmcTask.Builder<T, R>()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(threshold)
                .rateLimiter(rateLimiter)
                .taskListener(taskListener)
                .taskName(taskName)
                .build();

        return execute(mmcTask);
    }

    // 同步执行并返回结果
    @Override
    public R execute(MmcTask<T, R> mmcTask) {

        // 检查MmcTask的构造参数，如果为空，则使用MmcTaskExecutor的参数
        checkAndSet(mmcTask);

        long startTime = System.currentTimeMillis();
        mmcTask.setTaskRuntime(mmcTask.getTaskRuntime());

        // 调用onTasksSubmitted方法
        taskListener.onTasksSubmitted(mmcTask.getTaskRuntime());

        R result = forkJoinPool.invoke(mmcTask);

        // 调用onTasksCompleted方法
        long elapsedTime = System.currentTimeMillis() - startTime;
        taskListener.onTasksCompleted(mmcTask.getTaskRuntime(), elapsedTime);

        return result;
    }

    // 异步执行
    @Override
    public void commit() {
        commit((result -> {
        }));
    }

    // 异步执行并获取结果
    @Override
    public void commit(MmcTaskCallback<R> callback) {

        MmcTask<T, R> mmcTask = new MmcTask.Builder<T, R>()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(threshold)
                .rateLimiter(rateLimiter)
                .taskListener(taskListener)
                .taskName(taskName)
                .build();

       commit(mmcTask, callback);
    }

    // 异步执行并获取结果
    @Override
    public void commit(MmcTask<T, R> mmcTask, MmcTaskCallback<R> callback) {

        // 检查MmcTask的构造参数，如果为空，则使用MmcTaskExecutor的参数
        checkAndSet(mmcTask);

        long startTime = System.currentTimeMillis();

        // 调用onTasksSubmitted方法
        taskListener.onTasksSubmitted(mmcTask.getTaskRuntime());

        forkJoinPool.submit(() -> {

            R result = mmcTask.invoke();

            // 调用onTasksCompleted方法
            long elapsedTime = System.currentTimeMillis() - startTime;
            taskListener.onTasksCompleted(mmcTask.getTaskRuntime(), elapsedTime);

            if (callback != null) {
                callback.onComplete(result);
            }
        });
    }

    public static class Builder<T, R> {

        private String taskName;
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

        public Builder<T, R> taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder<T, R> forkJoinPoolConcurrency(int concurrency) {
            this.forkJoinPool = new ForkJoinPool(concurrency);
            return this;
        }

        public MmcTaskExecutor<T, R> build() {

            if (null == this.forkJoinPool) {
                this.forkJoinPool = new ForkJoinPool();
            }

            return new MmcTaskExecutor<>(this);
        }
    }
}