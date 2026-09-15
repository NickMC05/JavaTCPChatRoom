package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection implements Runnable
{
    // TCP connection between this client and the server.
    private final Socket socket;

    // Provides access to all other connected clients.
    private final ClientManager clientManager;

    // Reads text sent from the client.
    private BufferedReader reader;

    // Sends text to the client.
    private PrintWriter writer;

    // Username belonging to this connection.
    private String username;

    // Indicates whether this connection is still active.
    private volatile boolean connected;

    public ClientConnection(
            Socket socket,
            ClientManager clientManager)
    {
        this.socket = socket;
        this.clientManager = clientManager;

        connected = true;
    }

    @Override
    public void run()
    {
        try
        {
            setupStreams();

            requestUsername();

            // Tell everyone that this client joined.
            clientManager.broadcast(
                    username + " joined the chat!"
            );

            System.out.println(
                    username + " connected."
            );

            String message;

            // readLine() blocks until the client sends a line or disconnects.
            while (
                    connected &&
                            (message = reader.readLine()) != null
            )
            {
                handleMessage(message);
            }
        }
        catch (IOException e)
        {
            // Usually means the client disconnected unexpectedly.
        }
        finally
        {
            // Always remove the client when communication ends.
            disconnect();
        }
    }

    private void setupStreams() throws IOException
    {
        // InputStream receives raw bytes from the TCP socket.
        reader = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()
                )
        );

        // OutputStream sends raw bytes through the TCP socket.
        writer = new PrintWriter(
                socket.getOutputStream(),
                true
        );
    }

    private void requestUsername() throws IOException
    {
        sendMessage("Please enter a username:");

        // Blocks until the client sends its username.
        username = reader.readLine();

        if (username == null || username.isBlank())
        {
            username = "Anonymous";
        }
    }

    private void handleMessage(String message)
    {
        // Commands begin with '/'.
        if (message.startsWith("/"))
        {
            CommandHandler.handle(
                    this,
                    message
            );

            return;
        }

        // Normal messages are broadcast to everyone.
        clientManager.broadcast(
                username + ": " + message
        );
    }

    public void sendMessage(String message)
    {
        if (writer == null)
            return;

        // println() writes the message and flushes because autoFlush is enabled.
        writer.println(message);
    }

    public void changeUsername(String newUsername)
    {
        if (newUsername == null || newUsername.isBlank())
        {
            sendMessage("Username cannot be empty.");
            return;
        }

        String oldUsername = username;

        username = newUsername;

        clientManager.broadcast(
                oldUsername +
                        " renamed themselves to " +
                        username
        );

        sendMessage(
                "Successfully changed username to " +
                        username
        );
    }

    public void disconnect()
    {
        // Prevent multiple threads from disconnecting the same client.
        if (!connected)
            return;

        connected = false;

        if (username != null)
        {
            clientManager.broadcast(
                    username + " left the chat!"
            );
        }

        // Remove this client from the server's client list.
        clientManager.remove(this);

        closeResources();
    }

    public void shutdown()
    {
        connected = false;

        closeResources();
    }

    private void closeResources()
    {
        try
        {
            // Closing the reader releases the socket input stream.
            if (reader != null)
                reader.close();

            // Closing the writer releases the socket output stream.
            if (writer != null)
                writer.close();

            // Closing the socket terminates the TCP connection.
            if (socket != null && !socket.isClosed())
                socket.close();
        }
        catch (IOException ignored)
        {
        }
    }

    public String getUsername()
    {
        return username;
    }

    public ClientManager getClientManager()
    {
        return clientManager;
    }
}
