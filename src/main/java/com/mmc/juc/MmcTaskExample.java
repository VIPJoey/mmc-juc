package com.mmc.juc;

import java.util.ArrayList;
import java.util.List;

public class MmcTaskExample {
    public static void main(String[] args) {
        List<Integer> taskSource = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            taskSource.add(i);
        }

        MmcTaskProcessor<Integer, Integer> taskProcessor = new MmcTaskProcessor<Integer, Integer>() {
            @Override
            public Integer process(List<Integer> taskSource) {
                int sum = 0;
                for (Integer num : taskSource) {
                    sum += num;
                }
                return sum;
            }
        };

        MmcTaskMerger<Integer> taskMerger = new MmcTaskMerger<Integer>() {
            @Override
            public Integer merge(Integer leftResult, Integer rightResult) {
                return leftResult + rightResult;
            }
        };

        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .threshold(10)
                .build();

        mmcTaskExecutor.commit(result -> System.out.println("异步执行结果：" + result));  // 输出5050

        // 等待异步任务完成，防止主线程提前退出
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}