/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import java.io.Serializable;

/**
 *
 * @author Mac
 */
public interface Byteable extends Serializable{
    byte[] toBytes();
}
