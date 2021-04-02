package com.zkxl.locklibrary.bluetoothlib.handler;

import com.zkxl.locklibrary.bluetoothlib.handler.bean.BaseData;
import com.zkxl.locklibrary.bluetoothlib.service.BluetoothCommands;

public class BaseLockMessageHandler extends BaseDataHandler{
    public BaseLockMessageHandler(String data) {
        super(data);
    }

    @Override
    protected HandlerSelector getHandlerByFunc(int b) {
        switch (b){
            case BluetoothCommands.PRODUCT_ORDER_WRITE_SUCESS:
                return new LockMessageHandler(data);
            default:return null;
        }
    }

    @Override
    protected BaseData resolve(int[] b) {
        return null;
    }
}
