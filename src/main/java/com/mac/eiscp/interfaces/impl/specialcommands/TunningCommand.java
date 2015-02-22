/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces.impl.specialcommands;

import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Parameter;
import com.mac.eiscp.interfaces.impl.abstractcommands.InputCommand;
import java.util.regex.Pattern;

/**
 * TunningCommand represents a command used for changing the frequency<br>
 * from Main, Zone(1-n), or Port. It is therefore, an InputCommand, since<br>
 * it accepts input from an outside source.<br><br>
 * 
 * @author MacDerson Louis
 */
public class TunningCommand extends InputCommand{

    public static final Pattern TUNNING_PATTERN = Pattern.compile("^([n]+)$");
    
}
