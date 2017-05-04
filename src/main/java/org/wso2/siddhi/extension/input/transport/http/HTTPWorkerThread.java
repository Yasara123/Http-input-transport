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
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.extension.input.transport.http.Util.ServerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class HTTPWorkerThread implements Runnable {
    private CarbonMessage carbonMessage;
    private CarbonCallback carbonCallback;
    private SourceEventListener sourceEventListener;
    private ClientConnector clientConnector;
    private static final Logger logger = LoggerFactory.getLogger(HTTPWorkerThread.class);
    public HTTPWorkerThread(CarbonMessage cMessage, CarbonCallback cCallback, SourceEventListener sourceEventListener, ClientConnector clientConnector) {
        this.carbonMessage = cMessage;
        this.carbonCallback = cCallback;
        this.sourceEventListener = sourceEventListener;
        this.clientConnector=clientConnector;
    }

    @Override
    public void run() {
        try {
            if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null && carbonMessage
                    .getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                    .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
                InputStream inputStream = carbonMessage.getInputStream();
                String response = new String(ByteStreams.toByteArray(inputStream), Charset.defaultCharset());
                String alteredContent = "Altered " + response + " content";
                sourceEventListener.onEvent(response);
                DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
                defaultCarbonMessage.setStringMessageBody(alteredContent);
                carbonCallback.done(defaultCarbonMessage);
            } else {
                carbonMessage.setProperty(Constants.HOST, ServerUtil.TEST_HOST);
                carbonMessage.setProperty(Constants.PORT, ServerUtil.TEST_SERVER_PORT);
                clientConnector.send(carbonMessage, carbonCallback);
            }
        } catch (IOException e) {
            logger.error("Error cast i to byte stream  ", e);
        } catch (ClientConnectorException e) {
            e.printStackTrace();
        }

     /*   if (carbonMessage instanceof TextCarbonMessage) {
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
            throw new HTTPInputAdaptorRuntimeException("The message type of the HTTP message is not supported!");
        }
        if (carbonCallback != null) {
            carbonCallback.done(carbonMessage);
        }*/
    }
}
