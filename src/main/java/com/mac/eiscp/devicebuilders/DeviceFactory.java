/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devicebuilders;

import com.mac.eiscp.interfaces.Device;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * DeviceFactory manages the handling of devices. Use this class
 * to retrieve a known device by name i.e. TX-NR626
 * @author Mac
 */
@Component
public class DeviceFactory {

    private static DeviceFactory instance;
    private final Set<Device> devices;

    private DeviceFactory() throws IOException {
        CommandMapBuilder cmb = new CommandMapBuilder();

        devices = cmb.buildDeviceSet();
    }

    /**
     * Returns a single instance of this class.
     * @return
     * @throws IOException 
     */
    public static DeviceFactory getInstance() throws IOException{
        if(instance == null){
            instance = new DeviceFactory();
        }
        return instance;
    }
    /**
     * Returns a list of all managed device names.
     * @return 
     */
    public List<String> getDeviceNames() {
        List<String> devNames = null;

        if (Objects.nonNull(devices)) {
            Iterator<Device> allDevices = devices.iterator();
            devNames = new ArrayList();

            while (allDevices.hasNext()) {
                Device dev = allDevices.next();

                devNames.add(dev.getDeviceName());
            }
        }
        return devNames;
    }
    /**
     * Determines if the given name matches a managed device.
     * @param deviceName
     * @return 
     */
    public boolean isExists(String deviceName) {
        return getDeviceNames().contains(deviceName);
    }
    /**
     * Returns a device given a name that matches the managed devices.
     * @param deviceName A managed device name. i.e. TX-NR626
     * @return 
     */
    public final Device getDevice(String deviceName) {
        if (Objects.nonNull(devices)) {
            Iterator<Device> allDevices = devices.iterator();

            while (allDevices.hasNext()) {
                Device dev = allDevices.next();

                if (dev.getDeviceName().equalsIgnoreCase(deviceName)) {
                    return dev;
                }
            }
        }
        return null;
    }

//    public static void main(String[] args) throws IOException {
//        DeviceFactory df = new DeviceFactory();
//
//        Device device = df.getDevice("TX-NR626");
//
//        Map<Command, Boolean> pwrGroup = device.getCommandGroup("SWL");
//
//        Set<Entry<Command, Boolean>> entries = pwrGroup.entrySet();
//
//        entries.stream().filter((entry) -> (entry.getKey().hasRange()))
//                .forEach((entry) -> {
//                    for (int i = 0; i < 20; i++) {
//                        Command cmd = entry.getKey();
//                        cmd.getParameter().getRange().increase();
//                    }
//                });
//    }
}
