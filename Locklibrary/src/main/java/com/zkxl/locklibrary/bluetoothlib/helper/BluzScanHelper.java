package com.zkxl.locklibrary.bluetoothlib.helper;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.zkxl.locklibrary.bluetoothlib.BluetoothAttributes;
import com.zkxl.locklibrary.bluetoothlib.BluetoothLeService;
import com.zkxl.locklibrary.bluetoothlib.model.ExtendedBluetoothDevice;
import com.orhanobut.logger.Logger;
import com.zkxl.locklibrary.bluetoothlib.utils.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * 蓝牙设备搜索连接管理类
 * Created by ${zhoupeng} on 2016/8/9.
 */
public class BluzScanHelper implements IBluzScanHelper,
        BluetoothLeService.OnConnectionStateChangeListener {

    private static final long DELAY_MILLIS = 5 * 1000;
    private static final long TOATAL_DELAY_MILLIS = 15 * 1000;
    private Handler mScanHandler = null;
    //蓝牙服务类
    private BluetoothLeService mBluetoothLeService;
    //蓝牙适配器
    private BluetoothAdapter bluetoothAdapter;

    private static IBluzScanHelper INSTANCE = null;
    private Context mContext = null;
    private final IntentFilter filter;
    private int mode = BluetoothAttributes.WORK_MODE;
    private TreeSet<ExtendedBluetoothDevice> devices = new TreeSet<>(new Comparator<ExtendedBluetoothDevice>() {
        @Override
        public int compare(ExtendedBluetoothDevice device1, ExtendedBluetoothDevice device2) {
            if (device1.equals(device2)) {
                return 0;
            } else {
                return -(device1.rssi - device2.rssi);
            }
        }
    });


    public Context getActivity() {
        return mContext;
    }

    private AtomicBoolean isDiscovery = new AtomicBoolean(false);


    //连接
    private static final int MSG_CONNECTED = 1;
    //未连接
    private static final int MSG_DISCONNECTED = 2;
    //    private static final int MSG_STOP_SCAN = 3;//停止查找

    //搜索设备
    public static final int SCAN_DEVICES = 4;
    /**
     * 10秒后停止查找搜索.
     */
    //    private static final int SCAN_PERIOD = 10 * 1000;

    private SharePreferenceUtils preferenceUtils = null;
    private ExtendedBluetoothDevice findDevice(final ScanResult result) {
        for (final ExtendedBluetoothDevice device : devices)
            if (device.matches(result))
                return device;
        return null;
    }

    @SuppressLint("MissingPermission")
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            boolean flag = false;
            for (ScanResult result : results) {
                ScanRecord scanRecord = result.getScanRecord();
                //信号强度
                int rssi = result.getRssi();
                String name = result.getDevice().getName();
                if (!TextUtils.isEmpty(name)&&"L312-TOOL".equals(name)) {
                    Log.e("Anubis", name);
                    Log.e("Anubis", rssi+"");
                }
                if ("L312-TOOL".equals(name)&&rssi>-55){
                    SparseArray<byte[]> manufacturerSpecificData = scanRecord.getManufacturerSpecificData();
                    int i = manufacturerSpecificData.keyAt(0);
                    if (i == 41746) {
                        byte[] bytes = manufacturerSpecificData.get(i);
                        byte aByte = bytes[bytes.length - 1];
                        Log.e("bind", aByte+"");
                            flag = true;
                            final ExtendedBluetoothDevice dev = findDevice(result);
                            if (dev == null) {
                                devices.add(new ExtendedBluetoothDevice(result));
                            }else{
                                dev.rssi = result.getRssi();
                                dev.advInt = (result.getTimestampNanos() - dev.time_temp) / 1000000;
                                dev.time_temp = result.getTimestampNanos();
                            }
                    }
                }
            }

            if (isDiscovery.get() && flag == true) {
                if (discoveryListener != null) {
                    discoveryListener.onFound(devices);
                }
            }
        }
    };

    /**
     * 使用handler返回主线程,避免UI层直接操作而导致的奔溃
     */
    private Handler mHandler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                //连接
                case MSG_CONNECTED:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if (connectionListener != null && device != null) {
                        Log.e("Anubis","可以进行数据发送");
                        connectionListener.onConnected(device);
                    }
                    break;
                //未连接
                case MSG_DISCONNECTED:
                    device = (BluetoothDevice) msg.obj;
                    if (connectionListener != null) {
                        connectionListener.onDisconnected(device);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    public static IBluzScanHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BluetoothLeService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluzScanHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    private BluzScanHelper(Context context) {
        this.mContext = context;
        //蓝牙服务
        mBluetoothLeService = BluetoothLeService.getInstance(context);
        mBluetoothLeService.setOnConnectionStateChangeListener(this);
        //蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //SharePreference保存蓝牙断开行为是主动断开还是自动断开
        preferenceUtils = SharePreferenceUtils.getInstance(context);

        HandlerThread handlerThread = new HandlerThread("蓝牙搜索");
        handlerThread.start();
        filter = new IntentFilter();
        mScanHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void registerBroadcast(Context context) {
        Logger.e("注册监听系统蓝牙状态变化广播 ");
        //蓝牙状态改变action
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //蓝牙断开action
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(receiver, filter);
    }

    @Override
    public void unregisterBroadcast(Context context) {
        try {
            if (receiver != null) {
                context.unregisterReceiver(receiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean disable() {
        return bluetoothAdapter != null && bluetoothAdapter.disable();

    }
    @SuppressLint("MissingPermission")
    @Override
    public boolean enable() {
        return bluetoothAdapter != null && bluetoothAdapter.enable();
    }

    @Override
    public void openBluetooth() {
        //打开蓝牙提示框
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(enableBtIntent);
    }

    @Override
    public void startDiscovery() {
        startDiscovery(BluetoothAttributes.WORK_MODE);
    }

    @Override
    public void startDiscovery(int mode) {
        this.mode = mode;
        if (bluetoothAdapter == null) {
            return;
        }
        Logger.e("开始搜索蓝牙 isDiscovering=" + isDiscovery.get());
        //正在查找中，不做处理
        if (isDiscovery.get()) {
            return;
        }
        // 搜索设备
        startScanDevices();
    }

    @Override
    public void cancelDiscovery() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (isDiscovery.get()) {
            stopScanDevices();
        }

        Logger.e("取消搜索蓝牙 isDiscovering=" + isDiscovery.get());
    }


    /**
     * 开始扫描BLE蓝牙设备
     **/
    @SuppressLint("MissingPermission")
    private void startScanDevices() {
        devices.clear();
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(500).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
//        if (mode == BluetoothAttributes.WORK_MODE) {
//            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BluetoothAttributes.UUID_CHARACTERISTICS_SERVICE)).build());
//            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BluetoothAttributes.UUID_DFU_SERVICE)).build());
//            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BluetoothAttributes.UUID_SELF_DFU_SERVICE)).build());
//        } else if (mode == BluetoothAttributes.UPGRADE_MODE) {
//            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BluetoothAttributes.UUID_UART_PROFILE)).build());
//            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BluetoothAttributes.UUID_SELF_DFU_SERVICE)).build());
//        }
        scanner.startScan(null, settings, scanCallback);

        isDiscovery.set(true);
//        mScanHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isDiscovery.get()) {
//                    if (discoveryListener == null) {
//                        mScanHandler.postDelayed(this, DELAY_MILLIS);
//                    } else {
//                        cancelDiscovery();
//                        discoveryListener.onFound(devices);
//                    }
//                }
//            }
//        }, DELAY_MILLIS);

//        mScanHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                cancelDiscovery();
//            }
//        }, TOATAL_DELAY_MILLIS);
    }


    /**
     * 停止扫描蓝牙设备
     */
    @SuppressLint("MissingPermission")
    private void stopScanDevices() {
        mScanHandler.removeCallbacksAndMessages(null);
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        isDiscovery.set(false);
    }


    /**
     * 连接蓝牙设备
     */
    @Override
    public boolean connect(BluetoothDevice device) {
        if (device == null || mBluetoothLeService == null) {
            return false;
        }
        //cancelDiscovery();//取消搜索
        return mBluetoothLeService.connect(device.getAddress());
    }


    /**
     * 连接蓝牙设备
     */
    @Override
    public boolean connect(String macAddress) {
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        //cancelDiscovery();
        return mBluetoothLeService.connect(macAddress);
    }


    /**
     * 断开连接
     */
    @Override
    public void disconnect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            preferenceUtils.save(SharePreferenceUtils.SHARE_REFRESH_IS_MANUAL, true);
        }
    }

    @Override
    public void connected(BluetoothDevice device) {
        Message message = new Message();
        message.obj = device;
        message.what = MSG_CONNECTED;
        mHandler.sendMessage(message);
        preferenceUtils.save(SharePreferenceUtils.SHARE_REFRESH_IS_MANUAL, false);
        //cancelDiscovery();//连接成功后取消搜索
    }

    @Override
    public void disconnected(BluetoothDevice device) {
        Message message = new Message();
        message.obj = device;
        message.what = MSG_DISCONNECTED;
        mHandler.sendMessage(message);
    }

    @Override
    public void release() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
        }
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return mBluetoothLeService.getDevice();
    }

    //查找蓝牙回调接口

    private OnDiscoveryListener discoveryListener;
    //连接蓝牙回调接口
    private OnConnectionListener connectionListener;

    @Override
    public void setOnDiscoveryListener(OnDiscoveryListener listener) {
        discoveryListener = listener;
    }


    @Override
    public void setOnConnectionListener(OnConnectionListener listener) {
        connectionListener = listener;
    }


    /**
     * 查找蓝牙广播回调
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //关闭系统蓝牙
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                isDiscovery.set(false);
                Logger.e("系统蓝牙断开！！");
                boolean isEnable = enable();
                if (!isEnable) {
                    openBluetooth();
                }
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                isDiscovery.set(false);
                Logger.e("系统蓝牙打开！！");
            }
        }
    };
}
