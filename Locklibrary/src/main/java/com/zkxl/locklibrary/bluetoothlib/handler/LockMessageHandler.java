package com.zkxl.locklibrary.bluetoothlib.handler;

import com.zkxl.locklibrary.bluetoothlib.handler.bean.BaseData;
import com.zkxl.locklibrary.bluetoothlib.handler.bean.LockMessageData;

public class LockMessageHandler extends BaseDataHandler{
    public LockMessageHandler(String data) {
        super(data);
    }

    @Override
    protected BaseData resolve(int[] b) {
        return new LockMessageData(b);
    }
}
