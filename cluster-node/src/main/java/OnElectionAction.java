import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import networking.WebClient;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchCoordinator;
import search.SearchWorker;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry workersServiceRegistry;
    private final ServiceRegistry coordinatorsServiceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry workersServiceRegistry,
                            ServiceRegistry coordinatorsServiceRegistry,
                            int port) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        workersServiceRegistry.unregisterFromCluster();
        workersServiceRegistry.registerForUpdates();

        if (webServer != null) {
            try {
                webServer.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            String currentServerAddress = String.format("%s:%d", InetAddress.getLoopbackAddress().getHostAddress(), port);
            SearchCoordinator searchCoordinator = new SearchCoordinator(workersServiceRegistry, new WebClient(InetAddress.getLoopbackAddress().getHostAddress(), port));
            webServer = new WebServer(port, searchCoordinator);
            webServer.startServer();
            coordinatorsServiceRegistry.registerToCluster(currentServerAddress);
        } catch (IOException | KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //            String currentServerAddress = String.format("%s:%d", InetAddress.getLocalHost().getHostAddress(), port);

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if (webServer == null) {
            webServer = new WebServer(port, searchWorker);
            try {
                webServer.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String currentServerAddress =
                    String.format("%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), port);
            workersServiceRegistry.registerToCluster(currentServerAddress);
        } catch (UnknownHostException | KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

