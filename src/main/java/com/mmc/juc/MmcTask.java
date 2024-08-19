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

    public MmcTask(List<T> taskSource, MmcTaskProcessor<T, R> taskProcessor, MmcTaskMerger<R> taskMerger, int threshold, int start, int end) {
        this.taskSource = taskSource;
        this.taskProcessor = taskProcessor;
        this.taskMerger = taskMerger;
        this.threshold = threshold;
        this.start = start;
        this.end = end;
    }

    @Override
    protected R compute() {
        if (end - start <= threshold) {
            return taskProcessor.process(taskSource.subList(start, end));
        }

        int middle = (start + end) / 2;
        MmcTask<T, R> leftTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, start, middle);
        MmcTask<T, R> rightTask = new MmcTask<>(taskSource, taskProcessor, taskMerger, threshold, middle, end);

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