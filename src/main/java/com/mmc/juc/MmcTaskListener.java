package com.mmc.juc;

public interface MmcTaskListener {

    void onTasksSubmitted(int totalTasks, long startTime);

    void onTaskStarted(int completedTasks, int remainingTasks);

    void onTasksCompleted(long elapsedTime, long endTime);
}