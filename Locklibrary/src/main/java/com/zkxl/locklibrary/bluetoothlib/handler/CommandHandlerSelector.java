package com.zkxl.locklibrary.bluetoothlib.handler;


import com.zkxl.locklibrary.bluetoothlib.service.BluetoothCommands;

/**
 * Date: 17/11/2
 * Time: 09:46
 * Description: 介绍这个类
 *
 * @author csym_ios_04.
 */

public class CommandHandlerSelector {
    public static HandlerSelector getDataHandler(String data) {
        String[] results = data.split("-");
        if ( Integer.parseInt(results[0], 16)==0xa5&&
                Integer.parseInt(results[1], 16)==0xb1&&
                Integer.parseInt(results[2], 16)==0xb1&&
                Integer.parseInt(results[3], 16)==0x00
        ){
            return null ;
        }
        if (results.length >= 2) {
            int b = Integer.parseInt(results[0], 16);
            switch (b) {
                case 0xa5:
                        int i = Integer.parseInt(results[4], 16);
                        if (i == BluetoothCommands.PRODUCT_ORDER_WRITE_SUCESS) {
                            return new BaseLockMessageHandler(data);
                        } else {
                            return null;
                        }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }
}
