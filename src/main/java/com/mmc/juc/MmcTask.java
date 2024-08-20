package com.mmc.juc;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MmcTask<T, R> extends RecursiveTask<R> {
    private List<T> taskSource;
    private MmcTaskProcessor<T, R> taskProcessor;
    private MmcTaskMerger<R> taskMerger;
    private int threshold;
    private int start;
    private int end;
    private RateLimiter rateLimiter;
    private MmcTaskListener taskListener;
    private AtomicInteger completedTasks;

    public MmcTask(List<T> taskSource, MmcTaskProcessor<T, R> taskProcessor, MmcTaskMerger<R> taskMerger, int threshold, int start, int end, RateLimiter rateLimiter, MmcTaskListener taskListener, AtomicInteger completedTasks) {
        this.taskSource = taskSource;
        this.taskProcessor = taskProcessor;
        this.taskMerger = taskMerger;
        this.threshold = threshold;
        this.start = start;
        this.end = end;
        this.rateLimiter = rateLimiter;
        this.taskListener = taskListener;
        this.completedTasks = completedTasks;

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

            // 在处理任务之前调用onTaskStarted方法
            taskListener.onTaskStarted(completedTasks.get(), end - start);

            R result = taskProcessor.process(taskSource.subList(start, end));

            // 在任务完成后更新已完成任务的计数
            int completed = completedTasks.addAndGet(end - start);

            // 调用onTaskStarted方法，以便在每个小任务完成时更新已完成任务的计数
            taskListener.onTaskStarted(completed, end - start - completed);

            return result;
        }

        int middle = (start + end) / 2;
        MmcTask<T, R> leftTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, start, middle,
                rateLimiter, taskListener, completedTasks);
        MmcTask<T, R> rightTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, middle, end,
                rateLimiter, taskListener, completedTasks);

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