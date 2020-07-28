/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devicebuilders;

import com.mac.eiscp.interfaces.impl.SingleDevice;
import com.mac.eiscp.interfaces.impl.SimpleRange;
import com.mac.eiscp.interfaces.impl.EiscpCommand;
import com.mac.eiscp.interfaces.impl.EiscpParameter;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Device;
import com.mac.eiscp.interfaces.Parameter;
import com.mac.eiscp.interfaces.Range;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * CommandMapBuilder converts an Onkyo eiscp command sheet into a mapping of commands
 * to their compatible devices.
 * 
 * @author MacDerson Louis
 */
public class CommandMapBuilder {

    private static final Pattern CMD_PATTERN = Pattern.compile("\"\\w{3}\"\\s*-\\s*.*");

    private static final Pattern RANGE_PATTERN = Pattern.compile("\"\\w{2}\"-\"\\w{2}\"");
    private static final Pattern Wxx_RANGE_PATTERN = Pattern.compile("\\[A-Z]xx");
    private static final Pattern w0w_RANGE_PATTERN = Pattern.compile("\\\"-[C|F]\\\"-\\\"00\\\"-\\\"\\+[C|F]\\\"");

    /**
     * Builds a set of devices with their commands
     * @return set of devices
     * @throws FileNotFoundException if the spreadsheet containing the devices<br>
     * and commands isn't found.
     * @throws IOException if there is an error reading the spreadsheet
     */
    public Set<Device> buildDeviceSet() throws FileNotFoundException, IOException {
        Set<Device> devices = null;
        Command cmd;

        ClassLoader classLoader = getClass().getClassLoader();
        File myFile = new File(classLoader.getResource("eiscp/protocol/ISCP_AVR_2014.xlsx").getFile());
        FileInputStream fis = new FileInputStream(myFile);

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

        if (Objects.nonNull(myWorkBook)) {
            devices = new HashSet();
            int numberOfSheets = myWorkBook.getNumberOfSheets();

            for (int i = 0; i < numberOfSheets; i++) {
                // Return first sheet from the XLSX workbook
                XSSFSheet mySheet = myWorkBook.getSheetAt(0);
                String sheetName = mySheet.getSheetName();
                // Get iterator to all the rows in getCurrent sheet
                Iterator<Row> rowIterator = mySheet.iterator();

                // Traversing over each row of XLSX file
                String commandName = "";
                String cmdFunction = "";
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    if (isDeviceRow(row)) {
                        getDevices(sheetName, devices, row);
                    } else if (isCommandRow(row)) {
                        commandName = getCommandName(row);
                        cmdFunction = getCommandFunction(row);
                    } else {
                        Command command = getCommand(row, commandName, cmdFunction);
                        setCommandDevices(devices, row, command, sheetName);
                    }
                }
            }
        }
        return devices;
    }

    /**
     * Retrieve the devices from the given sheet on the spreadsheet.<br>
     * Note: every sheet of the excel file do not contain the same<br>
     * devices.<br>
     * @param sheetName The name of the given sheet on the excel file.
     * @param devices the Set containing all devices, only new devices<br>
     * are added to this set. if device exist from another sheet, device<br>
     * will not be added.
     * @param row The row on the excel sheet.
     */
    @SuppressWarnings("null")
    private void getDevices(String sheetName, Set<Device> devices, Row row) {

        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();

            String val = cell.getStringCellValue();
            String[] vals = val.split("\\s+");
            List<String> deviceNames = new ArrayList(vals.length);

            for (String str : vals) {
                if (!str.isEmpty()) {
                    if (!(str.contains("(") && str.contains(")"))) {
                        deviceNames.add(str.trim());
                    }
                }
            }

            String lastDeviceName = null;

            for (String deviceName : deviceNames) {
                if (Objects.nonNull(deviceName) && !deviceName.isEmpty()) {

                    SingleDevice device = new SingleDevice();
                    device.putSheetIndex(sheetName, cell.getColumnIndex());

                    if (deviceName.charAt(0) == '/') {
                        if (Objects.nonNull(lastDeviceName)) {
                            String subDeviceName = "";
                            int idx = 0;
                            while (idx < lastDeviceName.length()
                                    && !Character.isDigit(lastDeviceName.charAt(idx))) {
                                subDeviceName += lastDeviceName.charAt(idx);

                                idx++;
                            }
                            subDeviceName += deviceName.substring(1);
                            device.setDeviceName(subDeviceName);
                        }
                    } else {
                        device.setDeviceName(deviceName);
                    }

                    Iterator<Device> iter = devices.iterator();

                    boolean exists = false;
                    while (iter.hasNext()) {
                        Device singleDevice = iter.next();
                        if (singleDevice.hashCode() == device.hashCode()) {
                            ((SingleDevice) singleDevice).putSheetIndex(sheetName, cell.getColumnIndex());
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        devices.add(device);
                    }

                    lastDeviceName = deviceName;
                }
            }
        }
    }

    /**
     * Creates and returns a Command object from the given command name, function<br>
     * and row found on an excel sheet.
     * @param row the row on the excel sheet.
     * @param commandName the name of the command.
     * @param cmdFunction A description of this commands functionality.
     * @return Command.
     */
    @SuppressWarnings("null")
    private Command getCommand(Row row, String commandName, String cmdFunction) {
        Command cmd;

        cmd = new EiscpCommand();
        cmd.setName(commandName);
        cmd.setCommandMainFunction(cmdFunction);

        Iterator<Cell> cellIterator = row.cellIterator();
        String param = null;
        String paramFunc = null;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();

            if (cell.getColumnIndex() == 0) {
                param = cell.getStringCellValue().trim();
            } else if (cell.getColumnIndex() == 1) {
                paramFunc = cell.getStringCellValue().trim();
                cmd.setCommandFunction(paramFunc);
            } else {
                break;
            }
        }

        if (Objects.nonNull(param) && Objects.nonNull(paramFunc)) {
            Matcher m = RANGE_PATTERN.matcher(param);
            Parameter single = new EiscpParameter();
            Range r;
            if (m.matches()) {
                r = getDualRange(param);
                single.setRange(r);
                cmd.setParameter(single);
                return cmd;
            }

            m = Wxx_RANGE_PATTERN.matcher(param);
            if (m.matches()) {
                r = getXXRange(paramFunc);
                single.setRange(r);
                single.setParamName(String.valueOf(param.charAt(0)));
                cmd.setParameter(single);
                return cmd;
            }

            m = w0w_RANGE_PATTERN.matcher(param);
            if (m.matches()) {
                r = getW0WRange(param);
                single.setRange(r);
                cmd.setParameter(single);
                return cmd;
            }

            //System.out.println("PARAM BEFORE: " + param);
            param = param.replaceAll("\"", "");
            //.out.println("PARAM AFTER: " + param);
            single.setParamName(param);
            cmd.setParameter(single);
        }
        return cmd;
    }

    /**
     * Return a Range object containing a min and max value that increments<br>
     * in single steps.
     * @param param
     * @return 
     */
    private Range getDualRange(String param) {
        Range range;

        String[] ranges = param.split("-");
        ranges[0] = ranges[0].replaceAll("\"", "");
        ranges[1] = ranges[1].replaceAll("\"", "");
        int min;
        int max;

        min = Integer.parseInt(ranges[0], 16);
        max = Integer.parseInt(ranges[1], 16);
        range = new SimpleRange(min, max);
        range.setCurrent((max + min) / 2);
        range.setSteps(1);
        return range;
    }

    /**
     * Returns a Range object containing a fixed small range that increments<br>
     * in 1 or 2 steps. range is usually finite i.e. -10 to 10
     * @param paramFunc
     * @return 
     */
    private Range getXXRange(String paramFunc) {
        int min = -10;
        int max = 10;

        Range range;
        range = new SimpleRange(min, max);
        range.setCurrent((max + min) / 2);
        range.setSteps(paramFunc.contains("step") ? 2 : 1);

        return range;
    }

    /**
     * Returns a range object containing a fixed range from -15 to 12
     * @param param
     * @return 
     */
    private Range getW0WRange(String param) {
        int min = 0;
        int max = 0;

        if (param.contains("-F")) {
            min = -15;
        } else if (param.contains("-C")) {
            min = -12;
        }

        if (param.contains("+F")) {
            max = 15;
        } else if (param.contains("+C")) {
            max = 12;
        }

        Range range;
        range = new SimpleRange(min, max);
        range.setCurrent((max + min) / 2);
        range.setSteps(1);

        return range;
    }

    /**
     * Sets the commands for a given device found on a given row of an excel<br>
     * sheet.
     * @param devices The set containing the devices.
     * @param row The row for which a command is defined.
     * @param command The command to associate with all devices found in that row.
     * @param sheetName The name of the sheet in the excel file.
     */
    private void setCommandDevices(Set<Device> devices, Row row, Command command, String sheetName) {
        Iterator<Cell> cellIterator = row.cellIterator();

        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getColumnIndex() < 2) {
                continue;
            }

            Iterator<Device> allDevices = devices.iterator();
            while (allDevices.hasNext()) {
                SingleDevice dev = (SingleDevice) allDevices.next();
                int devIdx = dev.getIndexForSheet(sheetName);

                if (devIdx == cell.getColumnIndex()) {
                    dev.addCommand(command, cell.getStringCellValue().contains("Yes"));
                }

            }
        }

    }

    /**
     * Returns the name of a command in the given row.
     * @param row
     * @return 
     */
    private String getCommandName(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();

        String cmd = null;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();

            String cellValue = cell.getStringCellValue().trim();
            cmd = cellValue.split("\\s+")[0];
            cmd = cmd.replaceAll("\"", "");
            break;
        }
        return cmd;
    }

    /**
     * REturns the commands description.
     * @param row
     * @return 
     */
    @SuppressWarnings("null")
    private String getCommandFunction(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();

        String func = null;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();

            String cellValue = cell.getStringCellValue().trim();
            String[] tokens = cellValue.split("\\s+");
            for (int i = 2; i < tokens.length; i++) {
                func += tokens[i] + " ";
            }
            break;
        }
        return Objects.isNull(func) ? "" : func.trim();
    }

    /**
     * Determines if the given row, contains a master command.
     * @param row
     * @return 
     */
    private boolean isCommandRow(Row row) {
        if (Objects.isNull(row)) {
            return false;
        }

        boolean isCmdRow = false;
        Iterator<Cell> cellIterator = row.cellIterator();
        int cellCount = 0;

        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            cellCount = cell.getColumnIndex();

            String value = cell.getStringCellValue().trim();
            Matcher m = CMD_PATTERN.matcher(value);
            isCmdRow = m.matches();
            if (isCmdRow) {
                break;
            }
        }
        return isCmdRow && cellCount == 0;
    }

    /**
     * Determines if the given row contains the device names.
     * @param row
     * @return 
     */
    private boolean isDeviceRow(Row row) {
        if (Objects.isNull(row)) {
            return false;
        }

        boolean isDeviceRow = false;
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();

            if (cell.getColumnIndex() == 0) {
                isDeviceRow = cell.getRowIndex() == 0 && cell.getStringCellValue().equals("Code");
            } else if (cell.getColumnIndex() == 1) {
                isDeviceRow = cell.getRowIndex() == 0 && cell.getStringCellValue().equals("Means");
            }
            if (isDeviceRow) {
                break;
            }
        }
        return isDeviceRow;
    }

    public static void main(String[] args) throws IOException {
        CommandMapBuilder cmb = new CommandMapBuilder();
        cmb.buildDeviceSet();
    }
}
