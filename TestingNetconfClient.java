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
    private final long sessionId; //unique identifier

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
    	java.io.Console console = System.console();
        if (console == null)
        {
        	System.out.println("no console");
        }
        
        while(true) {
        	
	        System.out.println("1 : get");
	    	System.out.println("2 : get with filter");
	    	System.out.println("3 : edit-config");
	    	System.out.println("4 : add-config");
	    	System.out.println("5 : delete-config");
	    	System.out.println("6 : exit");

	        System.out.println("7 : login");
	    	System.out.println("8 : logout");
	        String choice = console.readLine("Enter your choice : ");
	        
	        switch(choice)
	        {
    	
//		 String name = console.readLine("Enter the Name : ");
//	     String password = console.readLine("Enter the Password : ");
//	     String ip = console.readLine("Enter the ip address : ");
//         
//         
//        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(); //schedules the timeout
//        NioEventLoopGroup nettyGroup = new NioEventLoopGroup(); //handle i/o operation
//        NetconfClientDispatcherImpl netconfClientDispatcher = new NetconfClientDispatcherImpl(nettyGroup, nettyGroup,hashedWheelTimer);//creating client session
//        LoginPasswordHandler authHandler = new LoginPasswordHandler(name, password);
//        TestingNetconfClient client = new TestingNetconfClient("client", netconfClientDispatcher,
//                getClientConfig(ip, 2022, true, Optional.of(authHandler)));
//        System.out.println(client.getCapabilities());
//        System.out.println(client.sessionId);
//        System.out.println(client.hashCode());
        
        
        
        
        
       
        
//        if(name.equals("admin") && password.equals("admin")  && ip.equals("127.0.0.0"))
//        {
	        
//	        while(true) {
//	        	
//	        System.out.println("1 : get");
//	    	System.out.println("2 : get with filter");
//	    	System.out.println("3 : edit-config");
//	    	System.out.println("4 : add-config");
//	    	System.out.println("5 : delete-config");
//	    	System.out.println("6 : exit");
//	        String choice = console.readLine("Enter your choice : ");
//	        
//	        switch(choice)
//	        {
	        case "1":
	            NetconfMessage getconf = GetMessage();
	            Future<NetconfMessage> resp = client.sendRequest(getconf);
	            System.out.println(resp.toString());
	            NetconfMessage getResp = client.sendMessage(getconf);
	            System.out.println(getResp);
	        	break;
	        case "2":
	            NetconfMessage getconfFilter = GetConfigMessage(); 
	            Future<NetconfMessage> respFilter = client.sendRequest(getconfFilter);
	            System.out.println(respFilter.toString());
	            NetconfMessage getRespFilter = client.sendMessage(getconfFilter);
	            System.out.println(getRespFilter);
	        	break;
	        case "3":
	            NetconfMessage editconf = editConfigMessage();
	            Future<NetconfMessage> respEdit = client.sendRequest(editconf);
	            System.out.println(respEdit.toString());
	            NetconfMessage editResp = client.sendMessage(editconf);
	            System.out.println(editResp);
	        case "4":
	            NetconfMessage addconf = addConfigMessage();
	//            System.out.println(deletetconf);
	            Future<NetconfMessage> respAdd = client.sendRequest(addconf);
	            System.out.println(respAdd.toString());
	            NetconfMessage AddResp = client.sendMessage(addconf);
	            System.out.println(AddResp);
	        	break;
	        case "5":
	            NetconfMessage deletetconf = deleteConfigMessage();
	//            System.out.println(deletetconf);
	            Future<NetconfMessage> respDelete = client.sendRequest(deletetconf);
	            System.out.println(respDelete.toString());
	            NetconfMessage deleteResp = client.sendMessage(deletetconf);
	            System.out.println(deleteResp);
	        	break;
	       
	        case "6":
	        	System.out.println("Exit.....");
	        	System.exit(0);
//	        	return;
	        case "7":
	        	 String name = console.readLine("Enter the Name : ");
	    	     String password = console.readLine("Enter the Password : ");
	    	     String ip = console.readLine("Enter the ip address : ");
	             
	             
	            HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(); //schedules the timeout
	            NioEventLoopGroup nettyGroup = new NioEventLoopGroup(); //handle i/o operation
	            NetconfClientDispatcherImpl netconfClientDispatcher = new NetconfClientDispatcherImpl(nettyGroup, nettyGroup,hashedWheelTimer);//creating client session
	            LoginPasswordHandler authHandler = new LoginPasswordHandler(name, password);
	            TestingNetconfClient client = new TestingNetconfClient("client", netconfClientDispatcher,
	                    getClientConfig(ip, 2022, true, Optional.of(authHandler)));
	            System.out.println(client.getCapabilities());
	            System.out.println(client.sessionId);
	            System.out.println(client.hashCode());
	        default:
	        	System.out.println("Invalid choice!");
	        }
	        }
//        }  
//        else {
//        	System.out.println("User name or Password or ip address is Incorrect!!!!");
//        }
    }

    //Client Configuration methods
    private static NetconfClientConfiguration getClientConfig(final String host, final int port, final boolean ssh,
            final Optional<? extends AuthenticationHandler> maybeAuthHandler) throws UnknownHostException {
        InetSocketAddress netconfAddress = new InetSocketAddress(InetAddress.getByName(host), port);
        final NetconfClientConfigurationBuilder b = NetconfClientConfigurationBuilder.create();
        b.withAddress(netconfAddress);
        b.withSessionListener(new SimpleNetconfClientSessionListener()); //handle the session
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
