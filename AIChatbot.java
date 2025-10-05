import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AIChatbot {

    private static Map<String, String> knowledgeBase = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the Java AI Chatbot!");

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1) Chat (CLI)");
            System.out.println("2) Train (add Q&A)");
            System.out.println("3) Save KB");
            System.out.println("4) Load KB");
            System.out.println("5) Start GUI chat");
            System.out.println("6) Show all Q&A");
            System.out.println("7) Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": chatCLI(); break;
                case "2": train(); break;
                case "3": saveKnowledgeBase(); break;
                case "4": loadKnowledgeBase(); break;
                case "5": startGUI(); break;
                case "6": showAllQA(); break;
                case "7": System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private static void chatCLI() {
        System.out.println("Type 'exit' to stop chatting.\n");
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("exit")) break;
            System.out.println("Bot: " + getResponse(userInput));
        }
    }

    private static String getResponse(String input) {
        input = input.toLowerCase().trim();

        // Rule-based responses
        if (input.contains("hello") || input.contains("hi")) return "Hello! How can I help you today?";
        if (input.contains("your name")) return "I'm a simple Java chatbot.";
        if (input.contains("time")) return "Current time: " + new Date().toString();
        if (input.contains("bye")) return "Goodbye! Have a nice day.";

        // Check knowledge base
        String bestMatch = findBestMatch(input);
        if (bestMatch != null) return knowledgeBase.get(bestMatch);

        return "I don't know about that yet. You can train me using option 2.";
    }

    private static String findBestMatch(String input) {
        int maxScore = 0;
        String best = null;
        for (String q : knowledgeBase.keySet()) {
            int score = similarity(input, q);
            if (score > maxScore) {
                maxScore = score;
                best = q;
            }
        }
        return (maxScore > 1) ? best : null;
    }

    private static int similarity(String a, String b) {
        Set<String> wa = new HashSet<>(Arrays.asList(a.split(" ")));
        Set<String> wb = new HashSet<>(Arrays.asList(b.split(" ")));
        wa.retainAll(wb);
        return wa.size();
    }

    private static void train() {
        System.out.print("Enter a question: ");
        String q = scanner.nextLine().toLowerCase().trim();
        System.out.print("Enter the answer: ");
        String a = scanner.nextLine().trim();
        knowledgeBase.put(q, a);
        System.out.println("Added successfully!");
    }

    private static void saveKnowledgeBase() {
        try (PrintWriter out = new PrintWriter(new FileWriter("knowledge.txt"))) {
            for (Map.Entry<String, String> e : knowledgeBase.entrySet()) {
                out.println(escape(e.getKey()) + " ::: " + escape(e.getValue()));
            }
            System.out.println("Knowledge base saved to knowledge.txt");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void loadKnowledgeBase() {
        try (BufferedReader br = new BufferedReader(new FileReader("knowledge.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ::: ", 2);
                if (parts.length == 2)
                    knowledgeBase.put(unescape(parts[0]), unescape(parts[1]));
            }
            System.out.println("Knowledge base loaded!");
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"").replace(":::", "\\:::");
    }

    private static String unescape(String s) {
        return s.replace("\\:::", ":::").replace("\\\"", "\"");
    }

    private static void showAllQA() {
        if (knowledgeBase.isEmpty()) {
            System.out.println("No Q&A available.");
            return;
        }
        System.out.println("--- Knowledge Base ---");
        for (Map.Entry<String, String> e : knowledgeBase.entrySet()) {
            System.out.println("Q: " + e.getKey());
            System.out.println("A: " + e.getValue());
            System.out.println();
        }
    }

    private static void startGUI() {
        JFrame frame = new JFrame("Java Chatbot");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JTextField inputField = new JTextField();

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                chatArea.append("You: " + text + "\n");
                chatArea.append("Bot: " + getResponse(text) + "\n\n");
                inputField.setText("");
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}