package com.zkxl.locklibrary.bluetoothlib.event;

/**
 * Date: 17/11/1
 * Time: 18:03
 * Description: 蓝牙连接状态变更事件
 *
 * @author csym_ios_04.
 */

public class DeviceServerEvent {
    /**蓝牙连接成功*/
    public static final int ON_CONNECTED = 0x00;
    /**蓝牙连接断开*/
    public static final int ON_DISCONNECTED = 0x01;
    /**蓝牙进入工作模式*/
    public static final int ON_SERVER = 0x03;
    /**蓝牙进入了升级模式*/
    public static final int ON_UPGRADE = 0x04;

    /**
     * {@linkplain ConnectionEvent#disconnectContext}
     * 断开的场景
     */
    private int disconnectContext;

    private int type;
    /**
     * 当前正在服务的serviceAddr
     */
    private String serverAddr;

    public DeviceServerEvent(int type) {
        this.type = type;
    }

    public DeviceServerEvent(int type, int disconnectContext) {
        this.type = type;
        this.disconnectContext = disconnectContext;
    }

    public int getType() {
        return type;
    }

    public int getDisconnectContext() {
        return disconnectContext;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
