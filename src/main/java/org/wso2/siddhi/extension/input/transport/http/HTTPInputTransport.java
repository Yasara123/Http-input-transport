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
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorController;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.extension.input.transport.http.Util.ServerUtil;
import org.wso2.siddhi.extension.input.transport.http.server.HTTPServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Extension(
        name = "http",
        namespace = "inputtransport",
        description = ""
)
public class HTTPInputTransport extends InputTransport {
    private static final Logger log = Logger.getLogger(HTTPInputTransport.class);
    public static final String ADAPTER_PROXY_HOST = "HOST";
    public static final String ADAPTER_PROXY_PORT = "PORT";
    public static final String ADAPTER_PROXY_METHOD = "METHOD";
    public static final String ADAPTER_PROXY_CONTEXT = "PATH";
    public static final int ADAPTER_PROXY_SERVER_PORT = 9000;
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private int threadPoolSize;
    private final int DEFAULT_THREAD_POOL_SIZE = 1;
    private HTTPServerConnector  httpServerConnector;
    private HTTPMessageProcessor httpMessageProcessor;
    private HTTPServer httpServer;
    private List<HTTPServerConnector> serverConnector;
    private TransportsConfiguration configuration;
    private String host;
    private int port;
    private String path;
    private String method ;
    private Set<TransportProperty> confTrasport;
    Set<ListenerConfiguration> confListner;
    private Set<SenderConfiguration> confSender;
    ListenerConfiguration Config;
    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ExecutionPlanContext executionPlanContext) {
        //Reading default configurations
        configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");

        confTrasport=configuration.getTransportProperties();
        confListner=configuration.getListenerConfigurations();
        confSender=configuration.getSenderConfigurations();
        //Reading user given configurations
        //TODO:configure as init method and read extra parameters
        host=optionHolder.validateAndGetStaticValue(ADAPTER_PROXY_HOST,"");
        port=Integer.parseInt(optionHolder.validateAndGetStaticValue(ADAPTER_PROXY_PORT,"0"));
        method=optionHolder.validateAndGetStaticValue(ADAPTER_PROXY_METHOD,"");
        path=optionHolder.validateAndGetStaticValue(ADAPTER_PROXY_CONTEXT,"");

        port = (port==Integer.parseInt("0")) ? 9763 : port;
        host = (host.equals("")) ? "0.0.0.0" : host;

        Config = new ListenerConfiguration("Http Listener", host, port);
        confListner = new HashSet<ListenerConfiguration>();
        confListner.add(Config);
        //confListner.clear();
        //confListner.add(Config);
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        String id ="Http Listener";
        httpServerConnector=new HTTPServerConnector(id);

            }

    @Override
    public void connect() throws ConnectionUnavailableException {
        httpMessageProcessor = new HTTPMessageProcessor(sourceEventListener, threadPoolSize);
        TransportsConfiguration configuration =new TransportsConfiguration();
        configuration.setTransportProperties(confTrasport);
        configuration.setListenerConfigurations(confListner);
        configuration.setSenderConfigurations(confSender);

        //serverConnector =  ServerUtil.startConnectors(configuration, httpMessageProcessor);
        //httpServer = ServerUtil.startHTTPServer(ADAPTER_PROXY_SERVER_PORT);

        ServerConnectorController serverConnectorController=new ServerConnectorController(configuration);
        serverConnectorController.start();
        httpServerConnector.setMessageProcessor(httpMessageProcessor);
        httpServerConnector.setListenerConfiguration(Config);
        httpServerConnector.setServerConnectorController(serverConnectorController);
        try {
            httpServerConnector.init();
            httpServerConnector.start();
        } catch (ServerConnectorException e) {
            e.printStackTrace();
        }


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
        try {
            httpServerConnector.destroyConnector();
        } catch (ServerConnectorException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
httpMessageProcessor.pause();
    }

    @Override
    public void resume() {
httpMessageProcessor.resume();
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
   /* private Map<String, String> initHTTPProperties() {
        List<String> requiredOptions = Arrays.asList();
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
