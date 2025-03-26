import java.io.Console;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.HashedWheelTimer;
import io.netty.channel.nio.NioEventLoopGroup;

public class NetconfClientApp {
    private static TestingNetconfClient client = null;  // Store active client session
    private static String sessionId = null;  // Store session ID

    public static void main(final String[] args) throws Exception {
        Console console = System.console();
        if (console == null) {
            System.out.println("No console available.");
            return;
        }

        while (true) {
            // Show login option if not logged in, otherwise show Netconf operations
            if (sessionId == null) {
                System.out.println("1 : Login");
                System.out.println("2 : Exit");
            } else {
                System.out.println("\n--- Netconf Operations ---");
                System.out.println("1 : Get");
                System.out.println("2 : Get with Filter");
                System.out.println("3 : Edit Config");
                System.out.println("4 : Add Config");
                System.out.println("5 : Delete Config");
                System.out.println("6 : Logout");
            }

            String choice = console.readLine("Enter your choice: ");

            switch (choice) {
                case "1":
                    if (sessionId == null) {
                        login(console);
                    } else {
                        performGetOperation();
                    }
                    break;
                case "2":
                    if (sessionId == null) {
                        System.out.println("Exiting...");
                        System.exit(0);
                    } else {
                        performGetWithFilter();
                    }
                    break;
                case "3":
                    if (sessionId != null) performEditConfig();
                    else System.out.println("Invalid choice!");
                    break;
                case "4":
                    if (sessionId != null) performAddConfig();
                    else System.out.println("Invalid choice!");
                    break;
                case "5":
                    if (sessionId != null) performDeleteConfig();
                    else System.out.println("Invalid choice!");
                    break;
                case "6":
                    if (sessionId != null) logout();
                    else System.out.println("Invalid choice!");
                    break;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void login(Console console) {
        String name = console.readLine("Enter Username: ");
        String password = console.readLine("Enter Password: ");
        String ip = console.readLine("Enter IP Address: ");

        try {
            HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
            NioEventLoopGroup nettyGroup = new NioEventLoopGroup();
            NetconfClientDispatcherImpl netconfClientDispatcher = new NetconfClientDispatcherImpl(nettyGroup, nettyGroup, hashedWheelTimer);
            LoginPasswordHandler authHandler = new LoginPasswordHandler(name, password);
            
            client = new TestingNetconfClient("client", netconfClientDispatcher, getClientConfig(ip, 2022, true, Optional.of(authHandler)));
            sessionId = client.sessionId;  // Store session ID
            
            System.out.println("Login successful! Session ID: " + sessionId);
            System.out.println("Server Capabilities: " + client.getCapabilities());
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            client = null;
            sessionId = null;
        }
    }

    private static void logout() {
        if (client != null) {
            client.close();
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
