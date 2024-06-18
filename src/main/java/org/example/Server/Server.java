package org.example.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8081;
    private static final int HEARTBEAT_TIMEOUT=10000;
    private Map<String, Socket> clients = new HashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен и ожидает подключения...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = in.readLine();
                    if (command.startsWith("admin_command:")) {
                        handleAdminCommand(clientSocket);
                    } else {
                        handleClientConnection(command, clientSocket);
                        executor.execute(() -> monitorClientConnection(command, clientSocket));
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при обработке клиента: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }

    private void handleAdminCommand(Socket adminSocket) {
        if (adminSocket == null) {
            System.out.println("Админский сокет равен null.");
            return;
        }
        try {
            BufferedReader adminIn = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
            PrintWriter adminOut = new PrintWriter(adminSocket.getOutputStream(), true);

            String command;
            while ((command = adminIn.readLine()) != null) {
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
                String[] parts = command.split(":",3);
                if (parts.length == 3) {
                    String targetUsername = parts[1].trim();
                    String actualCommand = parts[2].trim();
                    if ("server".equals(targetUsername)) {
                        sendInfoToAdmin(adminSocket, actualCommand);
                    }else{
                    sendCommandToClient(targetUsername, actualCommand);
                    adminOut.println("Пользователю отправлена команда");}
                } else {
                    System.out.println("Некорректный формат админской команды: " + command);
                    adminOut.println("Некорректный формат команды. Пожалуйста, попробуйте снова.");
                }
            }

            adminSocket.close();
        } catch (IOException e) {
            System.out.println("Ошибка при обработке администраторской команды: " + e.getMessage());
        }
    }
    private void monitorClientConnection(String username, Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            long lastHeartbeatTime = System.currentTimeMillis();
            boolean clientDisconnected = false; // Флаг для определения, отключился ли клиент

            while (!clientSocket.isClosed() && !clientDisconnected) {
                if (in.ready()) {
                    String message = in.readLine();
                    if ("heartbeat".equals(message)) {
                        lastHeartbeatTime = System.currentTimeMillis();
                    }
                }

                // Проверяем, прошло ли достаточно времени с момента последнего "heartbeat"
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastHeartbeatTime;
                if (elapsedTime > HEARTBEAT_TIMEOUT) {
                    System.out.println("Клиент " + username + " отключился (не получен heartbeat).");
                    clients.remove(username);
                    clientDisconnected = true; // Устанавливаем флаг, что клиент отключился
                }
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientConnection(String username, Socket clientSocket) throws IOException {
        System.out.println("Подключился клиент: " + username);
        clients.put(username, clientSocket);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("Вы успешно подключились к серверу, " + username + "!");

    }

    private void sendInfoToAdmin(Socket adminSocket, String command) {
        try {
            PrintWriter adminOut = new PrintWriter(adminSocket.getOutputStream(), true);
            switch (command) {
                case "users":
                    adminOut.println(clients.keySet());
                    break;
                default:
                    adminOut.println("Неверно введедна команда");
                    break;
            }
        } catch (IOException e) {
            System.out.println("Ошибка при отправке информации администратору: " + e.getMessage());
        }
    }

    private void sendCommandToClient(String targetUsername, String command) {
        Socket targetSocket = clients.get(targetUsername);
        if (targetSocket != null) {
            try {
                PrintWriter out = new PrintWriter(targetSocket.getOutputStream(), true);
                System.out.println("Админская команда пользователю:"+ targetUsername+ "Команда: "+ command);
                out.println(command);
            } catch (IOException e) {
                System.out.println("Ошибка при отправке команды клиенту: " + e.getMessage());
            }
        } else {
            System.out.println("Клиент с именем " + targetUsername + " не найден.");
        }
    }
}
