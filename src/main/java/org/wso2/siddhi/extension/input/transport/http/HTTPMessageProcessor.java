/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.siddhi.extension.input.transport.http;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientConnector;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.extension.input.transport.http.Util.PausableThreadPoolExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HTTPMessageProcessor implements CarbonMessageProcessor {
    private SourceEventListener sourceEventListener;
    private PausableThreadPoolExecutor executorService;
    private ClientConnector clientConnector;
    private LinkedBlockingQueue<Runnable> queue;
    private static final Logger logger = LoggerFactory.getLogger(HTTPMessageProcessor.class);
    private final long KEEP_ALIVE_TIME = 10;
    private final int MAX_THREAD_POOL_SIZE_MULTIPLIER = 2;

    public HTTPMessageProcessor(SourceEventListener sourceEventListener, int threadPoolSize) {
        this.sourceEventListener = sourceEventListener;
        int maxThreadPoolSize = MAX_THREAD_POOL_SIZE_MULTIPLIER *  threadPoolSize;
        this.queue = new LinkedBlockingQueue<>();
        this.executorService = new PausableThreadPoolExecutor( threadPoolSize, maxThreadPoolSize, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, queue);
        this.clientConnector= new HTTPClientConnector();
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        executorService.execute(new HTTPWorkerThread(carbonMessage, carbonCallback, sourceEventListener,clientConnector));

        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {

    }

    @Override
    public String getId()
    {
        return "HTTP-message-processor";
    }

    void pause() {
        executorService.pause();
    }

    void resume()
    {
        executorService.resume();
    }

    public void clear() {
        queue.clear();
    }

    public boolean isEmpty()
    {
        return queue.isEmpty();
    }

    void disconnect() {
        executorService.shutdown();
    }
}
