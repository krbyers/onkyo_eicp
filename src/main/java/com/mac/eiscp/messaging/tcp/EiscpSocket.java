/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging.tcp;

import com.mac.eiscp.constants.EiscpConstant;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Message;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mac
 */
public class EiscpSocket {

    private static final Logger log = LoggerFactory.getLogger(EiscpSocket.class);

    private final Socket socket;
    private BufferedOutputStream socketOut;
    private BufferedInputStream socketIn;

    private boolean connected;

    public EiscpSocket(InetAddress ip, int port) throws IOException {
        socket = new Socket(ip, port);
        socketOut = new BufferedOutputStream(socket.getOutputStream());
        socketIn = new BufferedInputStream(socket.getInputStream());
        connected = true;
    }

    public BufferedInputStream getSocketIn() {
        return this.socketIn;
    }

    public void closeSockets() throws IOException {
        if (Objects.nonNull(socketOut)) {
            socketOut.flush();
            socketOut.close();
            socketOut = null;
        }

        if (Objects.nonNull(socketIn)) {
            socketIn.close();
            socketIn = null;
        }
        connected = false;
    }

    public boolean isClosed() {
        return !connected;
    }

    /**
     *
     * @param cmd already has prepended !1
     * @throws IOException
     */
    public void sendCommand(Command cmd) throws IOException {
        if (connected) {
            log.debug("sendIscpCommand: " + cmd);
            Message eiscpMessage = EiscpMessageUtility.iscpToEiscpMessage(cmd);

            log.trace("sendIscpMessage: {} - eISCP message: {} bytes - {}",
                    cmd,
                    eiscpMessage.length(),
                    EiscpMessageUtility.convertToHexString(eiscpMessage));

            socketOut.write(eiscpMessage.toBytes());
            socketOut.flush();
        }
    }

    /**
     * Sends to command to the receiver and then waits for the response(s). The
     * responses often have nothing to do with the command sent so this method
     * can filter them to return only the responses related to the command sent.
     *
     * @param command must be one of the Command Class Constants from the
     * @return the response to the command
     * @throws java.lang.Exception
     *
     */
    public String sendQueryCommand(Command command) throws Exception {
        StringBuilder retVal = new StringBuilder();

        /* Send The Command and then... */
        //sendCommand(command);
        //sleep(50); // docs say so

        /* now listen for the response. */
        List<String> rv;
        rv = readQueryResponses(command);
        String currResponse;
        for (String rv1 : rv) {
            currResponse = rv1;
            /* Send ALL responses OR just the one related to the commad sent??? */
            if (currResponse.startsWith(command.getName())) {
                retVal.append(currResponse).append("\n");
            }
        }
        return retVal.toString();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public List<String> readQueryResponses(Command command) throws Exception {
        //boolean debugging = debugging_;
        boolean foundCommand = false;
        ArrayList retVal = new ArrayList();
        byte[] responseBytes = new byte[32];
        String currResponse = "";
        int numBytesReceived;
        int totBytesReceived;
        int i = 0;
        int packetCounter = 0;

        int dataSizeDecimal;
        char endChar1;// NR-5008 response sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
        char endChar2;
        char endChar3;

        if (connected) {
            try {
                socket.setSoTimeout(EiscpConstant.SOCKET_TIMEOUT); // this must be set or the following read will BLOCK / hang the method when the messages are done

                while (!foundCommand && ((numBytesReceived = socketIn.read(responseBytes)) > 0)) {
                    totBytesReceived = 0;
                    StringBuilder msgBuffer = new StringBuilder();
                    System.out.println("\n*\n*\n*\n*Buffering bytes: " + numBytesReceived);
                    System.out.print(" Packet" + "[" + packetCounter + "]:");

                    /* Read ALL the incoming Bytes and buffer them */
                    // *******************************************
                    while (numBytesReceived > 0) {
                        totBytesReceived += numBytesReceived;
                        msgBuffer.append(new String(responseBytes));
                        responseBytes = new byte[32];
                        numBytesReceived = 0;
                        if (socketIn.available() > 0) {
                            numBytesReceived = socketIn.read(responseBytes);
                        }

                        System.out.print(" " + numBytesReceived);

                    }

                    System.out.println();

                    convertStringToHex(msgBuffer.toString());

                    /* Response is done... process it into dataMessages */
                    // *******************************************
                    char[] responseChars = msgBuffer.toString().toCharArray(); // use the charArray to step through

                    System.out.println("responseChars.length=" + responseChars.length);

                    int responseByteCnt = 0;
                    char versionChar = '1';
                    char dataStartChar = '!';
                    char dataUnitChar = '1';
                    //char[] headerSizeBytes = new char[4];
                    char[] dataSizeBytes = new char[4];
                    char[] dataMessage; //init dynamically
                    int dataByteCnt;
                    String dataMsgStr;

                    // loop through all the chars and split out the dataMessages
                    while (!foundCommand && (responseByteCnt < totBytesReceived)) {
                        /* read Header */
                        // 1st 4 chars are the leadIn
                        responseByteCnt += 4;

                        // read headerSize
                        responseByteCnt += 4;
//                        headerSizeBytes[0] = responseChars[responseByteCnt++];
//                        headerSizeBytes[1] = responseChars[responseByteCnt++];
//                        headerSizeBytes[2] = responseChars[responseByteCnt++];
//                        headerSizeBytes[3] = responseChars[responseByteCnt++];

                        // 4 char Big Endian data size;
                        dataSizeBytes[0] = responseChars[responseByteCnt++];
                        dataSizeBytes[1] = responseChars[responseByteCnt++];
                        dataSizeBytes[2] = responseChars[responseByteCnt++];
                        dataSizeBytes[3] = responseChars[responseByteCnt++];

                        System.out.println(" -HeaderSize-");

                        System.out.println(" -DataSize-");

                        dataSizeDecimal = convertHexNumberStringToDecimal(new String(dataSizeBytes));

                        // version
                        versionChar = responseChars[responseByteCnt++];

                        // 3 reserved bytes
                        responseByteCnt += 3;
                        dataByteCnt = 0;

                        // Now the data message
                        dataStartChar = responseChars[responseByteCnt++]; // parse and throw away (like parsley)
                        dataUnitChar = responseChars[responseByteCnt++]; // dito

                        System.out.println("new dataMessage[" + dataSizeDecimal + "]");
                        if (dataSizeDecimal < 4096) {
                            dataMessage = new char[dataSizeDecimal];
                        } else {
                            System.out.println("error data message size hexVal=" + Arrays.toString(dataSizeBytes));
                            break;
                        }

                        /* Get the dataMessage from this response */
                        // NR-5008 response sends 3 chars to terminate the packet - so DON't include them in the message
                        while (dataByteCnt < (dataSizeDecimal - 5) && responseByteCnt < (totBytesReceived - 3)) {
                            dataMessage[dataByteCnt++] = responseChars[responseByteCnt++];
                        }
                        dataMsgStr = new String(dataMessage);

                        System.out.println("dataMessage:\n~~~~~~~~~~~~~");

                        System.out.println(dataMsgStr);

                        retVal.add(dataMsgStr);

                        // Read the end packet char(s) "[EOF]"
                        // [EOF]            End of File        ASCII Code 0x1A
                        // NOTE: the end of packet char (0x1A) for a response message is DIFFERENT that the sent message
                        // NOTE: ITs also different than what is in the Onkyo eISCP docs
                        // NR-5008 sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
                        endChar1 = responseChars[responseByteCnt++];
                        endChar2 = responseChars[responseByteCnt++];
                        endChar3 = responseChars[responseByteCnt++];
                        if (endChar1 == (char) Integer.parseInt("1A", 16)
                                && endChar2 == (char) Integer.parseInt("0D", 16)
                                && endChar3 == (char) Integer.parseInt("0A", 16)) {

                            System.out.println(" EndOfPacket[" + packetCounter + "]\n");

                        }
                        packetCounter++;
                        // Now check if we end early
                        if (dataMsgStr.startsWith(command.getName())) {
                            foundCommand = true;

                            System.out.println("Found Response:" + command.getCommand());
                        }
                    }// done packet
                } // check for more data
            } catch (java.net.SocketTimeoutException noMoreDataException) {
                System.out.println("Response Done: ");
            } catch (EOFException eofException) {
                System.out.println("received: \"" + retVal + "\"");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            System.out.println("!!Not Connected to Receive ");
        }
        return retVal;
    }

    /**
     * Converts an ascii decimal String to a hex String.
     *
     * @param str holding the string to convert to HEX
     * @return a string holding the HEX representation of the passed in str.
     *
     */
    public static String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        String out_put;

        System.out.println("    Ascii: " + str);
        System.out.print("    Hex: ");

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            out_put = Integer.toHexString(chars[i]);
            if (out_put.length() == 1) {
                hex.append("0");
            }
            hex.append(out_put);

            System.out.print("0x" + (out_put.length() == 1 ? "0" : "") + out_put + " ");

        }
        System.out.println();

        return hex.toString();
    }

    /**
     * Converts an HEX number String to its decimal equivalent.
     *
     * @param str holding the Hex Number string to convert to decimal
     * @return an int holding the decimal equivalent of the passed in HEX
     * numberStr.
     *
     */
    public static int convertHexNumberStringToDecimal(String str) {
        char[] chars = str.toCharArray();
        String out_put;

        System.out.println("      AsciiHex: 0x" + str);
        System.out.print("       Decimal: ");

        StringBuilder hex = new StringBuilder();
        String hexInt;
        for (char aChar : chars) {
            out_put = Integer.toHexString(aChar);
            if (out_put.length() == 1) {
                hex.append("0");
            }
            hex.append(out_put);

            System.out.print((out_put.length() == 1 ? "0" : "") + out_put);

        }
        hexInt = "" + (Integer.parseInt(hex.toString(), 16));

        System.out.println();
        System.out.println("      Decimal: " + hexInt);

        return Integer.parseInt(hexInt);
    }

    /**
     * Converts a hex byte to an ascii String.
     *
     * @param hex byte holding the HEX string to convert back to decimal
     * @return a string holding the HEX representation of the passed in str.
     *
     */
    public static String convertHexToString(byte hex) {
        byte[] bytes = {hex};
        return convertHexToString(new String(bytes));
    }

    /**
     * Converts a hex String to an ascii String.
     *
     * @param hex the HEX string to convert backk to decimal
     * @return a string holding the HEX representation of the passed in str.
     *
     */
    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        String out_put;

        System.out.print("    Hex: ");

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            out_put = hex.substring(i, (i + 2));

            System.out.print("0x" + out_put + " ");

            //convert hex to decimal
            int decimal = Integer.parseInt(out_put, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        System.out.println("    Decimal : " + temp.toString());
        return sb.toString();
    }
}
