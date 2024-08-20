package com.mmc.juc;

public interface MmcTaskListener {
    void onTasksSubmitted(String taskName, int totalTasks, long startTime);

    void onTaskStarted(String taskName, int completedTasks, int remainingTasks);

    void onTasksCompleted(String taskName, long elapsedTime, long endTime);
}