/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.interfaces;

import com.mac.eiscp.interfaces.Command;
import java.util.Objects;

/**
 *
 * @author Mac
 */
public interface CommandStateUpdatable {

    void updateState(Command cmd);

    public default boolean isValid(Command cmd, String... comparable) {
        boolean isNonNull = Objects.nonNull(cmd);
        if (!isNonNull) {
            return false;
        }
        if (Objects.isNull(comparable) || comparable.length == 0) {
            return true;
        } else {
            for (String str : comparable) {
                if (cmd.getName().equalsIgnoreCase(str.substring(0, 3))) {
                    return true;
                }
            }
        }
        return false;
    }
}
