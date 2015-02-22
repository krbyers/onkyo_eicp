/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mac.eiscp.constants.EiscpConstant;
import com.mac.eiscp.devices.options.Mute;
import com.mac.eiscp.devices.options.Power;
import com.mac.eiscp.devices.options.Sleep;
import com.mac.eiscp.devices.options.Source;
import com.mac.eiscp.devices.options.Volume;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Device;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Mac
 */
public class SingleDevice implements Device {

    @JsonIgnore
    private final Map<Command, Boolean> commands;
    private String deviceName;
    @JsonIgnore
    private final Map<String, Integer> sheetIndex;
    @JsonIgnore
    private List<Command> validCommands;
    @JsonIgnore
    private int devicePort;
    @JsonIgnore
    private boolean isInit;
    @JsonIgnore
    private InetAddress ipAddress;

    private Power power;
    private Volume volume;
    private Source source;
    private Sleep sleep;
    private Mute mute;

    public SingleDevice() {
        isInit = false;
        sheetIndex = new HashMap();
        this.commands = new HashMap();
        this.devicePort = EiscpConstant.DEFAULT_EISCP_PORT;
        power = new Power();
        volume = new Volume();
        source = new Source();
        sleep = new Sleep();
        mute = new Mute();
    }
    
    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public Volume getVolume() {
        return volume;
    }

    public void setVolume(Volume volume) {
        this.volume = volume;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
    
    public Sleep getSleep(){
        return sleep;
    }
    
    public void setSleep(Sleep sleep){
        this.sleep = sleep;
    }

    public Mute getMute() {
        return mute;
    }

    public void setMute(Mute mute) {
        this.mute = mute;
    }

    @Override
    public void setDeviceName(String name) {
        this.deviceName = name;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @JsonIgnore
    @Override
    public void setDeviceIP(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    @JsonIgnore
    @Override
    public InetAddress getDeviceIP() {
        return this.ipAddress;
    }

    @JsonIgnore
    @Override
    public boolean isCompatibleCommand(Command command) {
        if (Objects.isNull(command)) {
            return false;
        }
        Boolean isValid = this.commands.get(command);
        return isValid == null ? false : isValid;
    }

    @JsonIgnore
    @Override
    public Map<Command, Boolean> getCommandMap() {
        return commands;
    }

    @JsonIgnore
    @Override
    public void addCommand(Command command, boolean isValid) {
        Preconditions.checkNotNull(command, command);
        this.commands.put(command, isValid);
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(deviceName, Charset.defaultCharset()).hash();
        return hc.asInt();
    }

    @JsonIgnore
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SingleDevice other = (SingleDevice) obj;
        return Objects.equals(this.deviceName, other.deviceName);
    }

    @JsonIgnore
    public void putSheetIndex(String sheetName, int indexOnSheet) {
        this.sheetIndex.put(sheetName, indexOnSheet);
    }

    @JsonIgnore
    public int getIndexForSheet(String name) {
        Integer index = this.sheetIndex.get(name);
        return index == null ? -1 : index;
    }

    @JsonIgnore
    @Override
    public Map<Command, Boolean> getCommandGroup(String baseCommand) {
        Map<Command, Boolean> cmdGroup = new HashMap();

        Set<Entry<Command, Boolean>> entries = commands.entrySet();

        entries.stream().filter((entry) -> (entry.getKey().getName()
                .equalsIgnoreCase(baseCommand))).forEach((entry) -> {
                    cmdGroup.put(entry.getKey().cloneCommand(), entry.getValue());
                });
        return cmdGroup;
    }

    @JsonIgnore
    @Override
    public void setDevicePort(int port) {
        this.devicePort = port;
    }

    @JsonIgnore
    @Override
    public int getDevicePort() {
        return this.devicePort;
    }

    @JsonIgnore
    public List<Command> getValidCommands() {
        return getCompatibleCommands();
    }

    @JsonIgnore
    public void setValidCommands(List<Command> validCommands) {
        this.validCommands = validCommands;
    }

    @JsonIgnore
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    @JsonIgnore
    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return getDeviceName() + " @ [IP: " + (Objects.isNull(ipAddress) ? null
                : getDeviceIP().getHostAddress()) + " PORT: " + getDevicePort();
    }

    @JsonIgnore
    @Override
    public List<Command> getCompatibleCommands() {
        if (!isInit) {
            validCommands = new ArrayList();

            Set<Entry<Command, Boolean>> entries = commands.entrySet();

            entries.stream().filter((entry) -> (entry.getValue())).forEach((entry) -> {
                validCommands.add(entry.getKey().cloneCommand());
            });
            Collections.sort(validCommands);
            isInit = true;
        }
        return validCommands;
    }

    /**
     * getCommand:<br><br>
     * Returns a cloned copy of the command if found. Note: A clone copy is sent
     * in order to maintain the integrity of the original command<br>
     * since returning a reference to the original command would allow it to be
     * changed when<br>
     * sending the command to the receiver. A Cloned copy is sent and can be
     * modified without<br>
     * affecting this devices internal command.
     *
     * @param command
     * @return
     */
    @JsonIgnore
    @Override
    public Command getCommand(String command) {
        if (Objects.isNull(command) || command.isEmpty()) {
            return null;
        }
        for (Command cmd : getCompatibleCommands()) {
            if (cmd.getCommand().equalsIgnoreCase(command)) {
                return cmd.cloneCommand();
            }
        }
        return null;
    }
        
}
