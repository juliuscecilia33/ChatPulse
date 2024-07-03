import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static Map<String, User> users = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            ClientHandler clientThread = new ClientHandler(clientSocket, clients, users);
            clients.add(clientThread);
            new Thread(clientThread).start();
        }
    }
}

// Modify ClientHandler to handle user registration and login
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private Map<String, User> users;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, List<ClientHandler> clients, Map<String, User> users) throws IOException {
        this.clientSocket = socket;
        this.clients = clients;
        this.users = users;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void run() {
        try {
            // Handle registration and login
            out.println("Enter command (register/login):");
            String command = in.readLine();
            if (command.equals("register")) {
                out.println("Enter username:");
                String username = in.readLine();
                out.println("Enter password:");
                String password = in.readLine();
                users.put(username, new User(username, password));
                out.println("Registration successful!");
            } else if (command.equals("login")) {
                out.println("Enter username:");
                String username = in.readLine();
                out.println("Enter password:");
                String password = in.readLine();
                User user = users.get(username);
                if (user != null && user.getPassword().equals(password)) {
                    out.println("Login successful!");
                    // Continue with chat functionality
                } else {
                    out.println("Invalid credentials");
                    return;
                }
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                for (ClientHandler aClient : clients) {
                    aClient.out.println(inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                clients.remove(this);
                System.out.println("Client disconnected: " + clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
