/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.services.controllers;

import com.mac.eiscp.devicebuilders.DeviceFactory;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.MessageListener;
import com.mac.eiscp.messaging.EiscpMessageHandler;
import com.mac.eiscp.messaging.SocketListener;
import com.mac.eiscp.services.controllers.status.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Mac
 */
@RestController
public class DeviceController implements MessageListener {

    private static final String QSTN_STRING = "QSTN";
    private static final String DEFAULT_DEVICE = "TX-NR626";
    @Autowired
    private DeviceFactory dvf;
    @Autowired
    private EiscpMessageHandler msgHandler;

    private String query;
    private Message response;

    @RequestMapping(value = "/{device}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Device device(@PathVariable("device") String device) {
        String deviceName = Optional.ofNullable(device).orElse(DEFAULT_DEVICE);
        return !deviceName.isEmpty() && dvf.isExists(deviceName) ? dvf.getDevice(deviceName) : null;
    }

    /**
     * This should be a POST or a PUT
     *
     * @param device the name of the device to send the command to (defaults to
     * TX-NR626 if<br>
     * device name is null, empty or incorrect.
     * @param command the fully qualified command to send to the given device
     * @return
     */
    @RequestMapping(value = "/{device}/{command}", produces = "application/json;charset=UTF-8")
    public Status sendCommand(@PathVariable("device") String device, @PathVariable("command") String command) {
        Device dev = device(device);

        if (Objects.nonNull(dev)) {
            Command cmd = dev.getCommand(command);
//            if (!cmd.getCommand().contains(QSTN_STRING)) {
                if (dev.isCompatibleCommand(cmd)) {
                    try {
                        msgHandler.submitCommand(cmd);
                        return Status.SUCCESS;
                    } catch (Exception ex) {
                        Logger.getLogger(DeviceController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
//            }
        }
        return Status.FAILED;
    }

    @RequestMapping(value = "/query/{device}/{query}", produces = "application/json;charset=UTF-8")
    public Message query(@PathVariable("device") String device, @PathVariable("query") String query) {
        response = null;
        String cmd = Optional.ofNullable(query).orElse("").toUpperCase();
        if (!cmd.isEmpty() && cmd.contains(QSTN_STRING)) {

            Device dev = device(device);

            if (Objects.nonNull(dev)) {
                Command command = dev.getCommand(cmd);
                if (dev.isCompatibleCommand(command)) {
                    this.query = command.getName();
                    try {
                        msgHandler.queryDevice(this, command);
                    } catch (Exception ex) {
                        Logger.getLogger(DeviceController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return response;
    }

    @RequestMapping(value = "/{device}/commands", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public List<Command> commands(@PathVariable("device") String device) {
        Device dev = device(device);

        if (Objects.nonNull(dev)) {
            return dev.getCompatibleCommands();
        }
        return null;
    }

    @RequestMapping(value = "/listener/{protocol}/{host}/{port}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String addListener(@PathVariable("protocol") String protocol, @PathVariable("host") String host, 
            @PathVariable("port") String port) {
        if (Objects.nonNull(protocol) && Objects.nonNull(host)) {
            switch (protocol.toUpperCase()) {
                case "HTPP": {

                    break;
                }
                case "WS": {
                    try {
                        SocketListener sl = new SocketListener(new URI(protocol + "://" + (host.equalsIgnoreCase("local")? "localhost" : host) + ":" + port + "/"));
                        msgHandler.registerListener(sl);
                        return Status.OK.name();
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(DeviceController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
        }
        return Status.FAILED.name();
    }

    @Override
    public void messageReceived(Message message) {
        if (message.getMessage().contains(query)) {
            response = message;
        }
    }
}
