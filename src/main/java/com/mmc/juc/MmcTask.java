package com.mmc.juc;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class MmcTask<T, R> extends RecursiveTask<R> {
    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private int start;
    private int end;
    private RateLimiter rateLimiter;

    public MmcTask(List<T> taskSource, MmcTaskProcessor<T, R> taskProcessor, MmcTaskMerger<R> taskMerger, int threshold, int start, int end, RateLimiter rateLimiter) {
        this.taskSource = taskSource;
        this.taskProcessor = taskProcessor;
        this.taskMerger = taskMerger;
        this.threshold = threshold;
        this.start = start;
        this.end = end;
        this.rateLimiter = rateLimiter;
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
            return taskProcessor.process(taskSource.subList(start, end));
        }

        int middle = (start + end) / 2;
        MmcTask<T, R> leftTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, start, middle,
                rateLimiter);
        MmcTask<T, R> rightTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, middle, end,
                rateLimiter);

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