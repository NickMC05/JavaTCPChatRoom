package server;

public class CommandHandler
{
    private CommandHandler()
    {
        // Prevent creating instances of this utility class.
    }

    public static void handle(
            ClientConnection client,
            String message)
    {
        String[] parts = message.split(" ", 2);

        String command = parts[0].toLowerCase();

        String argument = "";

        if (parts.length == 2)
        {
            argument = parts[1];
        }

        switch (command)
        {
            case "/user":
                handleUsername(
                        client,
                        argument
                );
                break;

            case "/quit":
                handleQuit(client);
                break;

            case "/help":
                handleHelp(client);
                break;

            default:
                client.sendMessage(
                        "Unknown command: " + command
                );
                break;
        }
    }

    private static void handleUsername(
            ClientConnection client,
            String argument)
    {
        if (argument.isBlank())
        {
            client.sendMessage(
                    "Usage: /user <username>"
            );

            return;
        }

        client.changeUsername(argument);
    }

    private static void handleQuit(
            ClientConnection client)
    {
        client.disconnect();
    }

    private static void handleHelp(
            ClientConnection client)
    {
        client.sendMessage(
                "Available commands:"
        );

        client.sendMessage(
                "/user <username> - Change username"
        );

        client.sendMessage(
                "/quit - Leave the chat"
        );

        client.sendMessage(
                "/help - Show this help message"
        );
    }
}
