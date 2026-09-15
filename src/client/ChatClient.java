package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient
{
    // Server IP address; 127.0.0.1 means this same computer.
    private static final String HOST = "127.0.0.1";

    // Must match the port used by ChatServer.
    private static final int PORT = 9999;

    // TCP socket used for both sending and receiving.
    private Socket socket;

    // Reads data coming from the server.
    private BufferedReader reader;

    // Sends data to the server.
    private PrintWriter writer;

    // Indicates whether the client is still running.
    private volatile boolean running;

    public void start()
    {
        try
        {
            // Establishes a TCP connection to the server.
            socket = new Socket(HOST, PORT);

            System.out.println(
                    "Connected to " +
                            HOST +
                            ":" +
                            PORT
            );

            // TCP socket input receives data from the server.
            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            // TCP socket output sends data to the server.
            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            running = true;

            // Thread responsible for receiving server messages.
            Thread serverReader =
                    new Thread(
                            new ServerReader(
                                    reader,
                                    this
                            )
                    );

            // Thread responsible for reading user input.
            Thread inputHandler =
                    new Thread(
                            new ConsoleInputHandler(
                                    writer,
                                    this
                            )
                    );

            serverReader.start();
            inputHandler.start();

            // Wait for the server-reading thread to finish.
            serverReader.join();
        }
        catch (IOException e)
        {
            System.out.println("Connection error: " + e.getMessage());

            shutdown();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            shutdown();
        }
    }

    public void shutdown()
    {
        if (!running)
            return;

        running = false;

        try
        {
            // Closing the reader closes the socket input stream.
            if (reader != null)
                reader.close();

            // Closing the writer closes the socket output stream.
            if (writer != null)
                writer.close();

            // Closing the socket terminates the TCP connection.
            if (socket != null && !socket.isClosed())
                socket.close();
        }
        catch (IOException ignored) {}

        System.out.println("Disconnected.");
    }

    public static void main(String[] args)
    {
        ChatClient client = new ChatClient();

        client.start();
    }
}
