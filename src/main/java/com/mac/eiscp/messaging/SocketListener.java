/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging;

import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.MessageListener;
import com.mac.eiscp.services.utilities.JsonConverter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Mac
 */
public class SocketListener implements MessageListener {

    private final SocketClient socketClient;

    private final URI wsUri;

    public SocketListener(URI wsUri) throws URISyntaxException {
        this.wsUri = wsUri;
        socketClient = new SocketClient(this.wsUri);//new URI("ws:/localhost:9092/")
    }

    public URI getWsUri() {
        return wsUri;
    }

    @Override
    public void messageReceived(final Message message) {
        try {
            socketClient.sendMessage(JsonConverter.toJsonString(message));
        } catch (IOException ex) {
            Logger.getLogger(SocketListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.wsUri);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SocketListener other = (SocketListener) obj;
        return Objects.equals(this.wsUri, other.wsUri);
    }

    private static class SocketClient extends WebSocketClient {

        private static WebSocket webSocket;

        public SocketClient(URI uri) throws URISyntaxException {
            super(uri);
            super.connect();
        }

        public void sendMessage(String message) {
            new Thread(() -> {
                if (Objects.nonNull(message)) {
                    if (Objects.nonNull(webSocket)) {
                        if (webSocket.isOpen()) {
                            webSocket.send(message);
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onOpen(ServerHandshake sh) {
            System.out.println("Established Connection");
            webSocket = getConnection();
            System.out.println("FROM SOCKETLISTENER: " + webSocket.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(String string) {
        }

        @Override
        public void onClose(int i, String string, boolean bln) {
            if (Objects.nonNull(webSocket)) {
                if (webSocket.isOpen()) {
                    webSocket.close();
                    webSocket = null;
                } else {
                    webSocket = null;
                }
            }
        }

        @Override
        public void onError(Exception excptn) {
        }
    }

}
