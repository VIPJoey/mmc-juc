package com.mmc.juc;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class TaskRuntime {
    private String taskName;
    private int totalTasks;
    private AtomicInteger completedTasks;
    private AtomicInteger remainingTasks;

    public TaskRuntime(String taskName, int totalTasks) {
        this.taskName = taskName;
        this.totalTasks = totalTasks;
        this.completedTasks = new AtomicInteger();
        this.remainingTasks = new AtomicInteger(totalTasks);
    }

}