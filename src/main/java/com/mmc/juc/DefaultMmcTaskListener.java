package com.mmc.juc;

public class DefaultMmcTaskListener implements MmcTaskListener {

    @Override
    public void onTasksSubmitted(int totalTasks, long startTime) {
        System.out.println("Tasks submitted. Total tasks: " + totalTasks + ", start time: " + startTime);
    }

    @Override
    public void onTaskStarted(int completedTasks, int remainingTasks) {
        System.out.println("Task started. Completed tasks: " + completedTasks + ", remaining tasks: " + remainingTasks);
    }

    @Override
    public void onTasksCompleted(long elapsedTime, long endTime) {
        System.out.println("Tasks completed. Elapsed time: " + elapsedTime + " ms, end time: " + endTime);
    }
}