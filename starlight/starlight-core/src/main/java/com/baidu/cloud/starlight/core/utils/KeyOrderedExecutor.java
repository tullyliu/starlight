/*
 * Copyright (c) 2019 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.baidu.cloud.starlight.core.utils;

import com.baidu.cloud.starlight.api.rpc.threadpool.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同一个key的任务，会按照顺序执行的线程池
 */
public class KeyOrderedExecutor {
    private int threadNums;

    private List<ExecutorService> executorServiceList = new ArrayList<>();
    private static final int DEFAULT_THREAD_NUMS = 16;

    private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 2048;

    public KeyOrderedExecutor(String threadNamePrefix) {
        this(DEFAULT_THREAD_NUMS, DEFAULT_BLOCKING_QUEUE_SIZE, threadNamePrefix);
    }

    public KeyOrderedExecutor(int threadNums, int blockingQueueSize, String threadNamePrefix) {
        if (threadNums <= 0) {
            throw new IllegalArgumentException("illegal threadNums");
        }
        if (blockingQueueSize <= 0) {
            throw new IllegalArgumentException("illegal blockingQueueSize");
        }
        if (threadNamePrefix == null) {
            throw new IllegalArgumentException("illegal threadNamePrefix");
        }
        this.threadNums = threadNums;
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory(threadNamePrefix);

        for (int i = 0; i < threadNums; i++) {
            executorServiceList.add(
                // 要保证有序，必须是单线程的
                new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(blockingQueueSize),
                    namedThreadFactory));
        }
    }

    public void execute(long key, Runnable command) {
        int solt = (int) (key % executorServiceList.size());
        executorServiceList.get(solt).execute(command);
    }

}
