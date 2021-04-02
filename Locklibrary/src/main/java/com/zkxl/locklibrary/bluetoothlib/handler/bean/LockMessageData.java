package com.zkxl.locklibrary.bluetoothlib.handler.bean;

import com.zkxl.locklibrary.bluetoothlib.event.LockWriteSucessEvent;
import com.zkxl.locklibrary.bluetoothlib.service.BluetoothCommands;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;

public class LockMessageData extends BaseData {
    private boolean isWriteSucess;
    public LockMessageData(int[] data) {
        super(data);
    }

    @Override
    protected void handleData() {
        if (data[2]== BluetoothCommands.PRODUCT_ORDER_WRITE_SUCESS){
            isWriteSucess=true;
        }else if (data[2]!=-1){
            isWriteSucess=false;
        }

        EventBusUtils.post(new LockWriteSucessEvent(isWriteSucess));
    }
}
