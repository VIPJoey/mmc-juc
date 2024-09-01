/*
 * Copyright (c) 2010-2030 Founder Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Founder. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Founder.
 *
 */

package com.mmc.juc;

/**
 * TaskExecutor.
 *
 * @author tenkye
 * @date 2024/9/1 16:16
 */
public interface TaskExecutor<T, R> {

    // 同步执行并返回结果
    R execute();

    // 同步执行并返回结果
    R execute(MmcTask<T, R> mmcTask);

    // 异步执行
    void commit();

    // 异步执行并获取结果
    void commit(MmcTaskCallback<R> callback);

    // 异步执行并获取结果
    void commit(MmcTask<T, R> mmcTask, MmcTaskCallback<R> callback);
}
