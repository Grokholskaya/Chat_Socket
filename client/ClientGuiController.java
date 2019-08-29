package com.javarush.task.task30.task3008.client;

public class ClientGuiController extends Client {
    public class GuiSocketThread extends SocketThread{
        @Override
        protected void processIncomingMessage(String message) {
            //должен устанавливать новое сообщение у модели и
            // вызывать обновление вывода сообщений у представления.
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            //должен добавлять нового пользователя в модель и вызывать
            // обновление вывода пользователей у отображения.
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            //должен удалять пользователя из модели и вызывать
            // обновление вывода пользователей у отображения.
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }
    private ClientGuiModel model;
    private ClientGuiView view;

    public ClientGuiModel getModel() {
        return model;
    }

    public ClientGuiController() {
        this.model = new ClientGuiModel();
        this.view = new ClientGuiView(this);
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        //должен получать объект SocketThread через метод getSocketThread() и вызывать у него метод run()
       getSocketThread().run();
       //почему нет необходимости вызывать метод run() в отдельном потоке,
        // как мы это делали для консольного клиента???
    }

    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    public static void main(String[] args) {
        new ClientGuiController().run();
    }
}
