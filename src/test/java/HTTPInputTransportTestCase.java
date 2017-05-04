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

import io.netty.handler.codec.http.HttpMethod;
import junit.framework.Assert;
import org.junit.Test;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.input.transport.http.Util.ServerUtil;
import org.wso2.siddhi.extension.input.transport.http.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HTTPInputTransportTestCase {
    private List<String> receivedEventNameList;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9005));

    @Test
    public void TestHTTPTopicInputTransport() throws InterruptedException {
        receivedEventNameList = new ArrayList<>(2);
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "" +
                "@source(type='http', @map(type='text'), "
                + "HOST='localhost', "
                + "PORT='9005',"
                + "METHOD='POST', "
                + "PATH='/'"
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

        // publishing events
        List<String> expected = new ArrayList<>(2);
        expected.add("John");
        expected.add("Mike");
        String event1 ="John,26,USA";
        String event2 ="Mike,32,Germany";
        HTTPServer httpServer=ServerUtil.startHTTPServer(9050);
        publishEvent( event1);
        publishEvent(event2);
       Assert.assertEquals("HTTP Input Transport expected input not received", expected, receivedEventNameList);
       ServerUtil.shutDownHttpServer(httpServer);
       executionPlanRuntime.shutdown();

    }
public void publishEvent(String event){
    try {

        HttpURLConnection urlConn = null;

        try {
            urlConn = ServerUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
        } catch (IOException e) {
            ServerUtil.handleException("IOException occurred while running the HTTPInputTransportTestCase", e);
        }
        ServerUtil.writeContent(urlConn, event) ;
        assertEquals(200, urlConn.getResponseCode());
        String content2 = urlConn.getResponseMessage();
        String content = ServerUtil.getContent(urlConn);
       // urlConn.disconnect();
    } catch (IOException e) {
        ServerUtil.handleException("IOException occurred while running the HTTPInputTransportTestCase", e);
    }
}
}
