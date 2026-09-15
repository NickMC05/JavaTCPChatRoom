package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ConsoleInputHandler implements Runnable
{
    // Sends user messages to the server.
    private final PrintWriter writer;

    // Allows this handler to shut down the client.
    private final ChatClient client;

    public ConsoleInputHandler(
            PrintWriter writer,
            ChatClient client)
    {
        this.writer = writer;
        this.client = client;
    }

    @Override
    public void run()
    {
        try
        {
            // Reads keyboard input from the console.
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            String message;

            // Wait for the user to type a message.
            while ((message = console.readLine()) != null)
            {
                // Send the typed line through the TCP connection.
                writer.println(message);

                // /quit tells the server that this client wants to leave.
                if (message.equalsIgnoreCase("/quit"))
                    break;
            }
        }
        catch (IOException e)
        {
            client.shutdown();
        }
    }
}
