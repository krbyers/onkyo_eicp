/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging.tcp;

import com.google.common.collect.Sets;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.MessageListener;
import com.mac.eiscp.interfaces.impl.EiscpMessage;
import com.mac.eiscp.messaging.exceptions.EiscpException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mac
 */
public class EiscpSocketReaderThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EiscpSocketReaderThread.class);

    BufferedInputStream socketIn;
    /**
     * use thread safe implementation of collection for supporting concurrent
     * adding/removing of listener
     */
    Collection<MessageListener> listenerList;
    Message lastMsgSent;

    private volatile boolean quit;

    public EiscpSocketReaderThread(BufferedInputStream socketIn) {
        this.socketIn = socketIn;
        listenerList = Sets.newConcurrentHashSet();
        quit = false;
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void run() {
        byte[] response = new byte[4];
        EiscpMessage responseValidator = new EiscpMessage();

        while (!quit) {
            log.trace("readLoop");
            try {
                blockedReadQuadrupel(response);
                responseValidator.setMessage(new String(response));
                EiscpMessageUtility.validateIscpSignature(responseValidator, 0);

                blockedReadQuadrupel(response);
                responseValidator.setMessage(new String(response));
                EiscpMessageUtility.validateHeaderLengthSignature(responseValidator, 0);

                blockedReadQuadrupel(response);
                responseValidator.setMessage(new String(response));
                int messageSize = EiscpMessageUtility.readMessageSize(responseValidator, 0);

                blockedReadQuadrupel(response);
                responseValidator.setMessage(new String(response));
                EiscpMessageUtility.validateEiscpVersion(responseValidator, 0);

                // eISCP encapulation-header ends here - ISCP begins !1xxx
                byte[] iscpMessage = new byte[messageSize];
                for (int i = 0; i < messageSize; i++) {
                    iscpMessage[i] = (byte) socketIn.read();
                }

                Message responseMessage = EiscpMessageUtility.parseIscpMessage(iscpMessage);

                try {
                    fireReceivedIscpMessage(responseMessage);
                } catch (Throwable ex) {
                    log.error("error in listener {}", ex.getMessage(), ex);
                }
            } catch (EiscpException ex) {
                log.warn(ex.getMessage() + " - " + EiscpMessageUtility.convertToHexString(response));
                log.debug("skip bytes until EOF/CR");

                if (isEofMarkerfInArray(response)) {
                    log.debug("found eof in response block");
                } else {
                    boolean eofFound = false;
                    try {
                        while (!eofFound) {
                            byte b = (byte) socketIn.read();
                            if (b == -1) {
                                log.debug("end of stream");
                                quit();
                                eofFound = true;
                            } else {
                                log.debug("discard " + EiscpMessageUtility.convertToHexString(new byte[]{b}));
                                eofFound = EiscpMessageUtility.isEofMarker(b);
                            }
                        }
                        log.trace("found EOF");
                    } catch (Exception ex2) {
                        log.error("not handled", ex2);
                    }
                }
            } catch (Exception ex) {
                log.warn(ex.getMessage());
                ex.printStackTrace();
                quit();
            }
        }

        if (Objects.nonNull(socketIn)) {
            try {
                socketIn.close();
                socketIn = null;
                listenerList.clear();
                listenerList = null;
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }
    
    public void fireReceivedIscpMessage(Message msg) {
        if (Objects.isNull(lastMsgSent) || !lastMsgSent.getMessage().equalsIgnoreCase(msg.getMessage())) {
            listenerList.stream().forEach((listener) -> {
                listener.messageReceived(msg);
            });
            lastMsgSent = msg;
        }
    }

    public void addListener(MessageListener listener) {
        if (Objects.nonNull(listener)) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        }
    }

    public void removeListener(MessageListener listener) {
        if (Objects.nonNull(listener)) {
            listenerList.remove(listener);
        }
    }

    public boolean isEofMarkerfInArray(byte[] response) {
        boolean eofFound = false;
        for (int i = 0; i < response.length; i++) {
            eofFound = eofFound || EiscpMessageUtility.isEofMarker(response[i]);
        }
        return eofFound;
    }

    private void blockedReadQuadrupel(byte[] bb) throws IOException {
        bb[0] = (byte) socketIn.read();
        bb[1] = (byte) socketIn.read();
        bb[2] = (byte) socketIn.read();
        bb[3] = (byte) socketIn.read();
    }

    public void quit() {
        quit = true;
    }

}
