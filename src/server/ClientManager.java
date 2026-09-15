package server;

import java.util.concurrent.CopyOnWriteArrayList;

public class ClientManager
{
    // Thread-safe collection because multiple client threads access it.
    private final CopyOnWriteArrayList<ClientConnection> clients;

    public ClientManager()
    {
        clients = new CopyOnWriteArrayList<>();
    }

    public void add(ClientConnection client)
    {
        clients.add(client);

        System.out.println(
                "Connected clients: " + clients.size()
        );
    }

    public void remove(ClientConnection client)
    {
        clients.remove(client);

        System.out.println(
                "Connected clients: " + clients.size()
        );
    }

    public void broadcast(String message)
    {
        // Send the message to every connected TCP client.
        for (ClientConnection client : clients)
            client.sendMessage(message);
    }

    public void shutdown()
    {
        // Disconnect every client when the server shuts down.
        for (ClientConnection client : clients)
            client.shutdown();

        clients.clear();
    }

    public int getClientCount()
    {
        return clients.size();
    }
}
