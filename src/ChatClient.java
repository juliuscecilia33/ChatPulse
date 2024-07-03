import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private String username; // Store username after authentication

    public ChatClient(String serverAddress, int serverPort, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.onMessageReceived = onMessageReceived;
    }

    // Method to send messages to the server
    public void sendMessage(String msg) {
        out.println(msg);
    }

    // Method to handle user authentication
    public boolean authenticate(String command, String username, String password) throws IOException {
        sendMessage(command); // Send 'register' or 'login' command
        sendMessage(username); // Send username
        sendMessage(password); // Send password

        String response = in.readLine(); // Read server response
        if (response.equals("AUTH_SUCCESS")) {
            this.username = username; // Store username after successful authentication
            return true;
        } else {
            return false;
        }
    }

    // Method to start the client
    public void startClient() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessageReceived.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getUsername() {
        return username;
    }

    public static void main(String[] args) {
        // Example usage for testing purposes
        try {
            ChatClient client = new ChatClient("127.0.0.1", 5000, System.out::println);
            client.startClient();

            // Example of authentication (replace with actual GUI logic)
            boolean authenticated = client.authenticate("login", "user123", "password123");
            if (authenticated) {
                System.out.println("Authentication successful. Username: " + client.getUsername());
            } else {
                System.out.println("Authentication failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
