package com.mmc.juc;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class MmcTaskExecutorTest {

    @Test
    public void testMmcTaskExecutor() {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个任务处理器，将每个整数相加
        MmcTaskProcessor<Integer, Integer> taskProcessor = (integer) -> integer.stream().mapToInt(Integer::intValue)
                .sum();

        // 创建一个任务合并器，将所有整数的和合并
        MmcTaskMerger<Integer> taskMerger = Integer::sum;

        // 创建一个任务监听器，打印任务执行过程中的相关信息
        MmcTaskListener taskListener = new DefaultMmcTaskListener();

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .taskListener(taskListener)
                .rateLimiter(10, 10) // 每秒处理10个任务
                .taskName("testMmcTaskExecutor")
                .build();

        // 执行任务并获取结果
        Integer result = mmcTaskExecutor.execute();

        // 验证结果是否正确（1到100的和等于5050）
        assertEquals(5050, result.intValue());
    }

    @Test
    public void testMmcTaskExecutorWithTask() {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskProcessor(x -> x.stream().reduce(0, Integer::sum))
                .taskMerger(Integer::sum)
                .rateLimiter(new TokenBucket(10, 20))
                .taskListener(new DefaultMmcTaskListener())
                .build();

        Integer r = mmcTaskExecutor.execute(MmcTask.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskName("testMmcTaskExecutorWithTask")
                .build()
        );
        System.out.println("result: " + r);
    }

    @Test
    public void testCommit() throws InterruptedException {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个任务处理器，将每个整数相加
        MmcTaskProcessor<Integer, Integer> taskProcessor = (integer) -> integer.stream().mapToInt(Integer::intValue)
                .sum();

        // 创建一个任务合并器，将所有整数的和合并
        MmcTaskMerger<Integer> taskMerger = Integer::sum;

        // 创建一个任务监听器，打印任务执行过程中的相关信息
        MmcTaskListener taskListener = new DefaultMmcTaskListener();

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .taskListener(taskListener)
                .rateLimiter(10, 10) // 每秒处理10个任务
                .taskName("testCommit")
                .build();

        mmcTaskExecutor.commit();
        TimeUnit.SECONDS.sleep(3); // 等待异步任务完成
    }

    @Test
    public void testCommitWithCallback() throws InterruptedException {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个任务处理器，将每个整数相加
        MmcTaskProcessor<Integer, Integer> taskProcessor = (integer) -> integer.stream().mapToInt(Integer::intValue)
                .sum();

        // 创建一个任务合并器，将所有整数的和合并
        MmcTaskMerger<Integer> taskMerger = Integer::sum;

        // 创建一个任务监听器，打印任务执行过程中的相关信息
        MmcTaskListener taskListener = new DefaultMmcTaskListener();

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskSource(taskSource)
                .taskProcessor(taskProcessor)
                .taskMerger(taskMerger)
                .taskListener(taskListener)
                .rateLimiter(10, 10) // 每秒处理10个任务
                .taskName("testCommitWithCallback")
                .build();

        mmcTaskExecutor.commit(result -> {
            assertEquals(5050, result.intValue());
        });
    }

    @Test
    public void testCommitWithCustomMmcTaskAndCallback() throws InterruptedException {

        // 创建一个任务源，包含从1到100的整数
        List<Integer> taskSource = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());

        // 创建一个MmcTaskExecutor实例
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
                .taskProcessor(x -> x.stream().reduce(0, Integer::sum))
                .taskMerger(Integer::sum)
                .taskListener(new DefaultMmcTaskListener())
                .build();

        mmcTaskExecutor.commit(MmcTask.<Integer, Integer>builder()
                        .taskName("testCommitWithCustomMmcTaskAndCallback")
                        .rateLimiter(new TokenBucket(20, 20))
                        .taskSource(taskSource)
                        .build()
                , result -> {
                    assertEquals(5050, result.intValue());
                    System.out.println("result: " + result);
                });
    }
}