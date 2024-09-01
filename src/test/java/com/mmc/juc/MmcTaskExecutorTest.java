package com.mmc.juc;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class MmcTaskExecutorTest {

    @Test
    public void testMmcTaskExecutor() {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个任务处理器，将每个整数相加
        MmcTaskProcessor<Integer, Integer> taskProcessor = (integer) -> integer.stream().mapToInt(Integer::intValue).sum();

        // 创建一个任务合并器，将所有整数的和合并
        MmcTaskMerger<Integer> taskMerger = Integer::sum;

        // 创建一个任务监听器，打印任务执行过程中的相关信息
        MmcTaskListener taskListener = new DefaultMmcTaskListener();

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = new MmcTaskExecutor.Builder<Integer, Integer>()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .taskListener(taskListener)
                .rateLimiter(10, 10) // 每秒处理10个任务
                .taskName("SumTask")
                .build();

        // 执行任务并获取结果
        Integer result = mmcTaskExecutor.execute();

        // 验证结果是否正确（1到100的和等于5050）
        assertEquals(5050, result.intValue());
    }
}