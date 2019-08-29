package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {
    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            try {
                int i = 0;
                String userName = null, text =null;
                for (String s : message.split(": ")) {
                    if (i++ == 0)
                        userName = s;
                    else
                        text = s;
                }
                String format=null;
                if (text.equals("дата"))
                    format = "d.MM.YYYY";
                else if (text.equals("день"))
                    format = "d";
                else if (text.equals("месяц"))
                    format = "MMMM";
                else if (text.equals("год"))
                    format = "YYYY";
                else if (text.equals("время"))
                    format = "H:mm:ss";
                else if (text.equals("час"))
                    format = "H";
                else if (text.equals("минуты"))
                    format = "m";
                else if (text.equals("секунды"))
                    format = "s";
                if (format != null){
                    Date currDate = Calendar.getInstance().getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                    BotClient.this.sendTextMessage("Информация для " + userName +": " + dateFormat.format(currDate));
                }
            } catch(Exception e){
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int d = (int) (Math.random() * 100) + 0;
        return String.format("date_bot_%d",d);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
