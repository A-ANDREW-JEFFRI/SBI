package aj;

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opendaylight.netconf.api.NetconfMessage;
import org.opendaylight.netconf.api.xml.XmlUtil;
import org.opendaylight.netconf.client.NetconfClientDispatcher;
import org.opendaylight.netconf.client.NetconfClientDispatcherImpl;
import org.opendaylight.netconf.client.NetconfClientSession;
import org.opendaylight.netconf.client.NetconfClientSessionListener;
import org.opendaylight.netconf.client.SimpleNetconfClientSessionListener;
import org.opendaylight.netconf.client.conf.NetconfClientConfiguration;
import org.opendaylight.netconf.client.conf.NetconfClientConfiguration.NetconfClientProtocol;
import org.opendaylight.netconf.client.conf.NetconfClientConfigurationBuilder;
import org.opendaylight.netconf.nettyutil.NeverReconnectStrategy;
import org.opendaylight.netconf.nettyutil.handler.ssh.authentication.AuthenticationHandler;
import org.opendaylight.netconf.nettyutil.handler.ssh.authentication.LoginPasswordHandler;
import org.opendaylight.netconf.util.NetconfUtil;


/**
 * Synchronous netconf client suitable for testing.
 */
public class TestingNetconfClient implements Closeable {

    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private final String label; //client instance
    private final NetconfClientSession clientSession; //netconf communication
    private final NetconfClientSessionListener sessionListener; //handle session events
    private static Long sessionId; //unique identifier |  Store session ID
    private static TestingNetconfClient client = null;  // Store active client session
//    private static String sessionId = null;  // Store session ID

    //Constructor
    public TestingNetconfClient(final String clientLabel, //client identifier
                                final NetconfClientDispatcher netconfClientDispatcher, //create client session
                                final NetconfClientConfiguration config) throws InterruptedException { //contain connection details and listener
        this.label = clientLabel;
        sessionListener = config.getSessionListener();
        Future<NetconfClientSession> clientFuture = netconfClientDispatcher.createClient(config);
        clientSession = get(clientFuture); //create client session asyn(retrieve the session)
        this.sessionId = clientSession.getSessionId();
    }

    //retrieve the result from the provider future
    private static NetconfClientSession get(final Future<NetconfClientSession> clientFuture)
            throws InterruptedException {
        try {
            return clientFuture.get(); //retrieve the session
        } catch (CancellationException e) {
            throw new RuntimeException("Cancelling " + TestingNetconfClient.class.getSimpleName(), e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Unable to create " + TestingNetconfClient.class.getSimpleName(), e);
        }
    }

    //send netconf msg async
    public Future<NetconfMessage> sendRequest(final NetconfMessage message) {
        return ((SimpleNetconfClientSessionListener) sessionListener).sendRequest(message);
    }

    //waiting for the response
    public NetconfMessage sendMessage(final NetconfMessage message, final int attemptMsDelay) throws ExecutionException,
            InterruptedException, TimeoutException {
        return sendRequest(message).get(attemptMsDelay, TimeUnit.MILLISECONDS);
    }

    //use the default timeout
    public NetconfMessage sendMessage(final NetconfMessage message) throws ExecutionException,
            InterruptedException, TimeoutException {
        return sendMessage(message, DEFAULT_CONNECT_TIMEOUT);
    }

    //closes the client session
    @Override
    public void close() throws IOException {
        clientSession.close();
    }
    
    //creating a string contain label and sessionId
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestingNetconfClient{");
        sb.append("label=").append(label);
        sb.append(", sessionId=").append(sessionId);
        sb.append('}');
        return sb.toString();
    }

    //getter method
    public long getSessionId() {
        return sessionId;
    }

    //returns the sets of capabilities (server)
    public Set<String> getCapabilities() {
        checkState(clientSession != null, "Client was not initialized successfully");
        return Sets.newHashSet(clientSession.getServerCapabilities());
    }

    
    
//    public static Document convertToDocument(String xmlStr)
//    {
//    	try {
//    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//    	factory.setNamespaceAware(true);
//    	DocumentBuilder builder = factory.newDocumentBuilder();
//    	return builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
//    	}
//    	catch (Exception e)
//    	{
//    		System.out.println(e);
//    	}
//    }
    
    
    
//    .................netconf testtool...............
//    public static NetconfMessage GetConfigMessage()
//    {
//    	Document doc = XmlUtil.newDocument();
//    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","111");
//    	doc.appendChild(rpcElement);
//    	
//    	Element getConfigElement = doc.createElement("get");
//    	rpcElement.appendChild(getConfigElement);
//    	
//    	Element sourceElement =doc.createElement("filter");
//    	sourceElement.setAttribute("type","subtree");
//    	getConfigElement.appendChild(sourceElement);
//    	
//    	Element runningElement = doc.createElement("netconf-state");
//    	runningElement.setAttribute("xmlns","urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring");
//    	sourceElement.appendChild(runningElement);
//    	
//    	Element SchemasElement = doc.createElement("schemas");
//    	runningElement.appendChild(SchemasElement);
//    	
//    	NetconfMessage request = new NetconfMessage(doc);
//    	return request;
//    }
    
    
    
    public static NetconfMessage GetMessage()
    {
        java.io.Console console = System.console();

         String msgId = console.readLine("Enter message ID : ");
//         String operation = console.readLine("Enter operation : ");
//         String nameSpace = console.readLine("Enter namespace : ");
//         String container = console.readLine("Enter container : ");


         
    	Document doc = XmlUtil.newDocument();
    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","100");
    	rpcElement.setAttribute("message-id",msgId);
    	doc.appendChild(rpcElement);
    	
    	Element getConfigElement = doc.createElement("get");
//    	Element getConfigElement = doc.createElement(operation);
    	rpcElement.appendChild(getConfigElement);
    	
//    	Element sourceElement =doc.createElement("filter");
//    	sourceElement.setAttribute("type","subtree");
//    	getConfigElement.appendChild(sourceElement);
//    	
////    	Element runningElement = doc.createElement("interfaces");
//    	Element runningElement = doc.createElement(container);
////    	runningElement.setAttribute("xmlns","urn:test");
//    	runningElement.setAttribute("xmlns",nameSpace);
//    	sourceElement.appendChild(runningElement);
    	
    	NetconfMessage request = new NetconfMessage(doc);
    	return request;
    	
    	
    }
    
    public static NetconfMessage GetConfigMessage()
    {
        java.io.Console console = System.console();

         String msgId = console.readLine("Enter message ID : ");
//         String operation = console.readLine("Enter operation : ");
         String nameSpace = console.readLine("Enter namespace : ");
         String container = console.readLine("Enter container : ");


         
    	Document doc = XmlUtil.newDocument();
    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","100");
    	rpcElement.setAttribute("message-id",msgId);
    	doc.appendChild(rpcElement);
    	
    	Element getConfigElement = doc.createElement("get");
//    	Element getConfigElement = doc.createElement(operation);
    	rpcElement.appendChild(getConfigElement);
    	
    	Element sourceElement =doc.createElement("filter");
    	sourceElement.setAttribute("type","subtree");
    	getConfigElement.appendChild(sourceElement);
    	
//    	Element runningElement = doc.createElement("interfaces");
    	Element runningElement = doc.createElement(container);
//    	runningElement.setAttribute("xmlns","urn:test");
    	runningElement.setAttribute("xmlns",nameSpace);
    	sourceElement.appendChild(runningElement);
    	
    	NetconfMessage request = new NetconfMessage(doc);
    	return request;
    	
    	
    }
    
    
    
    
    public static NetconfMessage editConfigMessage()
    {
    	java.io.Console console = System.console();

        String msgId = console.readLine("Enter message ID : ");
//        String operation = console.readLine("Enter operation : ");
        String nameSpace = console.readLine("Enter namespace : ");
        String container = console.readLine("Enter container : ");
        String list = console.readLine("Enter list : ");
    
//    String nameSpace = console.readLine("Enter namespace : ");
        System.out.println("\nEnter the datas to update ");
    String ip = console.readLine("Enter ip : ");
    String name = console.readLine("Enter name : ");
    String enable = console.readLine("Enter enable(true/false) : ");

        
        
    	Document doc = XmlUtil.newDocument();

    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","100");
    	rpcElement.setAttribute("message-id",msgId);
    	doc.appendChild(rpcElement);
    	
    	Element editConfigElement = doc.createElement("edit-config");
    	rpcElement.appendChild(editConfigElement);
    	
    	Element targetElement = doc.createElement("target");
    	Element runningElement = doc.createElement("running");
    	targetElement.appendChild(runningElement);
    	editConfigElement.appendChild(targetElement);
   	
    	Element configElement = doc.createElement("config");
    	editConfigElement.appendChild(configElement);
    	
    	Element interfacesElement = doc.createElement(container);
    	interfacesElement.setAttribute("xmlns","urn:test");
    	configElement.appendChild(interfacesElement);
    	
//    	Element interfaceElement = doc.createElement("interface");
    	Element interfaceElement = doc.createElement(list);
    	interfacesElement.appendChild(interfaceElement);
    	
    	Element nameElement = doc.createElement("name");
    	nameElement.setTextContent(name);
    	interfaceElement.appendChild(nameElement);
    	
    	Element enableElement = doc.createElement("enable");
    	enableElement.setTextContent(enable);
    	interfaceElement.appendChild(enableElement);
    	
    	Element ipAddressElement = doc.createElement("ip-address");
    	ipAddressElement.setTextContent(ip);
    	interfaceElement.appendChild(ipAddressElement);
    	
    	NetconfMessage request = new NetconfMessage(doc);
    	return request;
    }
    
    
    public static NetconfMessage addConfigMessage()
    {
    	java.io.Console console = System.console();

        String msgId = console.readLine("Enter message ID : ");
//        String operation = console.readLine("Enter operation : ");
        String nameSpace = console.readLine("Enter namespace : ");
        String container = console.readLine("Enter container : ");
        String list = console.readLine("Enter list : ");
    
//    String nameSpace = console.readLine("Enter namespace : ");
        System.out.println("\nEnter the datas to add ");
    String ip = console.readLine("Enter ip : ");
    String name = console.readLine("Enter name : ");
    String enable = console.readLine("Enter enable(true/false) : ");

        
        
    	Document doc = XmlUtil.newDocument();

    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","100");
    	rpcElement.setAttribute("message-id",msgId);
    	doc.appendChild(rpcElement);
    	
    	Element editConfigElement = doc.createElement("edit-config");
    	rpcElement.appendChild(editConfigElement);
    	
    	Element targetElement = doc.createElement("target");
    	Element runningElement = doc.createElement("running");
    	targetElement.appendChild(runningElement);
    	editConfigElement.appendChild(targetElement);
   	
    	Element configElement = doc.createElement("config");
    	editConfigElement.appendChild(configElement);
    	
    	Element interfacesElement = doc.createElement(container);
    	interfacesElement.setAttribute("xmlns","urn:test");
    	configElement.appendChild(interfacesElement);
    	
//    	Element interfaceElement = doc.createElement("interface");
    	Element interfaceElement = doc.createElement(list);
    	interfacesElement.appendChild(interfaceElement);
    	
    	Element nameElement = doc.createElement("name");
    	nameElement.setTextContent(name);
    	interfaceElement.appendChild(nameElement);
    	
    	Element enableElement = doc.createElement("enable");
    	enableElement.setTextContent(enable);
    	interfaceElement.appendChild(enableElement);
    	
    	Element ipAddressElement = doc.createElement("ip-address");
    	ipAddressElement.setTextContent(ip);
    	interfaceElement.appendChild(ipAddressElement);
    	
    	NetconfMessage request = new NetconfMessage(doc);
    	return request;
    }
    
    public static NetconfMessage deleteConfigMessage()
    {
    	java.io.Console console = System.console();

        String msgId = console.readLine("Enter message ID : ");
//        String operation = console.readLine("Enter operation : ");
        String nameSpace = console.readLine("Enter namespace : ");
        String container = console.readLine("Enter container : ");
    
//    String nameSpace = console.readLine("Enter namespace : ");
        System.out.println("\n Enter the key name ");
//    String ip = console.readLine("Enter ip : ");
    String name = console.readLine("Enter name : ");
//    String enable = console.readLine("Enter enable(true/false) : ");

        
        
    	Document doc = XmlUtil.newDocument();

    	Element rpcElement = doc.createElementNS("urn:ietf:params:xml:ns:netconf:base:1.0","rpc");
//    	rpcElement.setAttribute("message-id","100");
    	rpcElement.setAttribute("message-id",msgId);
    	doc.appendChild(rpcElement);
    	
    	Element editConfigElement = doc.createElement("edit-config");
    	rpcElement.appendChild(editConfigElement);
    	
    	Element targetElement = doc.createElement("target");
    	Element runningElement = doc.createElement("running");
    	targetElement.appendChild(runningElement);
    	editConfigElement.appendChild(targetElement);
   	
    	Element configElement = doc.createElement("config");
    	configElement.setAttribute("xmlns:urn", nameSpace);
    	editConfigElement.appendChild(configElement);
    	
    	Element interfacesElement = doc.createElement(container);
    	interfacesElement.setAttribute("xmlns",nameSpace);
    	configElement.appendChild(interfacesElement);
    	
//    	Element interfaceElement = doc.createElement("interface");
    	Element interfaceElement = doc.createElement("interface");
    	interfaceElement.setAttribute("operation","delete");
    	interfacesElement.appendChild(interfaceElement);
    	
      	Element nameElement = doc.createElement("name");
    	nameElement.setTextContent(name);
    	interfaceElement.appendChild(nameElement);
    	
    	NetconfMessage request = new NetconfMessage(doc);
    	return request;
    }
    

    
    
    
    //Main Method
    public static void main(final String[] args) throws Exception {
    	Console console = System.console();
        if (console == null) {
            System.out.println("No console available.");
            return;
        }

        while (true) {
            // Show login option if not logged in, otherwise show Netconf operations
//            if (sessionId == null) {
                System.out.println("1 : Login");
//                System.out.println("2 : Exit");
//            } else {
//                System.out.println("\n--- Netconf Operations ---");
                System.out.println("2 : Get");
                System.out.println("3 : Get with Filter");
//                System.out.println("3 : Edit Config");
//                System.out.println("4 : Add Config");
//                System.out.println("5 : Delete Config");
                System.out.println("4 : Logout");

                System.out.println("5 : Exit");
                String choice = console.readLine("Enter your choice: ");
//            }

//            String choice = console.readLine("Enter your choice: ");

            switch (choice) {
            case "1":
                if (sessionId == null) {
                    login(console);
                } else {
                    System.out.println("please logout to login ");
                }
                break;
            case "2":
            	if (sessionId != null) 
        		{
            		 performGetOperation();
        		
        		}
            	else {
            		System.out.println("please login to perform netconf operation");
                }
                break;
            case "3":
            	if (sessionId != null) 
            		{
            		performGetWithFilter();
            		}
               
                 else {
             		System.out.println("please login to perform netconf operation");

                }
                break;
            case "4":
                if (sessionId != null) logout();
                else System.out.println("please login to perform logout operation");
                break;

            case "5":
                if (sessionId == null) {
                    System.out.println("Exiting...");
                    System.exit(0);
                } else {
                	System.out.println("please logout to exit ");
                }
                break;
            default:
                System.out.println("Invalid choice! Try again.");
            
            
            
            
            
            
            
            
            
            
//            switch (choice) {
//                case "1":
//                    if (sessionId == null) {
//                        login(console);
//                    } else {
//                        performGetOperation();
//                    }
//                    break;
//                case "2":
//                    if (sessionId == null) {
//                        System.out.println("Exiting...");
//                        System.exit(0);
//                    } else {
//                        performGetWithFilter();
//                    }
//                    break;
//                case "3":
//                    if (sessionId != null) performEditConfig();
//                    else System.out.println("Invalid choice!");
//                    break;
//                case "4":
//                    if (sessionId != null) performAddConfig();
//                    else System.out.println("Invalid choice!");
//                    break;
//                case "5":
//                    if (sessionId != null) performDeleteConfig();
//                    else System.out.println("Invalid choice!");
//                    break;
//                case "6":
//                    if (sessionId != null) logout();
//                    else System.out.println("Invalid choice!");
//                    break;
//                default:
//                    System.out.println("Invalid choice! Try again.");
            }
        }
    }



    
    private static void login(Console console) {
    	String ip = console.readLine("Enter IP Address: ");
        String name = console.readLine("Enter Username: ");
        String password = console.readLine("Enter Password: ");
        

        try {
            HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
            NioEventLoopGroup nettyGroup = new NioEventLoopGroup();
            NetconfClientDispatcherImpl netconfClientDispatcher = new NetconfClientDispatcherImpl(nettyGroup, nettyGroup, hashedWheelTimer);
            LoginPasswordHandler authHandler = new LoginPasswordHandler(name, password);
            
            client = new TestingNetconfClient("client", netconfClientDispatcher, getClientConfig(ip, 2022, true, Optional.of(authHandler)));
            sessionId = client.sessionId;  // Store session ID
            
            System.out.println("Login successful! Session ID: " + sessionId);
//            System.out.println("Server Capabilities: " + client.getCapabilities());
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            client = null;
            sessionId = null;
        }
    }

    private static void logout() {
        if (client != null) {
        	try {
        		client.close();
        	}
            catch(Exception e)
        	{
            	System.out.println("Error in logout operation: " + e.getMessage());
        	}
            client = null;
            sessionId = null;
            System.out.println("Logged out successfully.");
        }
    }

    private static void performGetOperation() {
        try {
            NetconfMessage getConf = GetMessage();
            Future<NetconfMessage> resp = client.sendRequest(getConf);
            System.out.println(resp.get().toString());
        } catch (Exception e) {
            System.out.println("Error in Get operation: " + e.getMessage());
        }
    }

    private static void performGetWithFilter() {
        try {
            NetconfMessage getConfFilter = GetConfigMessage();
            Future<NetconfMessage> respFilter = client.sendRequest(getConfFilter);
            System.out.println(respFilter.get().toString());
        } catch (Exception e) {
            System.out.println("Error in Get with Filter: " + e.getMessage());
        }
    }

    private static void performEditConfig() {
        try {
            NetconfMessage editConf = editConfigMessage();
            Future<NetconfMessage> respEdit = client.sendRequest(editConf);
            System.out.println(respEdit.get().toString());
        } catch (Exception e) {
            System.out.println("Error in Edit Config: " + e.getMessage());
        }
    }

    private static void performAddConfig() {
        try {
            NetconfMessage addConf = addConfigMessage();
            Future<NetconfMessage> respAdd = client.sendRequest(addConf);
            System.out.println(respAdd.get().toString());
        } catch (Exception e) {
            System.out.println("Error in Add Config: " + e.getMessage());
        }
    }

    private static void performDeleteConfig() {
        try {
            NetconfMessage deleteConf = deleteConfigMessage();
            Future<NetconfMessage> respDelete = client.sendRequest(deleteConf);
            System.out.println(respDelete.get().toString());
        } catch (Exception e) {
            System.out.println("Error in Delete Config: " + e.getMessage());
        }
    }

    private static NetconfClientConfiguration getClientConfig(final String host, final int port, final boolean ssh,
            final Optional<? extends AuthenticationHandler> maybeAuthHandler) throws Exception {
        InetSocketAddress netconfAddress = new InetSocketAddress(InetAddress.getByName(host), port);
        final NetconfClientConfigurationBuilder b = NetconfClientConfigurationBuilder.create();
        b.withAddress(netconfAddress);
        b.withSessionListener(new SimpleNetconfClientSessionListener());
        b.withReconnectStrategy(new NeverReconnectStrategy(GlobalEventExecutor.INSTANCE,
                NetconfClientConfigurationBuilder.DEFAULT_CONNECTION_TIMEOUT_MILLIS));

        if (ssh) {
            b.withProtocol(NetconfClientProtocol.SSH);
            b.withAuthHandler(maybeAuthHandler.get());
        } else {
            b.withProtocol(NetconfClientProtocol.TCP);
        }
        return b.build();
    }
}
    
    
    
//    
//    
//    
//    //Client Configuration methods
//    private static NetconfClientConfiguration getClientConfig(final String host, final int port, final boolean ssh,
//            final Optional<? extends AuthenticationHandler> maybeAuthHandler) throws UnknownHostException {
//        InetSocketAddress netconfAddress = new InetSocketAddress(InetAddress.getByName(host), port);
//        final NetconfClientConfigurationBuilder b = NetconfClientConfigurationBuilder.create();
//        b.withAddress(netconfAddress);
//        b.withSessionListener(new SimpleNetconfClientSessionListener()); //handle the session
//        b.withReconnectStrategy(new NeverReconnectStrategy(GlobalEventExecutor.INSTANCE,
//                NetconfClientConfigurationBuilder.DEFAULT_CONNECTION_TIMEOUT_MILLIS));
//        if (ssh) {
//            b.withProtocol(NetconfClientProtocol.SSH);
//            b.withAuthHandler(maybeAuthHandler.get());
//        } else {
//            b.withProtocol(NetconfClientProtocol.TCP);
//        }
//        return b.build();
//    }
//}
