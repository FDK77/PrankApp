package org.example.Admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AdminClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            Thread sendMessageThread = new Thread(() -> {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

                    System.out.println("Введите команды для отправки на сервер (для выхода введите 'exit'):");

                    String userInput;
                    while ((userInput = consoleReader.readLine()) != null) {
                        if ("exit".equalsIgnoreCase(userInput)) {
                            out.println(userInput);
                            break;
                        }
                        out.println("admin_command:" + userInput);
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при отправке сообщения: " + e.getMessage());
                }
            });
            sendMessageThread.start();

            Thread receiveMessageThread = new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Ответ от сервера: " + serverResponse);
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при чтении сообщения: " + e.getMessage());
                }
            });
            receiveMessageThread.start();

            sendMessageThread.join();
            receiveMessageThread.join();

            socket.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка при подключении к серверу: " + e.getMessage());
        }
    }
}
