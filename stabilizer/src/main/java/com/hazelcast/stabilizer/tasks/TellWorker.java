/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.stabilizer.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.stabilizer.agent.Agent;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TellWorker implements Callable, Serializable, HazelcastInstanceAware {
    private final static ILogger log = Logger.getLogger(TellWorker.class);

    private transient HazelcastInstance hz;
    private final Callable task;
    private final long timeoutSec;

    public TellWorker(Callable task) {
        this(task, 60);
    }

    public TellWorker(Callable task, long timeoutSec) {
        this.task = task;
        this.timeoutSec = timeoutSec;
    }

    @Override
    public Object call() throws Exception {
        try {
            Agent agent = (Agent) hz.getUserContext().get(Agent.KEY_AGENT);
            IExecutorService executor = agent.getWorkerVmManager().getWorkerExecutor();
            Future future = executor.submit(task);
            return future.get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.severe("Failed to spawn Worker Virtual Machines", e);
            throw e;
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }
}