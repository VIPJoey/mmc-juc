package com.mmc.juc;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
public class MmcTask<T, R> extends RecursiveTask<R> {

    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private int start;
    private int end;
    private RateLimiter rateLimiter;
    private MmcTaskListener taskListener;
    private String taskName;
    private TaskRuntime taskRuntime;

    private MmcTask(Builder<T, R> builder) {
        this.taskSource = builder.taskSource;
        this.taskProcessor = builder.taskProcessor;
        this.taskMerger = builder.taskMerger;
        this.threshold = builder.threshold;
        this.start = builder.start;
        this.end = builder.end;
        this.rateLimiter = builder.rateLimiter;
        this.taskListener = builder.taskListener;
        this.taskName = builder.taskName;
        this.taskRuntime = builder.taskRuntime;
    }

    public static <T, R> Builder<T, R> builder() {
        return new Builder<>();
    }

    @Data
    @Accessors(fluent = true)
    public static class Builder<T, R> {

        private List<T> taskSource;
        private MmcTaskProcessor<T, R> taskProcessor;
        private MmcTaskMerger<R> taskMerger;
        private int threshold;
        private int start;
        private int end;
        private RateLimiter rateLimiter;
        private MmcTaskListener taskListener;
        private AtomicInteger completedTasks;
        private String taskName;
        private TaskRuntime taskRuntime;

        public MmcTask<T, R> build() {
            return new MmcTask<>(this);
        }
    }

    @Override
    protected R compute() {

        // 在处理任务之前尝试消耗令牌
        while (!rateLimiter.tryConsume()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (end - start <= threshold) {

            R result = taskProcessor.process(taskSource.subList(start, end));

            // 在任务完成后更新已完成任务的计数
            taskRuntime.getCompletedTasks().addAndGet(end - start);
            taskRuntime.getRemainingTasks().set(taskRuntime.getTotalTasks() - taskRuntime.getCompletedTasks().get());

            // 调用onTaskStarted方法，以便在每个小任务完成时更新已完成任务的计数
            taskListener.onTaskStarted(taskRuntime);

            return result;
        }

        int middle = (start + end) / 2;
        MmcTask<T, R> leftTask = new MmcTask.Builder<T, R>()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(threshold)
                .start(start)
                .end(middle)
                .rateLimiter(rateLimiter)
                .taskListener(taskListener)
                .taskName(taskName)
                .taskRuntime(taskRuntime)
                .build();

        MmcTask<T, R> rightTask = new MmcTask.Builder<T, R>()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(threshold)
                .start(middle)
                .end(end)
                .rateLimiter(rateLimiter)
                .taskListener(taskListener)
                .taskName(taskName)
                .taskRuntime(taskRuntime)
                .build();

        leftTask.fork();
        R rightResult = rightTask.compute();
        R leftResult = leftTask.join();

        if (taskMerger != null) {
            return taskMerger.merge(leftResult, rightResult);
        } else {
            return null;
        }
    }
}