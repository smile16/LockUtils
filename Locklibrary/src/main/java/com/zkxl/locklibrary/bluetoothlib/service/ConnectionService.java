package com.zkxl.locklibrary.bluetoothlib.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.Nullable;

import com.zkxl.locklibrary.bluetoothlib.event.ConnectionEvent;
import com.zkxl.locklibrary.bluetoothlib.event.SearchDeviceEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import com.zkxl.locklibrary.bluetoothlib.BluetoothAttributes;
import com.zkxl.locklibrary.bluetoothlib.event.DeviceServerEvent;
import com.zkxl.locklibrary.bluetoothlib.helper.BluzScanHelper;
import com.zkxl.locklibrary.bluetoothlib.helper.IBluzScanHelper;
import com.zkxl.locklibrary.bluetoothlib.manager.BluzManager;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;

/**
 * @author csym_ios_04
 */
public class ConnectionService extends Service {
    private static final String TAG = "ConnectionService";

    private IBluzScanHelper scanHelper;
    Handler searchHandler;
    /**
     * 断开连接的场景
     */
    private int disconnectContext;
    private IBluzScanHelper.OnDiscoveryListener discoveryListener = devices -> EventBusUtils.post(new SearchDeviceEvent(devices));


    private IBluzScanHelper.OnConnectionListener connectionListener = new IBluzScanHelper.OnConnectionListener() {
        @Override
        public void onConnected(BluetoothDevice device) {
            searchHandler.post(() -> {
//                //无论是否绑定过  都要获取UUID  因为需要判断 设备是自有还是外协
//                BluetoothCommands.gettingUid();
                Log.e("ANubis","蓝牙在ConnectionService中连接成功");
                //当蓝牙设备启动 连接的服务后  开启将蓝牙设备数据写入App的服务  TransferDataService
                startService(new Intent(ConnectionService.this, TransferDataService.class));

            });
        }

        @Override
        public void onDisconnected(BluetoothDevice device) {
            searchHandler.post(() -> {
                Toast.makeText(getApplicationContext(), "蓝牙连接断开", Toast.LENGTH_SHORT).show();
                stopService(new Intent(ConnectionService.this, TransferDataService.class));
            });
        }
    };

    private BluzManager.OnServiceListener serviceListener = new BluzManager.OnServiceListener() {
        @Override
        public void onServicesDiscovered(String address) {
            DeviceServerEvent event = new DeviceServerEvent(DeviceServerEvent.ON_SERVER);
            event.setServerAddr(address);
            EventBusUtils.post(event);
        }

        @Override
        public void onFirmwareUpgrade(String address) {
            DeviceServerEvent event = new DeviceServerEvent(DeviceServerEvent.ON_UPGRADE);
            event.setServerAddr(address);
            EventBusUtils.post(event);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        searchHandler = new Handler(getMainLooper());
        initConfig();
        initBluetoothScanner();
        initBluetoothManager();

        EventBusUtils.register(this);
//        刚启动进行搜索啊
//            handleActionSearch();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        EventBusUtils.unregister(this);
        super.onDestroy();
    }

    private void handleActionSearch() {
        scanHelper.startDiscovery();
    }

    private void handleActionSearchUpgrade() {
        scanHelper.startDiscovery(BluetoothAttributes.UPGRADE_MODE);
    }


    private void handleActionCancelSearch() {
        scanHelper.cancelDiscovery();
    }


    private void handleActionConnection(String deviceAddress) {
        if (!TextUtils.isEmpty(deviceAddress)) {
            scanHelper.connect(deviceAddress);
        }
    }


    private void handleActionDisConnection() {
        scanHelper.disconnect();
    }

    /**
     * 检测蓝牙配置
     */
    private void initConfig() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            stopSelf();
        }
    }


    /**
     * 初始化蓝牙搜索工具
     */
    private void initBluetoothScanner() {
        scanHelper = BluzScanHelper.getInstance(getApplicationContext());
        scanHelper.registerBroadcast(getApplicationContext());
        scanHelper.setOnConnectionListener(connectionListener);
        scanHelper.setOnDiscoveryListener(discoveryListener);
        openBlueTooth();

    }

    private void initBluetoothManager() {
        //此方法可获取到设备型号是自有还是外协
        BluzManager bluzManager = BluzManager.getInstance(getApplicationContext());
        bluzManager.setOnServiceListener(serviceListener);
    }

    /**
     * 开启蓝牙设备
     */
    private void openBlueTooth() {
        if (!scanHelper.isEnabled() || !scanHelper.enable()) {
            scanHelper.openBluetooth();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionActionEvent(ConnectionEvent event) {
        switch (event.getAction()) {
            case ConnectionEvent.SEARCH_ACTION:
                handleActionSearch();
                break;
//            case ConnectionEvent.SEARCH_ACTION_UPGRADE:
//                handleActionSearchUpgrade();
//                break;
            case ConnectionEvent.CANCEL_SEARCH_ACTION:
                handleActionCancelSearch();
                break;
//            case ConnectionEvent.CONNECTION_ACTION:
//                handleActionConnection(event.getDeviceAddress());
//                break;
            case ConnectionEvent.DIS_CONNECTION_ACTION:
                this.disconnectContext = event.getDisConnectContext();
                handleActionDisConnection();
                break;
            default:
                break;
        }
    }

    /**
     * 获取升级的mac
     *
     * @param normalMac
     * @return
     */
    private String getUpgradeMac(String normalMac) {
        String[] strings = normalMac.split(":");
        int i = Integer.parseInt(strings[strings.length - 1], 16);
        String hexString = Integer.toHexString((++i) & 0xFF).toUpperCase();
        StringBuilder builder = new StringBuilder(normalMac);
        builder.replace(normalMac.length() - 2, normalMac.length(), hexString);
        return builder.toString();
    }
}
