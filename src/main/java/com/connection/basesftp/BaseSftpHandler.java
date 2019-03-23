package com.connection.basesftp;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.io.IOException;
import java.util.Objects;

public abstract class BaseSftpHandler implements MessageHandler {


    public void handleMessage(Message<?> message) {
        String filename = Objects.requireNonNull(message.getHeaders().get("file_remoteFile")).toString();
        System.out.println(message.getHeaders());
        byte[] payload = (byte[]) message.getPayload();

        System.out.println(filename);

        try {
            System.out.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
