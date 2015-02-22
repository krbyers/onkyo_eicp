/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging.tcp;

import com.mac.eiscp.constants.EiscpConstant;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.impl.EiscpMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mac
 */
public class EiscpMessageUtility {

    private static final Logger log = LoggerFactory.getLogger(EiscpMessageUtility.class);

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /*
     * ISCP encapsulated in Ethernet (eISCP)
     */
    public static Message iscpToEiscpMessage(Command cmd) throws IOException {
        cmd.addPrefix("!1");
        byte[] messageBytes = cmd.toBytes();

        int dataSize = messageBytes.length + 2;
        if (dataSize > 255) {
            throw new IOException("dataSize > 255 not implemented");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write("ISCP".getBytes());

        // header size - fixed
        bos.write(0);
        bos.write(0);
        bos.write(0);
        bos.write(0x10);

        // dataSize - always < 255
        bos.write(intToByteArray(dataSize));

        // begin eISCP block
        bos.write(1);	// eISCP version 1
        bos.write(0);	// reserved block
        bos.write(0);	// reserved block
        bos.write(0);	// reserved block

        bos.write(messageBytes);

        bos.write(EiscpConstant.CR);
        bos.write(EiscpConstant.LF);

        byte[] eiscpMessage = bos.toByteArray();

        Message msg = new EiscpMessage();
        msg.setCommand(cmd);
        msg.setMessage(new String(eiscpMessage));

        return msg;
    }

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    /**
     * eISCP -> ISCP
     *
     * @param response
     * @return
     * @throws Exception
     */
//    public static Message parseEiscpToIscpMessage(byte[] response) throws Exception {
//        validateIscpSignature(response, 0);
//        validateHeaderLengthSignature(response, 4);
//        int messageSize = readMessageSize(response, 8);
//
//        validateEiscpVersion(response, 12);
//
//        int messageOffset = 12 + 4;
//        //messageSize = messageSize - 4; // skip version
//        byte[] messageBytes = new byte[messageSize];
//
//        System.arraycopy(response, messageOffset, messageBytes, 0, messageSize);
//        return new EiscpMessage(messageBytes);
//    }

    private static byte[] parseEiscpToIscpMessage(Message response) throws Exception {
        validateIscpSignature(response, 0);
        validateHeaderLengthSignature(response, 4);
        int messageSize = readMessageSize(response, 8);

        validateEiscpVersion(response, 12);

        int messageOffset = 12 + 4;
        //messageSize = messageSize - 4; // skip version
        byte[] messageBytes = new byte[messageSize];

        System.arraycopy(response.toBytes(), messageOffset, messageBytes, 0, messageSize);
        return messageBytes;
    }

    private static void validateEiscpVersion(byte[] response, int offset)
            throws Exception {
        if ((response[offset++] != 0x01) || // version
                (response[offset++] != 0x00)
                || (response[offset++] != 0x00)
                || (response[offset++] != 0x00)) {
            throw new Exception("illegal version != 01 00 00 00");
        }
    }
    
    public static void validateEiscpVersion(Message msg, int offset)
            throws Exception {
        validateEiscpVersion(msg.toBytes(), offset);
    }

    private static int readMessageSize(byte[] b, int offset)
            throws Exception {
        int messageSize
                = b[offset + 3] & 0xFF
                | (b[offset + 2] & 0xFF) << 8
                | (b[offset + 1] & 0xFF) << 16
                | (b[offset + 0] & 0xFF) << 24;
        offset += 4;
        return messageSize;
    }
    
    public static int readMessageSize(Message msg, int offset)
            throws Exception {
        return readMessageSize(msg.toBytes(), offset);
    }

    private static void validateHeaderLengthSignature(byte[] response, int offset)
            throws Exception {
        if ((response[offset++] != 0x00)
                || (response[offset++] != 0x00)
                || (response[offset++] != 0x00)
                || (response[offset++] != 0x10)) {
            throw new Exception("illegal header size != 0x10");
        }
    }

    public static void validateHeaderLengthSignature(Message msg, int offset)
            throws Exception {
        validateHeaderLengthSignature(msg.toBytes(), offset);
    }

    private static void validateIscpSignature(byte[] response, int offset)
            throws Exception {
        if ((response[offset++] != 0x49) || // I
                (response[offset++] != 0x53) || // S
                (response[offset++] != 0x43) || // C
                (response[offset++] != 0x50) // P
                ) {
            throw new Exception("illegal signature != ISCP");
        }
    }

    public static void validateIscpSignature(Message msg, int offset)
            throws Exception {
        validateIscpSignature(msg.toBytes(), offset);
    }

    public static Message parseIscpMessage(byte[] iscpMessage) throws Exception {
        if ((iscpMessage[0] == 0x21) // !
                && (iscpMessage[1] == 0x31)) { 	// 1

            int SKIP_PREFIX_LEN = 2;	// !1

            int length = iscpMessage.length;
            // up to three EOF-marker possible
            if (isEofMarker(iscpMessage[length - 1])) {
                length -= 1;
                if (isEofMarker(iscpMessage[length - 1])) {
                    length -= 1;
                    if (isEofMarker(iscpMessage[length - 1])) {
                        length -= 1;
                    }
                }
            }

            String res = new String(iscpMessage, SKIP_PREFIX_LEN, length - SKIP_PREFIX_LEN, UTF_8);
            Message msg = new EiscpMessage();
            msg.setMessage(res);
            return msg;
        } else {
            throw new Exception("wrong ISC signature");
        }
    }

    public static boolean isEofMarker(byte b) {
        return ((b == EiscpConstant.CR)
                || (b == EiscpConstant.LF)
                || (b == EiscpConstant.EOF));
    }

    /**
     * @param response
     * @return
     * @throws Exception
     *
     */
    private static Message interpreteEiscpResponse(Message response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("response: " + response.length() + " bytes - " + convertToHexString(response));
        }
        byte[] iscpMessage = parseEiscpToIscpMessage(response);
        if (log.isDebugEnabled()) {
            log.debug("iscpMessage: " + convertToHexString(iscpMessage));
        }
        return parseIscpMessage(iscpMessage);
    }
    
    public static Message interpreteEiscpResponse(byte[] response) throws Exception {
        return interpreteEiscpResponse(new EiscpMessage(response));
    }

    /**
     * converts a message into human readable hex representation like 49 53 43
     * 50 00 00 00 10 00 00 00 1a 01 00 00 00 21 31 4d 56 4c 51 53 54 4e 0d
     *
     * @param message
     * @return
     */
    public static String convertToHexString(Message message) {
        String ret = "";
        byte[] byteArray = message.toBytes();
        for (int i = 0; i < byteArray.length; i++) {
            if (i > 0) {
                ret += " ";
            }
            if (byteArray[i] < 16) {
                ret += "0";
            }
            ret += Integer.toString(byteArray[i], 16);
        }
        return ret.toUpperCase();
    }

    public static String convertToHexString(byte sByte) {
        Message msg = new EiscpMessage();
        msg.setMessage("" + sByte);
        return convertToHexString(msg);
    }

    public static String convertToHexString(byte[] bytes) {
        Message msg = new EiscpMessage();
        msg.setMessage(new String(bytes));
        return convertToHexString(msg);
    }
}
