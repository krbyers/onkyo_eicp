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
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.Parameter;
import com.mac.eiscp.interfaces.Range;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author Mac
 */
//@JsonSerialize(using = CommandSerializer.class)
//@JsonDeserialize(using = CommandDeserializer.class)
public class EiscpCommand implements Command {

    private String name;
    private String mainFunction;
    private String function;
    @JsonIgnore
    private String prefix;
    private Parameter param;

    public EiscpCommand() {
        this.name = "";
        this.param = null;
        this.mainFunction = null;
        this.function = null;
        this.prefix = null;
    }

    public EiscpCommand(Command cmd) {
        this();
        if (Objects.nonNull(cmd)) {
            this.name = cmd.getName();
            this.param = new EiscpParameter(cmd.getParameter());
            this.mainFunction = cmd.getCommandMainFunction();
            this.function = cmd.getCommandFunction();
            this.prefix = cmd.getPrefix();
        }
    }
    
    public EiscpCommand(Message msg) {
        this();
        if (Objects.nonNull(msg)) {
            this.name = msg.getMessage().substring(0, 3);
            this.param = new EiscpParameter(msg.getMessage().substring(3));
        }
    }

    @Override
    public void setName(String name) {
        Preconditions.checkNotNull(name, name);
        Preconditions.checkArgument(!name.isEmpty());
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setParameter(Parameter param) {
        Preconditions.checkNotNull(param, param);
        this.param = param;
    }

    @Override
    public Parameter getParameter() {
        return this.param;
    }

    @JsonIgnore
    @Override
    public String getCommand() {
        return Objects.nonNull(prefix)
                && Objects.nonNull(name) && Objects.nonNull(param)
                        ? prefix + name + param.getParameter()
                        : Objects.nonNull(name) && Objects.nonNull(param)
                                ? name + param.getParameter()
                                : Objects.nonNull(param) ? param.getParameter()
                                        : name;
    }

    @Override
    public int length() {
        return Objects.nonNull(param) ? name.length() + param.length()
                : name.length();
    }

    @Override
    public int hashCode() {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(name, Charset.defaultCharset())
                .putInt(this.param.hashCode()).hash();
        return hc.asInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EiscpCommand other = (EiscpCommand) obj;

        return Objects.equals(this.getCommand(), other.getCommand());
    }

    @Override
    public void setCommandFunction(String function) {
        function = function.replaceAll("\\s+", " ");
        this.function = function;
    }

    @Override
    public String getCommandFunction() {
        return this.function;
    }

    @Override
    public void setCommandMainFunction(String mainFunc) {
        if (mainFunc.contains("null")) {
            mainFunc = mainFunc.replace("null", "");
            mainFunc = mainFunc.replaceAll("\\s+", " ");
        }
        this.mainFunction = mainFunc;
    }

    @Override
    public String getCommandMainFunction() {
        return this.mainFunction;
    }

    @Override
    public boolean hasRange() {
        if (Objects.nonNull(this.param)) {
            Range range = this.param.getRange();
            if (Objects.nonNull(range)) {
                return range.getMin() != Integer.MIN_VALUE && range.getMax() != Integer.MIN_VALUE;
            }
        }
        return false;
    }

    @Override
    public void addPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public byte[] toBytes() {
        return getCommand().getBytes();
    }

    @Override
    public String toString() {
        return getCommand();
    }

    @Override
    public Command cloneCommand() {
        Command cmd = new EiscpCommand(this);
        return cmd;
    }

    @Override
    public int compareTo(Command o) {
        if (Objects.nonNull(o)) {
            if(Objects.deepEquals(this, o)){
                return 0;
            }else{
                return this.getCommand().compareTo(o.getCommand());
            }
        }else{
            return -1;
        }
    }
    
    public static class CommandComparator implements Comparator<Command>{

        @Override
        public int compare(Command o1, Command o2) {
            return Objects.equals(01, o2) == true ? 0 : o1.compareTo(o2);
        }
        
    }
}
