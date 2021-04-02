package com.zkxl.locklibrary.bluetoothlib.service;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.zkxl.locklibrary.bluetoothlib.BluetoothAttributes;
import com.zkxl.locklibrary.bluetoothlib.event.ReceiveDataEvent;
import com.zkxl.locklibrary.bluetoothlib.event.SendCommandEvent;
import com.zkxl.locklibrary.bluetoothlib.handler.BaseDataHandler;
import com.zkxl.locklibrary.bluetoothlib.handler.CommandHandlerSelector;
import com.zkxl.locklibrary.bluetoothlib.handler.HandlerSelector;
import com.zkxl.locklibrary.bluetoothlib.handler.bean.BaseData;
import com.zkxl.locklibrary.bluetoothlib.manager.BluzManager;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;

/**
 * @author csym_ios_04
 */
public class TransferDataService extends Service {
    /**
     * 首先开始服务-->onStartCommand-->  bluzManager.setOnNotifyChangedListener(notifyChangedListener) 注册数据返回监听器
     * -->notifyChangedListener回调此接口-->EventBusUtils.post(new ReceiveDataEvent(baseData)) 使用handle发送数据更新Ui
     */


    /**
     * 通知线程
     */
    private Handler notifyHandler;
    /**
     * 写线程
     */
    private Handler writeHandler;
    /**
     * 超时计数线程5秒钟
     */
    private Handler timeoutHandler;

    private BluzManager bluzManager;
    private BlockingQueue<SendCommandEvent> commands = new LinkedBlockingQueue<>(1024);
    //超时时间
    private static final int TIMEOUT_MILLS = 5 * 1000;

    /**
     * 是否是写入的状态
     */
    private AtomicBoolean isWriting = new AtomicBoolean(false);
    /**
     * 发送指令计数器,发送的时候记录当前指令序号,写入成功之后会将指令序号+1,如果5秒之后,现在的序号和刚刚保存的序号不一样
     * 说明发送了新的指令,反之则没有
     */
    private AtomicInteger commandOrder = new AtomicInteger(0);
    private AtomicInteger currentOrder = new AtomicInteger(0);


    /**
     * 超时Runnable
     */
    private class TimeoutRunnable implements Runnable {
        byte[] commands;

        void setCommands(byte[] commands) {
            this.commands = commands;
        }

        @Override
        public void run() {
            //5秒之后,数值未变,说明指令没有发送出去
            Log.e("Anubis","currentOrder.get()=" + currentOrder.get()+"           commandOrder.get()="+commandOrder.get());
            if (commandOrder.get() == currentOrder.get()) {
                Log.e("Anubis","2");
//                sendCommand(commands);
            }
        }
    }

    /**
     * 超时Runnable对象
     */
    private TimeoutRunnable timeoutRunnable = new TimeoutRunnable();

    private BluzManager.OnWriteCharacteristicListener writeCharacteristicListener = (gatt, characteristic, status) -> {
        isWriting.set(false);
        //得到回应了,没有超时的,加1
        commandOrder.addAndGet(1);
        Log.e("Anubis","commandOrder"+commandOrder.get());
        //写入命令
        writeHandler.post(() -> {
            Log.e("SendDataService", "status:" + ((status == BluetoothGatt.GATT_SUCCESS) ? "写入成功" : "写入失败"));
            Log.e("Anubis","status="+status+"            commands.size()="+commands.size());
            if (!commands.isEmpty()) {
                Log.e("Anubis","1");
                sendCommand(commands.remove().getCommands());
            }
        });
    };



    //接口回调回来的数据是从BluzManager.onCharacteristicChanged方法中返回的
    private BluzManager.OnNotifyChangedListener notifyChangedListener = new BluzManager.OnNotifyChangedListener() {
        @Override
        public void onNotifyChanged(String data) {
            notifyHandler.post(new Runnable() {
                @Override
                public void run() {
                    HandlerSelector commandHandler = CommandHandlerSelector.getDataHandler(data);
                    Log.e("Anubis","蓝牙返回数据"+data);
                    if (commandHandler != null) {
                        BaseDataHandler handler = (BaseDataHandler) commandHandler.getDataHandler();
                        if (handler != null) {
                            BaseData baseData = handler.handle();
                            EventBusUtils.post(new ReceiveDataEvent(baseData));
                            Log.e("Anubis","数据结果的返回确认");
                        }
                    }
                }
            });
        }
    };


    public TransferDataService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //启动通知线程
        HandlerThread notifyThread = new HandlerThread("NOTIFY-THREAD");
        notifyThread.start();
        notifyHandler = new Handler(notifyThread.getLooper());
        //启动写入线程
        HandlerThread writeThread = new HandlerThread("WRITE-THREAD");
        writeThread.start();
        writeHandler = new Handler(writeThread.getLooper());
        timeoutHandler = new Handler();

        //启动蓝牙管理器,注册监听器
        bluzManager = BluzManager.getInstance(getApplicationContext());
        bluzManager.setOnNotifyChangedListener(notifyChangedListener);
        bluzManager.setOnWriteCharacteristicListener(writeCharacteristicListener);

        EventBusUtils.register(this);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    /**
     * 发送消息到蓝牙设备
     */
    private void sendCommand(byte[] commands) {
        //开始计时
        isWriting.set(true);
        startTimer(commands);
        Logger.d("蓝牙 开始写入数据  data =" + Arrays.toString(commands));
       StringBuffer buffer=new StringBuffer();
        for (int i = 0; i < commands.length; i++) {
            String command = Integer.toHexString(commands[i]);
            String command2;
            if (command.length()>2) {
                command2 = command.substring(command.length() - 2);
            }else {
                command2=command;
            }
            buffer.append(command2);
            buffer.append(",");
        }
        Log.e("Anubis","蓝牙 开始写入数据  data =" + buffer);

        bluzManager.writeCharacteristic(BluetoothAttributes.UUID_WRITE_PROFILE, commands);

    }

    private void startTimer(byte[] commands) {
        //清除之前的消息
        timeoutHandler.removeCallbacksAndMessages(null);
        //同步当前的序号
        currentOrder.set(commandOrder.get());
        //设置当前的命令,做好重发的准备
        timeoutRunnable.setCommands(commands);
        //启动超时器
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MILLS);
    }

    /*************EventBus消息接收区***********/
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSendCommandEvent(SendCommandEvent event) {
        // 空队列并且当前不是正在写的状态直接发送(为了防止队列为空的时候,一直调用sendCommand而不会入队
        if (commands.isEmpty() && !isWriting.get()) {
            Log.e("Anubis","3");
            sendCommand(event.getCommands());
        }
        //如果不是空的,等前边的发送.排队
        else {
            Log.e("Anubis","0");
            commands.add(event);
        }
    }
}
