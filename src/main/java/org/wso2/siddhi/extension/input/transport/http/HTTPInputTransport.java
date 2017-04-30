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


import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.*;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.List;
import java.util.Map;
import org.wso2.siddhi.extension.input.transport.http.server.HTTPServer;

@Extension(
        name = "http",
        namespace = "inputtransport",
        description = ""
)
public class HTTPInputTransport extends InputTransport {
    private static final Logger log = Logger.getLogger(HTTPInputTransport.class);
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private int threadPoolSize;
    private final int DEFAULT_THREAD_POOL_SIZE = 1;
    HTTPServerConnector  httpServerConnector;
    public HTTPMessageProcessor httpMessageProcessor;
    private HTTPServer httpServer;
    private List<HTTPServerConnector> serverConnector2;
    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        // todo: thread pool size should be read from the configuration file, since it's not available at the time of
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        httpMessageProcessor = new HTTPMessageProcessor(sourceEventListener, threadPoolSize);
       TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/http2/netty-transports.yml");
        serverConnector2 =  TestUtil.startConnectors(configuration, httpMessageProcessor);

        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT,httpMessageProcessor.TEST_VALUE,Constants
                .TEXT_PLAIN);


    }

    @Override
    public void disconnect() {
        try {
            httpServerConnector.stop();
            httpMessageProcessor.disconnect();
        } catch (NullPointerException e) {//find
            log.error("Error disconnecting the HTTP receiver", e);
        } catch (Exception e) {//find
            log.error("Error shutting down the threads for the JMS Message Processor", e);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void pause() {
        //todo: implement this
    }

    @Override
    public void resume() {
        //todo: implement this
    }

    /**
     * Initializing HTTP properties.
     * The properties in the required options list are mandatory.
     * Other HTTP options can be passed in as key value pairs, key being in the HTTP spec or the broker spec.
     * @return all the options map.
     */
    private Map<String, String> initHTTPProperties() {

        return null;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // no state to restore
    }
    /**
     * Initializing HTTP properties.
     * The properties in the required options list are mandatory.
     * Other HTTP options can be passed in as key value pairs, key being in the JMS spec or the broker spec.
     * @return all the options map.
     */
    /*
    private Map<String, String> initJMSProperties() {
        List<String> requiredOptions = Arrays.asList(Constants.DESTINATION_PARAM_NAME,
                Constants.CONNECTION_FACTORY_JNDI_PARAM_NAME, Constants.NAMING_FACTORY_INITIAL_PARAM_NAME,
                Constants.PROVIDER_URL_PARAM_NAME, Constants.CONNECTION_FACTORY_TYPE_PARAM_NAME);
        // getting the required values
        Map<String, String> transportProperties = new HashMap<>();
        requiredOptions.forEach(requiredOption ->
                transportProperties.put(requiredOption, optionHolder.validateAndGetStaticValue(requiredOption)));
        // getting optional values
        optionHolder.getStaticOptionsKeys().stream()
                .filter(option -> !requiredOptions.contains(option) && !option.equals("type")).forEach(option ->
                transportProperties.put(option, optionHolder.validateAndGetStaticValue(option)));
        return transportProperties;
    }*/
}