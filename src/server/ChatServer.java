package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable
{
    // TCP port that the server listens on.
    private static final int PORT = 9999;

    // Manages all currently connected clients.
    private final ClientManager clientManager;

    // Thread pool allows multiple clients to communicate concurrently.
    private final ExecutorService threadPool;

    // ServerSocket listens for incoming TCP connection requests.
    private ServerSocket serverSocket;

    // Volatile makes changes visible across different threads.
    private volatile boolean running;

    public ChatServer()
    {
        clientManager = new ClientManager();

        // Cached thread pool creates threads as needed for connected clients.
        threadPool = Executors.newCachedThreadPool();

        running = false;
    }

    @Override
    public void run()
    {
        try
        {
            // Creates a TCP server socket bound to port 9999.
            serverSocket = new ServerSocket(PORT);

            running = true;

            System.out.println(
                    "Chat server started on port " + PORT
            );

            while (running)
            {
                // Blocks until a client establishes a TCP connection.
                Socket socket = serverSocket.accept();

                System.out.println(
                        "New client connected: " +
                                socket.getInetAddress()
                );

                // Creates an object responsible for communicating with this client.
                ClientConnection connection =
                        new ClientConnection(
                                socket,
                                clientManager
                        );

                // Register the client so it can receive broadcasts.
                clientManager.add(connection);

                // Give this client its own thread.
                threadPool.execute(connection);
            }
        }
        catch (IOException e)
        {
            // ServerSocket.close() causes accept() to throw when shutting down.
            if (running)
            {
                System.out.println(
                        "Server error: " + e.getMessage()
                );
            }
        }
        finally
        {
            shutdown();
        }
    }

    public void shutdown()
    {
        // Prevent shutdown from being executed multiple times.
        if (!running)
            return;

        running = false;

        System.out.println("Shutting down server...");

        // Disconnect every currently connected client.
        clientManager.shutdown();

        // Stop accepting new client tasks.
        threadPool.shutdownNow();

        try
        {
            // Closing ServerSocket causes accept() to stop blocking.
            if (serverSocket != null &&
                    !serverSocket.isClosed())
            {
                serverSocket.close();
            }
        }
        catch (IOException ignored)
        {
        }

        System.out.println("Server shut down.");
    }

    public static void main(String[] args)
    {
        ChatServer server = new ChatServer();

        // Starts the server's accept loop.
        server.run();
    }
}
