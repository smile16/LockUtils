package com.zkxl.locklibrary.bluetoothlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.zkxl.locklibrary.bluetoothlib.event.ConnectFailEvent;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;

/**
 * 蓝牙服务管理类
 * Created by ${zhoupeng} on 2016/8/10.
 */
public class BluetoothLeService {

    //用来获取蓝牙适配器

    private BluetoothManager mBluetoothManager;
    /**
     * 蓝牙适配器，处理系统蓝牙是否打开，搜索设备
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 发现蓝牙服务，根据特征值处理数据交互
     */
    private BluetoothGatt mBluetoothGatt;
    //蓝牙设备
    private BluetoothDevice mBluetoothDevice = null;
    private static Boolean actionConnectionPriority = false;
    private static int actionConnectionPriorityValue = 100;
    /**
     * 单列模式
     */

    private static BluetoothLeService INSTANCE = null;
    /**
     * 上下文
     */
    private static Context mContext;

    private Handler writeHandler;

    private HashMap<UUID, BluetoothGattCharacteristic> mGattHashMap;

    public void initData(HashMap<UUID, BluetoothGattCharacteristic> mGattHashMap) {
        this.mGattHashMap = mGattHashMap;
    }

    /**
     * 是否连接
     */
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    public boolean isConnected() {
        return isConnected.get();
    }

    public void setConnected(boolean connected) {
        isConnected.set(connected);
    }

    /**
     * 获取上下文
     *
     * @return context
     */
    private Context getContext() {
        return mContext;
    }

    private BluetoothLeService() {
        boolean value = initialize();
        if (!value) {
            Logger.e("蓝牙适配器adapter初始化失败!!!");
        }
    }

    /**
     * 单列模式，确保唯一性
     *
     * @param context 上下文
     * @return 对象
     */
    public static BluetoothLeService getInstance(Context context) {
        mContext = context;
        if (INSTANCE == null) {
            synchronized (BluetoothLeService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothLeService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化bluetooth adapter
     *
     * @return 为null返回false
     */

    private boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (mBluetoothManager == null) {
            return false;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            HandlerThread thread = new HandlerThread("写数据线程");
            thread.start();
            writeHandler = new Handler(thread.getLooper());
            return true;
        }
        else {
            return false;
        }
    }

    public void requestConnectionPriority()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && actionConnectionPriority == false && mBluetoothGatt != null) {
            actionConnectionPriority = mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }
    }

    /**
     * 启用或者禁用通知\标志返回特性 true, if the requested notification status was set successfully
     *
     * @param characteristic 蓝牙特征对象
     * @param enabled        是否允许
     * @return 设置成功或失败
     */

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        return !(mBluetoothAdapter == null || mBluetoothGatt == null) && mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    //    public boolean writeDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
    //        return !(bluetoothGattDescriptor == null || mBluetoothGatt == null) && mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
    //    }

    public void writeDescriptor(final BluetoothGattDescriptor bluetoothGattDescriptor) {
        if (!(bluetoothGattDescriptor == null || mBluetoothGatt == null)) {
            writeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean b = mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
                }
            }, 0);
        }
    }


    public  boolean writeDescriptorWorkaround(final BluetoothGattDescriptor descriptor) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || descriptor == null) {
            return false;
        }
         final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
        final int originalWriteType = parentCharacteristic.getWriteType();
        parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final boolean result = gatt.writeDescriptor(descriptor);
        parentCharacteristic.setWriteType(originalWriteType);
        return result;
    }

    /**
     * 发现数据通道服务
     *
     * @return true or false
     */

    public boolean discoverServices() {
        return !(!isConnected() || mBluetoothGatt == null) && mBluetoothGatt.discoverServices();
    }

    /**
     * 读取蓝牙数据
     *
     * @param characteristic 蓝牙特征值
     * @return true or false
     */

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return !(mBluetoothAdapter == null || mBluetoothGatt == null) && mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * 往设备中写入数据
     *
     * @param characteristic 蓝牙特征值
     * @return true or false
     */

    public boolean writeCharecteristic(BluetoothGattCharacteristic characteristic) {
        requestConnectionPriority();
        return !(mBluetoothAdapter == null || mBluetoothGatt == null) && mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 写数据
     *
     * @param buffer 指令
     * @return boolean
     */
    public void writeCharacteristic(final String uuidStr, final byte[] buffer, int delayMills) {
        writeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                write(uuidStr, buffer);
            }
        }, delayMills);
    }

    /**
     * 采用非响应的方式写数据
     *
     * @param buffer 指令
     * @return boolean
     */
    public void writeCharacteristicWithoutResponse(final String uuidStr, final byte[] buffer, int delayMills) {
        writeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                writeWithoutResponse(uuidStr, buffer);
            }
        }, delayMills);
    }

    /**
     * 写数据
     *
     * @param buffer 指令
     * @return boolean
     */

    private void write(String uuidStr, byte[] buffer) {
        //控制uuid
        //        UUID uuid = UUID.fromString(BluetoothAttributes.UUID_WRITE_CHARACTER);
        UUID uuid = UUID.fromString(uuidStr);
        if (mGattHashMap == null || !mGattHashMap.containsKey(uuid)) {
            return;
        }
        //根据uuid从缓存中拿取蓝牙服务特征
        BluetoothGattCharacteristic mCharacteristic = mGattHashMap.get(uuid);
        if (mCharacteristic == null) {
            return;
        }
        int charaProp = mCharacteristic.getProperties();
        write(buffer, charaProp, mCharacteristic);
    }

    /**
     * 不等待回应发数据
     *
     * @param uuidStr uuid
     * @param buffer  缓存
     */
    private void writeWithoutResponse(String uuidStr, byte[] buffer) {
        //控制uuid
        //        UUID uuid = UUID.fromString(BluetoothAttributes.UUID_CONTROL);
        UUID uuid = UUID.fromString(uuidStr);
        if (mGattHashMap == null || !mGattHashMap.containsKey(uuid)) {
            return;
        }
        //根据uuid从缓存中拿取蓝牙服务特征
        BluetoothGattCharacteristic mCharacteristic = mGattHashMap.get(uuid);
        if (mCharacteristic == null) {
            return;
        }
        writeWithoutResponse(buffer, mCharacteristic);
    }

    /**
     * 向设备写数据,根据Properties性质使用不同的写入方式
     *
     * @param buffer         写入的数据
     * @param charaProp      BluetoothGattCharacteristic属性
     * @param characteristic BluetoothGattCharacteristic对象
     * @return boolean
     */

    private void write(byte[] buffer, int charaProp, BluetoothGattCharacteristic characteristic) {
        // PROPERTY_WRITE 默认类型，需要外围设备的确认,才能继续发送写
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            // 可写，二进制1000
            characteristic.setValue(buffer);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            writeCharecteristic(characteristic);
        }
        // PROPERTY_WRITE_NO_RESPONSE 设置该类型不需要外围设备的回应，可以继续写数据。加快传输速率
        else if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            // 只可写，二进制0100
            characteristic.setValue(buffer);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            writeCharecteristic(characteristic);
        }
    }

    /**
     * 向设备写数据,根据非响应方式
     *
     * @param buffer         写入的数据
     * @param characteristic BluetoothGattCharacteristic对象
     * @return boolean
     */

    private void writeWithoutResponse(byte[] buffer, BluetoothGattCharacteristic characteristic) {

        characteristic.setValue(buffer);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharecteristic(characteristic);

    }

    /**
     * 获取gatt服务列表
     *
     * @return 远程设备提供的gatt服务列表(功能通道)
     */

    public List<BluetoothGattService> getSupportedGattServices() {
        return mBluetoothGatt == null ? null : mBluetoothGatt.getServices();
    }

    /**
     * 获取RSSI值
     *
     * @return null false
     */

    public boolean getRssiValue() {
        return mBluetoothGatt != null && mBluetoothGatt.readRemoteRssi();
    }

    /**
     * 获取蓝牙设备对象
     *
     * @return BluetoothDevice
     */
    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    /**
     * 判断设备是否连接
     *
     * @param address 设备地址
     * @return true 已连接
     */

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Logger.e("蓝牙适配器没有初始化获取mac地址未指明。");
            return false;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Logger.e("蓝牙设备没有发现，无法连接。");
            return false;
        }
        ////每次连接之前先释放掉原来的资源
        if (mBluetoothGatt != null) {
            close();
        }
        Logger.e("蓝牙 -> 发起连接");
        //创建新的连接
//        mBluetoothGatt = device.connectGatt(getContext(), false, mGattCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(getContext(), false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = device.connectGatt(getContext(), false, mGattCallback);
        }
        return true;
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        if (gatt == null) {
            return false;
        }
        try {
            Method refresh = gatt.getClass().getMethod("refresh", new Class[0]);
            if (refresh != null) {
                return (Boolean) refresh.invoke(gatt, new Object[0]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 断开连接
     */

    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 释放资源
     */

    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothDevice = null;
        }
    }

    /**
     * 蓝牙协议回调
     */

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (mStateChangeListener == null) {
                return;
            }
            // 连接状态
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.e("蓝牙 -> 连接成功");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    int mtu = 135;
//                    Logger.i("request " + mtu + " mtu:" + mBluetoothGatt.requestMtu(mtu));
//                }
                actionConnectionPriority = false;
                actionConnectionPriorityValue = 100;
                mStateChangeListener.connected(gatt.getDevice());
                setConnected(true);
                mBluetoothDevice = gatt.getDevice();
                ////先看看有没有缓存,有的话去掉缓存
                if (!gatt.getServices().isEmpty()) {
                    refreshDeviceCache(gatt);
                }

                //设备连接成功，查找服务!
                discoverServices();
            }
            // 断开连接
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //Toast.makeText(getContext(),"蓝牙连接断开",Toast.LENGTH_SHORT).show();
                Logger.e("蓝牙 -> 蓝牙连接已断开");
                EventBusUtils.post(new ConnectFailEvent());
                //断开连接之前,清除缓存
                refreshDeviceCache(gatt);
                close();
                mStateChangeListener.disconnected(gatt.getDevice());
                setConnected(false);
                mBluetoothDevice = null;
            }
        }

        /**
         * 是否发现服务
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mCharacteristicListener != null) {
                    mCharacteristicListener.onServicesDiscovered();
                }
            }
            else {
                Logger.e("蓝牙通信服务回调失败：status=" + status);
            }

        }

        /**
         * 读操作回调
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (mCharacteristicListener != null) {
                mCharacteristicListener.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (mCharacteristicListener != null) {
                mCharacteristicListener.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (mCharacteristicListener != null) {
                mCharacteristicListener.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (mCharacteristicListener != null) {
                mCharacteristicListener.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        /**
         * 信号强度
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (mCharacteristicListener != null) {
                mCharacteristicListener.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu,int status) {
            super.onMtuChanged(gatt, mtu,status);
            Logger.i(String.format("onMtuChanged：mtu = %s, status: %d", mtu - 3,status));
        }
        /**
         * Callback indicating the connection parameters were updated.
         *
         * @param gatt GATT client involved
         * @param interval Connection interval used on this connection, 1.25ms unit. Valid
         *            range is from 6 (7.5ms) to 3200 (4000ms).
         * @param latency Slave latency for the connection in number of connection events. Valid
         *            range is from 0 to 499
         * @param timeout Supervision timeout for this connection, in 10ms unit. Valid range is
         *            from 10 (0.1s) to 3200 (32s)
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the connection has been updated
         *                successfully
         * @hide
         */
        public void onConnectionUpdated(BluetoothGatt gatt, int interval, int latency, int timeout, int status) {
            Logger.i("onConnectionUpdated(interval):" + interval * 1.25 + "ms");
            if (actionConnectionPriority == false) return;
            if (actionConnectionPriorityValue == 100) {
                actionConnectionPriorityValue = interval;
            } else if (interval > actionConnectionPriorityValue) {
                actionConnectionPriority = false;
            } else {
                actionConnectionPriorityValue = interval;
            }
        }
    };


    /**
     * 连接状态接口
     */
    public interface OnConnectionStateChangeListener {
        void connected(BluetoothDevice device);

        void disconnected(BluetoothDevice device);
    }

    /**
     * 发现服务、数据读写操作接口
     */
    public interface OnCharacteristicListener {
        void onServicesDiscovered();

        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

        void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
    }

    private OnConnectionStateChangeListener mStateChangeListener = null;
    private OnCharacteristicListener mCharacteristicListener = null;

    public void setOnConnectionStateChangeListener(OnConnectionStateChangeListener listener) {
        this.mStateChangeListener = listener;
    }

    public void setOnCharacteristicListener(OnCharacteristicListener listener) {
        this.mCharacteristicListener = listener;
    }

}
