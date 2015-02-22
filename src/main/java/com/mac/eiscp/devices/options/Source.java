/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mac.eiscp.devices.interfaces.CommandStateUpdatable;
import com.mac.eiscp.devices.interfaces.InitializableState;
import com.mac.eiscp.devices.options.sources.AUXILARY;
import com.mac.eiscp.devices.options.sources.BDDVD;
import com.mac.eiscp.devices.options.sources.Bluetooth;
import com.mac.eiscp.devices.options.sources.CBLSAT;
import com.mac.eiscp.devices.options.sources.GAME;
import com.mac.eiscp.devices.options.sources.NET;
import com.mac.eiscp.devices.options.sources.PC;
import com.mac.eiscp.devices.options.sources.Phono;
import com.mac.eiscp.devices.options.sources.TVCD;
import com.mac.eiscp.devices.options.sources.Tuner;
import com.mac.eiscp.devices.options.sources.USB;
import com.mac.eiscp.devices.options.sources.abstracts.SingleSource;
import com.mac.eiscp.interfaces.Command;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mac
 */
public class Source implements CommandStateUpdatable, InitializableState {

    private static final String QSTN = "SLIQSTN";

    private final BDDVD bddvd;
    private final CBLSAT cblSat;
    private final GAME game;
    private final PC pc;
    private final TVCD tvCd;
    private final AUXILARY aux;
    private final Bluetooth bluetooth;
    private final Phono phono;
    private final USB usb;
    private final NET net;
    private final Tuner tuner;

    public Source() {
        bddvd = new BDDVD();
        cblSat = new CBLSAT();
        game = new GAME();
        pc = new PC();
        tvCd = new TVCD();
        aux = new AUXILARY();
        bluetooth = new Bluetooth();
        phono = new Phono();
        usb = new USB();
        net = new NET();
        tuner = new Tuner();
    }

    public BDDVD getBddvd() {
        return bddvd;
    }

    public CBLSAT getCblSat() {
        return cblSat;
    }

    public GAME getGame() {
        return game;
    }

    public PC getPc() {
        return pc;
    }

    public TVCD getTvCd() {
        return tvCd;
    }

    public AUXILARY getAux() {
        return aux;
    }

    public Bluetooth getBluetooth() {
        return bluetooth;
    }

    public Phono getPhono() {
        return phono;
    }

    public USB getUsb() {
        return usb;
    }

    public NET getNet() {
        return net;
    }

    public Tuner getTuner() {
        return tuner;
    }

    @Override
    public void updateState(Command cmd) {
        if (isValid(cmd, "SLI")) {
            System.out.println("SOURCE IS VALID");
            Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                boolean sourceFound = false;
                try {
                    Object value = field.get(this);
                    if (value instanceof SingleSource) {
                        System.out.println("VALUE IS INSTANCEOF SINGLESOURCE");
                        SingleSource sc = (SingleSource) value;
                        String[] acceptableSources = sc.getSourceCommand();
                        if (Objects.nonNull(acceptableSources)) {
                            for (String source : acceptableSources) {
                                if (source.equalsIgnoreCase(cmd.getCommand())) {
                                    sc.setIsSelected(true);
                                    sourceFound = true;
                                    break;
                                }
                            }
                            if(!sourceFound){
                                sc.setIsSelected(false);
                            }
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
                }
                field.setAccessible(false);
            }
        }
    }

    @JsonIgnore
    @Override
    public String getStateQstn() {
        return QSTN;
    }
}
