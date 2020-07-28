/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging;

import com.mac.eiscp.messaging.tcp.EiscpMessageUtility;
import com.mac.eiscp.messaging.tcp.EiscpSocket;
import com.mac.eiscp.constants.EiscpConstant;
import com.mac.eiscp.devicebuilders.DeviceFactory;
import com.mac.eiscp.interfaces.impl.EiscpCommand;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.MessageListener;
import com.mac.eiscp.messaging.tcp.EiscpSocketReaderThread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mac
 */
@Component
public class EiscpMessageHandler implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(EiscpMessageHandler.class);

    private Message autoDiscoverResponse;
    //private String address;
    private boolean discovered;
    private Device workingDevice;

    private EiscpSocketReaderThread readerThread;
    private Thread
            eiscpSocketReaderThreadThread;

    private EiscpSocket socket;

    private static EiscpMessageHandler instance;

    public static void main(String[] args) throws Exception {
        EiscpMessageHandler emh = new EiscpMessageHandler();
        Device dev = emh.getWorkingDevice();
        System.out.println(dev);
        Map<Command, Boolean> cmds = dev.getCommandGroup("SLI");

        Set<Entry<Command, Boolean>> sets = cmds.entrySet();
        Iterator<Entry<Command, Boolean>> entries = sets.iterator();
        while (entries.hasNext()) {
            Entry<Command, Boolean> entry = entries.next();
            Command cmd = entry.getKey();
            if (cmd.getCommand().equalsIgnoreCase("SLI2B")) {
                emh.registerListener(emh);
                //for (int i = 0; i < 4; i++) {

                    emh.submitCommand(cmd);
                    Thread.sleep(50);
                //}
            }
        }
    }

    private EiscpMessageHandler() throws Exception {
        discovered = false;
        workingDevice = autodiscover();
        if (discovered) {
            log.info("discovered");
            init(workingDevice.getDeviceIP(), workingDevice.getDevicePort());
        }
        discovered = false;
    }

    private EiscpMessageHandler(Device device) throws IOException {
        init(device.getDeviceIP(), device.getDevicePort());
    }

    public static EiscpMessageHandler getInstance(Device device) throws Exception {
        if (Objects.isNull(instance)) {
            if (Objects.nonNull(device)) {
                instance = new EiscpMessageHandler(device);
            } else {
                instance = new EiscpMessageHandler();
            }
        }
        return instance;
    }

    public Device getWorkingDevice() {
        return workingDevice;
    }

    public void registerListener(MessageListener listener) {
        if (Objects.nonNull(socket) && Objects.nonNull(listener)) {
            addListener(listener);
        }
    }

    public void submitCommand(Command command) throws Exception {
        if (Objects.nonNull(socket)) {
            if (Objects.nonNull(command)) {
                if (workingDevice.isCompatibleCommand(command)) {
                    socket.sendCommand(command);
                } else {
                    log.info("Command: " + command + " is not supported by this device: " + workingDevice);
                }
            }
        } else {
            log.debug("Socket not connected");
        }
    }
    
    public void queryDevice(MessageListener listener, Command query) throws Exception {
        Message msg = null;
        if (Objects.nonNull(socket)) {
            if (Objects.nonNull(query)) {
                if (workingDevice.isCompatibleCommand(query)) {
                    addListener(listener);
                    socket.sendCommand(query);
                    Thread.sleep(160);
                    removeListener(listener);
                } else {
                    log.info("Command: " + query + " is not supported by this device: " + workingDevice);
                }
            }
        } else {
            log.debug("Socket not connected");
        }
    }

    private void init(InetAddress ip, int port) throws IOException {
        socket = new EiscpSocket(ip, port);
    }

    private Device autodiscover() throws Exception {
        String queryDatagramString = EiscpConstant.AUTODISCOVER_QSTN;

        Command cmd = new EiscpCommand();
        cmd.setName(queryDatagramString);

        Message queryDatagram = Device.buildMessage(cmd);

        int port = EiscpConstant.DEFAULT_EISCP_PORT;

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setBroadcast(true);

        log.debug("send autodiscover datagram: " + queryDatagramString);
        DatagramPacket p = new DatagramPacket(
                queryDatagram.getMessage().getBytes(), queryDatagram.length());
        p.setAddress(
                InetAddress.getByAddress(
                        new byte[]{
                            (byte) 255, (byte) 255, (byte) 255, (byte) 255}));
        p.setPort(port);
        datagramSocket.send(p);

        String address = null;
        while (!discovered) {
            try {
                log.info("waiting for autodiscover answer");
                address = discoverDevice(datagramSocket);
            } catch (Exception ex) {
                throw ex;
            }
        }
        return getDevice(address, autoDiscoverResponse);
    }

    private String discoverDevice(DatagramSocket datagramSocket) throws Exception {
        byte[] buf = new byte[256];

        DatagramPacket pct = new DatagramPacket(buf, buf.length);

        datagramSocket.receive(pct);

        byte[] receivedMessage = new byte[pct.getLength()];
        System.arraycopy(buf, 0, receivedMessage, 0, receivedMessage.length);

        log.debug("answer from " + pct.getSocketAddress());
        autoDiscoverResponse = EiscpMessageUtility.interpreteEiscpResponse(receivedMessage);

        discovered = true;
        return pct.getAddress().getHostAddress();
    }

    public void addListener(MessageListener listener) {
        if (Objects.nonNull(socket)) {
            if (Objects.isNull(readerThread)) {
                readerThread = new EiscpSocketReaderThread(socket.getSocketIn());
                eiscpSocketReaderThreadThread = new Thread(readerThread);
                eiscpSocketReaderThreadThread.start();
                readerThread.addListener(new ConsoleMessageListener());
            }
            readerThread.addListener(listener);
        }
    }

    public void removeListener(MessageListener listener) {
        if (Objects.nonNull(readerThread)) {
            readerThread.removeListener(listener);
        }
    }

    private Device getDevice(String discoveredAddress, Message autodiscoverResponse) throws IOException {
        String[] message = autodiscoverResponse.getMessage().split("/");

        String devName = message[0];
        String port = message[1];
        DeviceFactory df = DeviceFactory.getInstance();

        devName = devName.substring(3);
        Device device = df.getDevice(devName);
        device.setDeviceIP(InetAddress.getByName(discoveredAddress));
        device.setDevicePort(Integer.parseInt(port));

        return device;
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private void close() {
        if (Objects.nonNull(readerThread)) {
            readerThread.quit();
            readerThread = null;
        }
        if (Objects.nonNull(socket)) {
            if (!socket.isClosed()) {
                log.debug("-- closing");

                try {
                    socket.closeSockets();
                    socket = null;
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        if (Objects.nonNull(workingDevice)) {
            workingDevice = null;
        }
    }

    public void switchDevices(Device device) throws IOException {
        if (Objects.nonNull(device)) {
            close();
            workingDevice = device;
            init(workingDevice.getDeviceIP(), workingDevice.getDevicePort());
        }
    }

    @Override
    public void messageReceived(Message message) {
        System.out.println(message.getMessage());
    }
}
