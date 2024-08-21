package com.mmc.juc;

public interface MmcTaskListener {
    void onTasksSubmitted(TaskRuntime taskRuntime);

    void onTaskStarted(TaskRuntime taskRuntime);

    void onTasksCompleted(TaskRuntime taskRuntime, long elapsedTime);

}