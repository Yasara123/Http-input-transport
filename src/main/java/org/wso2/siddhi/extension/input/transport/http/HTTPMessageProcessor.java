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

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPMessageProcessor implements CarbonMessageProcessor {
    private SourceEventListener sourceEventListener;
    private ExecutorService executorService;
    private ClientConnector clientConnector;
    public static final String TEST_VALUE = "Test Message from Yasara";
    public HTTPMessageProcessor(SourceEventListener sourceEventListener, int threadPoolSize) {
        this.sourceEventListener = sourceEventListener;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    public HTTPMessageProcessor(){

    }
    public HTTPMessageProcessor getInstance(){
        return this;
    }
    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        //executorService.submit(new HTTPWorkerThread(carbonMessage, carbonCallback, sourceEventListener));
     //   System.out.println(carbonMessage.getMessageBody());
      //DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
       // DefaultCarbonMessage defaultCarbonMessage = (DefaultCarbonMessage) carbonMessage;
       /* if (carbonMessage instanceof TextCarbonMessage) {
            String event = ((TextCarbonMessage) carbonMessage).getText();
            sourceEventListener.onEvent(event);
        } else if (carbonMessage instanceof MapCarbonMessage) {
            Map<String, String> event = new HashMap<>();
            Enumeration<String> mapNames = ((MapCarbonMessage) carbonMessage).getMapNames();
            while (mapNames.hasMoreElements()) {
                String key = mapNames.nextElement();
                event.put(key, ((MapCarbonMessage) carbonMessage).getValue(key));
            }
            sourceEventListener.onEvent(event);
        } else {
            System.out.print("The message type of the HTTP message is not supported!");
        }*/
       // System.out.println(event);
       /* carbonMessage.getProperties().forEach(defaultCarbonMessage::setProperty);
        defaultCarbonMessage.setHeader("content-type", "text/plain");
        defaultCarbonMessage.setStringMessageBody(TEST_VALUE);
        defaultCarbonMessage.setEndOfMsgAdded(true);
        int length = TEST_VALUE.getBytes().length;
        defaultCarbonMessage.setHeader("content-length", String.valueOf(length));
        carbonCallback.done(defaultCarbonMessage);*/
        carbonCallback.done(carbonMessage);
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
        this.clientConnector = clientConnector;
    }

    @Override
    public String getId() {
        return "HTTP-message-processor";
    }

    public void disconnect() throws InterruptedException {
        executorService.shutdown();
    }
}
