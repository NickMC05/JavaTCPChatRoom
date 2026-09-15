package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerReader implements Runnable
{
    // Reads text arriving from the server.
    private final BufferedReader reader;

    // Used to shut down the client if the server disconnects.
    private final ChatClient client;

    public ServerReader(
            BufferedReader reader,
            ChatClient client)
    {
        this.reader = reader;
        this.client = client;
    }

    @Override
    public void run()
    {
        try
        {
            String message;

            // Blocks until the server sends another line.
            while ((message = reader.readLine()) != null)
            {
                // Display the server's message to the user.
                System.out.println(message);
            }
        }
        catch (IOException e)
        {
            // Connection was probably closed.
        }
        finally
        {
            // Server disconnect means the client should also shut down.
            client.shutdown();
        }
    }
}
