package com.connection.basesftp.xmlhandler;

import com.connection.basesftp.BaseSftpHandler;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler extends BaseSftpHandler {

    private static final String DATA = "data";

    @Override
    @ServiceActivator(inputChannel = DATA, adviceChain = "after")
    public void handleMessage(Message<?> message) {
        super.handleMessage(message);
    }
}
