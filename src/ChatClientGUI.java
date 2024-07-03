import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientGUI extends JFrame {
    private JTextArea messageArea;
    private JTextField textField;
    private JLabel typingLabel; // To display typing indicator
    private JButton sendButton; // Changed from exitButton to sendButton for message sending
    private ChatClient client;
    private String name;
    private boolean isTyping = false; // Variable to track typing state

    public ChatClientGUI() {
        super("Chat Application");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Color backgroundColor = new Color(240, 240, 240);
        Color buttonColor = new Color(75, 75, 75);
        Color textColor = new Color(50, 50, 50);
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(textFont);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        name = JOptionPane.showInputDialog(this, "Enter your name:", "Name Entry", JOptionPane.PLAIN_MESSAGE);
        this.setTitle("Chat Application - " + name);
        textField = new JTextField();
        textField.setFont(textFont);
        textField.setForeground(textColor);
        textField.setBackground(backgroundColor);

        // Typing indicator label setup
        typingLabel = new JLabel();
        typingLabel.setFont(textFont.deriveFont(Font.ITALIC));
        typingLabel.setForeground(Color.GRAY);
        typingLabel.setVisible(false); // Initially invisible

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // Consume enter key event to prevent line break in text field
                    sendMessage();
                }
                if (!isTyping) {
                    setTyping(true); // Set typing indicator on key press
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (textField.getText().isEmpty()) {
                    setTyping(false); // Reset typing indicator when text field is empty
                }
            }
        });

        sendButton = new JButton("Send");
        sendButton.setFont(buttonFont);
        sendButton.setBackground(buttonColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> {
            sendMessage();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(typingLabel, BorderLayout.SOUTH); // Add typing indicator label
        bottomPanel.add(sendButton, BorderLayout.EAST); // Use sendButton for sending messages
        add(bottomPanel, BorderLayout.SOUTH);

        try {
            this.client = new ChatClient("127.0.0.1", 5000, this::onMessageReceived);
            client.startClient();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the server", "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendMessage() {
        String messageText = textField.getText();
        if (!messageText.isEmpty()) {
            String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + name + ": "
                    + messageText;
            client.sendMessage(message);
            textField.setText("");
            typingLabel.setVisible(false); // Hide typing indicator after sending message
            setTyping(false); // Reset typing indicator
        }
    }

    private void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("[typing]")) {
                String userTyping = message.substring("[typing]".length());
                if (!userTyping.equals(name)) {
                    typingLabel.setText(userTyping + " is typing...");
                    typingLabel.setVisible(true);
                }
            } else {
                if (message.contains(name + ": ")) {
                    messageArea.append(message + "\n");
                } else if (!isTyping) {
                    messageArea.append(message + "\n");
                    typingLabel.setVisible(false); // Hide typing indicator when message is received
                }
            }
        });
    }

    private void setTyping(boolean typing) {
        if (typing) {
            client.sendMessage("[typing]" + name);
        }
        isTyping = typing;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}
