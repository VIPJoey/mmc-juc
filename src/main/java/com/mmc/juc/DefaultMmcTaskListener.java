package com.mmc.juc;

public class DefaultMmcTaskListener implements MmcTaskListener {


    @Override
    public void onTasksSubmitted(TaskRuntime taskRuntime) {
        System.out.println(
                "[" + taskRuntime.getTaskName() + "] Tasks submitted. Total tasks: " + taskRuntime.getTotalTasks());

    }

    @Override
    public void onTaskStarted(TaskRuntime taskRuntime) {
        System.out.println(
                "[" + taskRuntime.getTaskName() + "] Task started. Completed tasks: " + taskRuntime.getCompletedTasks()
                        .get() + ", remaining tasks: " + taskRuntime.getRemainingTasks().get());

    }

    @Override
    public void onTasksCompleted(TaskRuntime taskRuntime, long elapsedTime) {
        System.out.println(
                "[" + taskRuntime.getTaskName() + "] Tasks completed. Elapsed time: " + elapsedTime + " ms ");

    }
}