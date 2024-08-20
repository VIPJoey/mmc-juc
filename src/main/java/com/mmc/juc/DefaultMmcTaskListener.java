package com.mmc.juc;

public class DefaultMmcTaskListener implements MmcTaskListener {
    @Override
    public void onTasksSubmitted(String taskName, int totalTasks, long startTime) {
        System.out.println("[" + taskName + "] Tasks submitted. Total tasks: " + totalTasks + ", start time: " + startTime);
    }

    @Override
    public void onTaskStarted(String taskName, int completedTasks, int remainingTasks) {
        System.out.println("[" + taskName + "] Task started. Completed tasks: " + completedTasks + ", remaining tasks: " + remainingTasks);
    }

    @Override
    public void onTasksCompleted(String taskName, long elapsedTime, long endTime) {
        System.out.println("[" + taskName + "] Tasks completed. Elapsed time: " + elapsedTime + " ms, end time: " + endTime);
    }
}