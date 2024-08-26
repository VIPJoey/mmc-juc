# mmc-juc [V1.0]

利用AI大模型，基于ForkJoinPool封装的多线程库，尤其适合单次长任务场景，例如遍历DB等检索任务，也支持批量小任务，例如kafka消息批量处理等，开箱即用。

## 一、功能特性

* 支持设置线程池并发度
* 支持设置任务池和来源
* 支持控制执行速率
* 支持设置任务处理器
* 支持设置任务监听器



1、引入最新依赖包，如果找不到依赖包，请到工程目录```mvn clean package install```执行一下命令。
```xml
<dependency>
    <groupId>io.github.vipjoey</groupId>
    <artifactId>mmc-juc</artifactId>
    <version>1.0</version>
</dependency>

```


## 二、快速开始

2、同步执行任务。
```java

    MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
        .taskSource(IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()))
        .taskProcessor(x -> x.stream().reduce(0, Integer::sum))
        .taskMerger(Integer::sum)
        .threshold(10)
        .taskName("mmcTaskExample")
        .rateLimiter(10, 20)  // 设置速率限制
        .forkJoinPoolConcurrency(4) // 设置ForkJoinPool的并发度为4
        .build();

    System.out.println("result: " + mmcTaskExecutor.execute());

```

3、异步执行任务。
```java

    MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
        .taskSource(IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList()))
        .taskProcessor(x -> x.stream().reduce(0, Integer::sum))
        .taskMerger(Integer::sum)
        .threshold(10)
        .taskName("mmcTaskExample")
        .rateLimiter(10, 20)  // 设置速率限制
        .forkJoinPoolConcurrency(4) // 设置ForkJoinPool的并发度为4
        .build();

    mmcTaskExecutor.commit((result -> System.out.println(result)));

```

4、多次提交小任务。
```java

public static void main(String[] args) {

        // 从0加到100任务
        List<Integer> taskSource = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            taskSource.add(i);
        }

        // 任务容器
        MmcTaskExecutor<Integer, Integer> mmcTaskExecutor = MmcTaskExecutor.<Integer, Integer>builder()
        .threshold(10)
        .rateLimiter(10, 20)  // 设置速率限制
        .forkJoinPoolConcurrency(4) // 设置ForkJoinPool的并发度为4
        .build();
        
        // 同步提交任务
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
            .build()
        );
        System.out.println("result: " + r);
}

```


## 三、变更记录

* 20240826  v1.0 初始化

## 四、参考文章

* [《《AI大模型编写多线程并发框架（六十一）：从零开始》](https://blog.csdn.net/hanyi_/article/details/133826712?spm=1001.2014.3001.5502)
* [《《AI大模型编写多线程并发框架（六十二）：限流和并发度优化》](https://blog.csdn.net/hanyi_/article/details/133826712?spm=1001.2014.3001.5502)
* [《《AI大模型编写多线程并发框架（六十三）：监听器优化》](https://blog.csdn.net/hanyi_/article/details/133826712?spm=1001.2014.3001.5502)
* [《《AI大模型编写多线程并发框架（六十四）：发布和应用》](https://blog.csdn.net/hanyi_/article/details/133826712?spm=1001.2014.3001.5502)


## 五、特别说明

* 欢迎共建
* 佛系改bug
