package com.mmc.juc;

import java.util.ArrayList;
import java.util.List;

public class MmcTaskExample {

    public static void main(String[] args) {

        List<Integer> taskSource = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            taskSource.add(i);
        }

        MmcTaskProcessor<Integer, Integer> taskProcessor = taskSource1 -> {
            int sum = 0;
            for (Integer num : taskSource1) {
                sum += num;
            }
            return sum;
        };

        MmcTaskMerger<Integer> taskMerger = Integer::sum;

        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(10)
                .taskName("mmcTaskExample")
                .rateLimiter(10, 20)  // 设置速率限制
                .forkJoinPoolConcurrency(4) // 设置ForkJoinPool的并发度为4
                .build();

        System.out.println("result: " + mmcTaskExecutor.execute());
    }
}