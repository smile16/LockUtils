package com.zkxl.locklibrary.bluetoothlib.model;

import android.bluetooth.BluetoothDevice;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @author csym_ios_04
 */
public class ExtendedBluetoothDevice {
    public final BluetoothDevice device;
    public String name;
    public int rssi;
    public long advInt = 0;
    public long time_temp = 0;
    /**
     * 是否存在dfu模式
     */
    public boolean isInDfuMode;

    public ExtendedBluetoothDevice(final ScanResult scanResult) {
        this.device = scanResult.getDevice();
        this.name = scanResult.getScanRecord() != null ? scanResult.getScanRecord().getDeviceName() : null;
        this.rssi = scanResult.getRssi();
        this.isInDfuMode = ("XW_OTA".equals(this.name));
    }


    public boolean matches(final ScanResult scanResult) {
        return device.getAddress().equals(scanResult.getDevice().getAddress());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ExtendedBluetoothDevice && device.equals(((ExtendedBluetoothDevice) o).device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }
}