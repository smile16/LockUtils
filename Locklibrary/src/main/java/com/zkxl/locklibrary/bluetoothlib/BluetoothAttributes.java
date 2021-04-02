package com.zkxl.locklibrary.bluetoothlib;

/**
 * 服务通道和特征通道
 * Created by ${zhoupeng} on 2016/8/9.
 * @author csym_ios_04
 * fuction:蓝牙设备UUID常量类
 */
public class BluetoothAttributes {
    public static final int WORK_MODE = 0x00;
    public static final int UPGRADE_MODE = 0x01;

    /**
     * 蓝牙特征服务uuid
     */
    public static final String UUID_CHARACTERISTICS_SERVICE = "00002902-0000-1000-8000-00805f9b34fb";
    /**
     *可用的特征服务uuid
     */
     public static final String UUID_UART_PROFILE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";//价签特征UUID
     public static final String UUID_WRITE_PROFILE = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";//write character
     public static final String UUID_READ_PROFILE = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";//read character



}
