package com.zkxl.locklibrary.bluetoothlib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zkxl.locklibrary.bluetoothlib.handler.bean.LockMessageBean;
import com.zkxl.locklibrary.bluetoothlib.helper.BluzScanHelper;
import com.zkxl.locklibrary.bluetoothlib.helper.IBluzScanHelper;
import com.zkxl.locklibrary.bluetoothlib.interf.BlueToothListener;
import com.zkxl.locklibrary.bluetoothlib.service.BluetoothCommands;
import com.zkxl.locklibrary.bluetoothlib.service.TransferDataService;
import com.zkxl.locklibrary.bluetoothlib.utils.Utils;

public class updateLock implements IBluzScanHelper.OnConnectionListener{
    private Context context;
    private BlueToothListener blueToothListener=null;
    private static updateLock updatelock;
    private updateLock(){}

    public static updateLock getInstance(Context context) {
//        EventBusUtils.register(context);
        if (updatelock == null) {
            updatelock = new updateLock();
        }
        return updatelock;
    }

    //用于更新锁信息
    public  boolean connectLockBlueTooth(String deviceAddress, Context context, BlueToothListener listener,LockMessageBean lockMessageBean) {
        if (lockMessageBean.getNAME().getBytes().length>16){
            return false;
        }
        if (lockMessageBean.getAPID().length()>13){
            return false;
        }
        if (!Utils.HeartIsLegal(lockMessageBean.getHBT()+"")){
            return false;
        }
        if (!Utils.HeartIsLegal(lockMessageBean.getMWT()+"")){
            return false;
        }
        if (!Utils.adressIsLegal(lockMessageBean.getADDR()+"")||lockMessageBean.getADDR().length()>8){
            return false;
        }
        if (!Utils.adressIsLegal(lockMessageBean.getCH()+"")||lockMessageBean.getCH().length()>4){
            return false;
        }
        this.context=context;
        this.blueToothListener=listener;
        BluzScanHelper.getInstance(context).setOnConnectionListener(this);
        //连接蓝牙
        boolean connect = BluzScanHelper.getInstance(context).connect(deviceAddress);
        return true;
    }

    //蓝牙连接成功之后方可进行数据传输
    public void sendLockMessage(LockMessageBean lockMessageBean){
                BluetoothCommands.writeLockSetting(lockMessageBean);
    }


    @Override
    public void onConnected(BluetoothDevice device) {
        Log.e("Anubis","updateLock回调回来了");
        Intent intent = new Intent(context, TransferDataService.class);
        context.startService(intent);
        //蓝牙连接成功的回调
        blueToothListener.onBlueToothConnectSucess();

    }

    @Override
    public void onDisconnected(BluetoothDevice device) {
        blueToothListener.onBlueToothConnectFail();
    }
}
