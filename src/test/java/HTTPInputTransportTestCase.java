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

import http.BalCallback;
import http.HTTPClient2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.Charsets;
import org.junit.Test;
import org.wso2.carbon.connector.framework.ConnectorManager;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.input.transport.http.HTTPInputTransport;
import org.wso2.siddhi.extension.input.transport.http.HTTPMessageProcessor;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HTTPInputTransportTestCase {
    private List<String> receivedEventNameList;
    private final String PROVIDER_URL = "vm://localhost?broker.persistent=false";

    @Test
    public void TestHTTPTopicInputTransport() throws InterruptedException {
        receivedEventNameList = new ArrayList<>(2);

        // starting the ActiveMQ broker
      //  ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(PROVIDER_URL);

        // deploying the execution plan
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "@source(type='http', @map(type='text'), "
                + "factoryInitial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                + "providerUrl='vm://localhost',"
                + "destination='DAS_HTTP_TEST', "
                + "connectionFactoryType='topic',"
                + "connectionFactoryJNDIName='QueueConnectionFactory',"
                + "transport.jms.SubscriptionDurable='true', "
                + "transport.jms.DurableSubscriberClientID='wso2dasclient1'"
                +")" +
                "define stream inputStream (name string, age int, country string);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    receivedEventNameList.add(event.getData(0).toString());
                }
            }
        });
        executionPlanRuntime.start();
        /*DefaultCarbonMessage defaultCarbonMessage= new DefaultCarbonMessage();
        defaultCarbonMessage.setHeader("content-type", "text/plain");
        defaultCarbonMessage.setProperty( "HTTP_METHOD", "POST");
        defaultCarbonMessage.setProperty("HOST","localhost");
        defaultCarbonMessage.setProperty("PROTOCOL","http");
        defaultCarbonMessage.setProperty("PORT","8420");
        defaultCarbonMessage.setProperty("TO","https://localhost:8420");
        defaultCarbonMessage.setStringMessageBody("sending test value");
        defaultCarbonMessage.setEndOfMsgAdded(true);
        int length = "sending test value".getBytes().length;
        defaultCarbonMessage.setHeader("content-length", String.valueOf(length));
        //carbonCallback.done(defaultCarbonMessage);
        ConnectorManager clientConnectoManger=new ConnectorManager();
       HTTPMessageProcessor mp= new HTTPMessageProcessor().getInstance();
        clientConnectoManger.initializeClientConnectors(mp);
        org.wso2.carbon.messaging.ClientConnector clientConnector = clientConnectoManger.getClientConnector("http");
        mp.setClientConnector(clientConnector);;
        //clientConnector.setMessageProcessor(defaultCarbonMessage,new HTTPMessageProcessor(this,1));
        try {
            clientConnector.send(defaultCarbonMessage,new BalCallback());
        } catch (ClientConnectorException e) {
            e.printStackTrace();
        }
*/
        HTTPClient2 httpClient = null;
        try {
            httpClient = new HTTPClient2(false, "localhost", 8420);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String json ="This is the value I send";
       // FullHttpRequest req =new FullHttpRequest();
        ByteBuf buf= Unpooled.wrappedBuffer(json.getBytes(Charsets.UTF_8));
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/",buf);
       // String json = HTTPMessageProcessor.TEST_VALUE;

        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        request.headers().set(HttpHeaderNames.ACCEPT, "text/plain");
        ByteBuf buffer = request.content().clear();
        int p0 = buffer.writerIndex();
        buffer.writeBytes(json.getBytes());
        int p1 = buffer.writerIndex();
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
        int send = 0;
        try {
            send = httpClient.send(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = httpClient.getResponse(send);
        System.out.println(response);
/*
        // publishing events
        publishEvents("DAS_JMS_TEST", null, "activemq", "text", "src/test/resources/events/events_text.txt");

        List<String> expected = new ArrayList<>(2);
        expected.add("\nJohn");
        expected.add("\nMike");
        Assert.assertEquals("JMS Input Transport expected input not received", expected, receivedEventNameList);*/
    }
/*
    private void publishEvents(String topicName, String queueName, String broker, String format, String filePath)
            throws InterruptedException {
        JMSClient jmsClient = new JMSClient();
        jmsClient.sendJMSEvents(filePath, topicName, queueName, format, broker, PROVIDER_URL);
        Thread.sleep(5000);
    }*/
}
