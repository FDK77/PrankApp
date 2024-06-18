package org.example.Client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int HEARTBEAT_INTERVAL = 5000;

    public static void main(String[] args) {
        while (true) {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("Введите ваше имя: ");
                String username = consoleInput.readLine();
                out.println(username);
                Thread heartbeatThread = new Thread(() -> {
                    try {
                        while (true) {
                            Thread.sleep(HEARTBEAT_INTERVAL);
                            out.println("heartbeat");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                heartbeatThread.start();

                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    System.out.println("Сообщение от сервера: " + serverResponse);

                    if (serverResponse.equals("screamer")) {
                        runScreamer();
                    }
                    if (serverResponse.startsWith("TEXT.")) {
                        runText(serverResponse);
                    }
                    if (serverResponse.startsWith("URL.")) {
                        launchBrowser(serverResponse);
                    }
                    if (serverResponse.equals("shutdown")) {
                        try {
                            Runtime.getRuntime().exec("shutdown /s /t 0");
                            System.out.println("Отключение компьютера");
                        } catch (IOException e) {
                            System.out.println("Ошибка при попытке выключить компьютер: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка при подключении к серверу: " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void runScreamer() {
        System.out.println("Я получил скример!");
        FullScreenImage.runScreamer("src\\main\\java\\org\\example\\Client\\Res\\screamer.jpg", "src\\main\\java\\org\\example\\Client\\Res\\screamer.wav");
    }

    private static void runText(String text) {
        int dotIndex = text.indexOf('.');
        String result = text.substring(dotIndex + 1);
        FullScreenText.showFullScreenText(result,new Font("Arial", Font.PLAIN, 36));
    }

    private static void launchBrowser(String url) {
        int dotIndex = url.indexOf('.');
        String result = url.substring(dotIndex + 1);
        System.out.println("Мне запустили ссылку");
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
