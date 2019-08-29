package com.javarush.task.task30.task3008;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private static class Handler extends Thread implements Closeable {
        private Socket socket;

        public Handler(Socket socket) {
            super();
            this.socket = socket;
        }

        @Override
        public void run() {
            String userName = null;
            ConsoleHelper.writeMessage("Установлено новое соединение с адресом:"+ socket.getRemoteSocketAddress().toString());
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                Message newUserMsg = new Message(MessageType.USER_ADDED,userName);
                sendBroadcastMessage(newUserMsg);
                notifyUsers(connection,userName);
                serverMainLoop(connection,userName);
                connectionMap.remove(userName);
                Message userRemove = new Message(MessageType.USER_REMOVED,userName);
                sendBroadcastMessage(userRemove);
                ConsoleHelper.writeMessage("Соединение с удаленным пользователем "+ userName +" закрыто.");
            } catch (IOException|ClassNotFoundException e) {
                if (!(e instanceof java.io.EOFException))
                    ConsoleHelper.writeMessage("Произошла ошибка при соединении с удаленным сервером: " + e.getMessage());
                if (userName != null){
                    connectionMap.remove(userName);
                    Message userRemove = new Message(MessageType.USER_REMOVED,userName);
                    sendBroadcastMessage(userRemove);
                    ConsoleHelper.writeMessage("Соединение с удаленным пользователем "+ userName +" закрыто.");
                }
            }
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String userName;
//            1) Сформировать и отправить команду запроса имени пользователя
           while (true) {
               connection.send(new Message(MessageType.NAME_REQUEST));
//            2) Получить ответ клиента
               Message message = connection.receive();
//            3) Проверить, что получена команда с именем пользователя
//            7) Если какая-то проверка не прошла, заново запросить имя клиента
               if (!message.getType().equals(MessageType.USER_NAME)) continue;
               userName = message.getData();
//            4) Достать из ответа имя, проверить, что оно не пустое и пользователь с таким именем еще не подключен (используй connectionMap)
               if (message.getData().isEmpty()) continue;
               if (connectionMap.containsKey(userName)) continue;
               break;
           }
//            5) Добавить нового пользователя и соединение с ним в connectionMap
            connectionMap.put(userName,connection);
//            6) Отправить клиенту команду информирующую, что его имя принято
            Message answer = new Message(MessageType.NAME_ACCEPTED,userName);
            connection.send(answer);
//            8) Вернуть принятое имя в качестве возвращаемого значения
            return userName;
        }

        //отправка клиенту (новому участнику) информации об остальных клиентах (участниках) чата.
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String s : connectionMap.keySet()) {
                if (s.equals(userName)) continue;
                Message info = new Message(MessageType.USER_ADDED,s);
                connection.send(info);
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true){
                Message rMessage  = connection.receive();
                if (rMessage.getType() != MessageType.TEXT) {
                    ConsoleHelper.writeMessage("Message type is not a TEXT");
                    continue;
                }
                Message sMessage = new Message(MessageType.TEXT, userName+": "+rMessage.getData());
                sendBroadcastMessage(sMessage);
            }
        }
    }

    public static void sendBroadcastMessage(Message message){
        Iterator<Map.Entry<String, Connection>> iterator = connectionMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Connection> el = iterator.next();
            try {
                el.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не смогли отправить сообщение!");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            ConsoleHelper.writeMessage("Server is started...");
            Socket socket = null;
            Handler handler = null;
            while (true) {
                try {
                    socket = server.accept(); // accepting a new clien
                    handler = new Handler(socket);
                    handler.start();
                } catch (Exception e){
                    handler.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
    }
}
