package com.mmc.juc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MmcTaskExample {

    public static void main(String[] args) {

        List<Integer> taskSource = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            taskSource.add(i);
        }

        // 从0加到100任务
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList())) // 设置任务源
                .taskProcessor(x -> x.stream().reduce(0, Integer::sum)) // 设置任务处理方法
                .taskMerger(Integer::sum) // 设置结果处理方法（可选）
                .threshold(10) // 设置任务处理阈值（可选）
                .taskName("mmcTaskExample") // 设置任务名称
                .rateLimiter(10, 20)  // 设置速率限制
                .forkJoinPoolConcurrency(4) // 设置ForkJoinPool的并发度为4
                .build();

        System.out.println("result: " + mmcTaskExecutor.execute());

        Integer r = mmcTaskExecutor.execute(MmcTask.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(x -> x.stream().reduce(0, Integer::sum))
                .taskMerger(Integer::sum)
                .rateLimiter(new TokenBucket(10, 20))
                .taskListener(new DefaultMmcTaskListener())
                .threshold(10)
                .start(0)
                .end(taskSource.size())
                .taskName("taskName")
                .taskRuntime(new TaskRuntime("taskName", taskSource.size()))
                .build()
        );
        System.out.println("result: " + r);
    }
}