package com.zkxl.locklibrary.bluetoothlib.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.zkxl.locklibrary.bluetoothlib.BluetoothAttributes;
import com.zkxl.locklibrary.bluetoothlib.BluetoothLeService;
import com.zkxl.locklibrary.bluetoothlib.event.ConnectSucessEvent;
import com.zkxl.locklibrary.bluetoothlib.factory.DecoderFactory;
import com.zkxl.locklibrary.bluetoothlib.utils.CRCUtils;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;
import com.orhanobut.logger.Logger;
import com.zkxl.locklibrary.bluetoothlib.utils.HexStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

/**
 * 设备管理类
 * Created by ${zhoupeng} on 2016/8/11.
 */
public class BluzManager implements BluetoothLeService.OnCharacteristicListener {

    private static BluzManager INSTANCE = null;
    private Context mContext;
    private BluetoothLeService mBluetoothLeService;
    private List<BluetoothGattDescriptor> descriptors = new ArrayList<>();

    /**
     * 缓存连接设备的蓝牙特征值和uuid
     */
    private HashMap<UUID, BluetoothGattCharacteristic> mWriteHashMap = new HashMap<>();

    //    private WriteThread mWriteThread;
    private DecoderFactory mDecoderFactory;

    private Context getContext() {
        return mContext;
    }

    public HashMap<UUID, BluetoothGattCharacteristic> getmWriteHashMap() {
        return mWriteHashMap;
    }

    /**
     * 蓝牙信号强度回调接口
     */
    private OnReadRemoteRssiListener readRemoteRssiListener;

    /**
     * 蓝牙服务回调
     */
    private OnServiceListener mServiceListener;

    /**
     * 蓝牙写入回调接口
     */
    private OnWriteCharacteristicListener writeCharacteristicListener;

    /**
     * 蓝牙描述符回调接口
     */

    private OnDescriptorWriteListener descriptorWriteListener;
    /**
     * 蓝牙通知回调接口
     */
    private OnNotifyChangedListener notifyChangedListener;

    /**
     * 读取RSSI信息接口
     */
    public interface OnReadRemoteRssiListener {
        void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
    }

    public interface OnWriteCharacteristicListener {
        void onWriteCharacter(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    }

    public interface OnDescriptorWriteListener {
        void onWriteDescriptor(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
    }

    public interface OnNotifyChangedListener {
        void onNotifyChanged(String data);
    }

    public interface OnServiceListener {
        void onServicesDiscovered(String macAddress);

        void onFirmwareUpgrade(String macAddress);
    }

    public void setOnReadRemoteRssiListener(OnReadRemoteRssiListener listener) {
        this.readRemoteRssiListener = listener;
    }

    public void setOnWriteCharacteristicListener(OnWriteCharacteristicListener listener) {
        this.writeCharacteristicListener = listener;
    }

    public void setDescriptorWriteListener(OnDescriptorWriteListener descriptorWriteListener) {
        this.descriptorWriteListener = descriptorWriteListener;
    }

    public void setOnNotifyChangedListener(OnNotifyChangedListener notifyChangedListener) {
        this.notifyChangedListener = notifyChangedListener;
    }

    public void setOnServiceListener(OnServiceListener l) {
        this.mServiceListener = l;
    }

    private BluzManager(Context context) {
        mContext = context;
        initialize();
    }

    /**
     * 单列模式，确保唯一性
     *
     * @param context 上下文
     * @return BluzManager蓝牙管理类
     */
    public static BluzManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BluzManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluzManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化相关数据
     */
    private void initialize() {
        //初始化蓝牙服务类
        mBluetoothLeService = BluetoothLeService.getInstance(getContext());
        mBluetoothLeService.setOnCharacteristicListener(this);
        //初始化解析工厂
        if (mDecoderFactory == null) {
            mDecoderFactory = new DecoderFactory(getContext());
        }
    }

    /**
     * 获取蓝牙信号强度,只能单次获取
     *
     * @return boolean
     */
    public boolean getRssiValue() {
        return mBluetoothLeService.getRssiValue();
    }

    /**
     * 清楚所有缓存数据
     */
    private void clearHashMap() {
        mWriteHashMap.clear();
    }

    /**
     * 蓝牙服务发现回调
     */

    @Override
    public void onServicesDiscovered() {
        Logger.d("蓝牙服务回调 uuid=");
        clearHashMap();
        descriptors.clear();
        List<BluetoothGattService> list = mBluetoothLeService.getSupportedGattServices();
        for (BluetoothGattService bluetoothGattService : list) {
             UUID uuid = bluetoothGattService.getUuid();
            Logger.d("蓝牙服务回调 uuid=" + uuid.toString());
            //根据底层提供的可用的特征服务uuid过滤出可用的服务以及特征值
            if (BluetoothAttributes.UUID_UART_PROFILE.equalsIgnoreCase(uuid.toString())) {
                List<BluetoothGattCharacteristic> characteristicList = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicList) {
                    //根据服务特征中的属性区分是可读、可写、通知。
                    int properties = characteristic.getProperties();
                    //拥有写权限的uuid放入集合中缓存起来，在需要使用的时候拿取出来。
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                        mWriteHashMap.put(characteristic.getUuid(), characteristic);
                    }
                    //打开通知权限，以下BluetoothAttributes.UUID_RESPONSE_2902为举例说明，具体根据底层给过来的文档去修改
                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        if (mBluetoothLeService.setCharacteristicNotification(characteristic, true)) {
                            BluetoothGattDescriptor descriptor = null;
                            if (BluetoothAttributes.UUID_READ_PROFILE.equalsIgnoreCase(characteristic.getUuid().toString())) {
                                descriptor = characteristic.getDescriptor(UUID.fromString(BluetoothAttributes.UUID_CHARACTERISTICS_SERVICE));
                            }
                            if (descriptor != null) {
                                descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                                //mBluetoothLeService.writeDescriptor(descriptor);
                                descriptors.add(descriptor);
                            }
                        }
                    }
                }
            }
        }


        mBluetoothLeService.initData(mWriteHashMap);
        // 去代开通知端口
        if (!descriptors.isEmpty()) {
            mBluetoothLeService.writeDescriptor(descriptors.get(0));
        }
        //说明是没有这个服务了,所以此时应该是升级模式
        else {
            if (mServiceListener != null) {
                mServiceListener.onFirmwareUpgrade(mBluetoothLeService.getDevice().getAddress());
            }
        }
        ////如果缓存特征服务为空，表示服务回调失败了，可以尝试断开连接或者关闭系统蓝牙重新去连接。
        //if (mWriteHashMap.size() == 0) {
        //    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //    if (bluetoothAdapter != null) {
        //        bluetoothAdapter.disable();
        //    }
        //    return;描述符写入回调
        //}

        Logger.e("蓝牙服务回调：mWriteHashMap=" + mWriteHashMap);

        //请求更新连接参数
//        mBluetoothLeService.requestConnectionPriority();
        Log.e("Anubis","蓝牙打开所有通知服务成功");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EventBusUtils.post(new ConnectSucessEvent());
    }


    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Logger.e("蓝牙数据读取回调 status=" + status + "\tcharacteristic:" + characteristic.getUuid().toString());
    }

    //蓝牙接连后 返回的数据

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //当前连接的蓝牙对象
        BluetoothDevice device = mBluetoothLeService.getDevice();
        if (characteristic == null || characteristic.getValue() == null || device == null) {
            return;
        }
        //将byte类型数据转换为16进制
        String data = HexStringUtils.bytesToHexString(characteristic.getValue());
        Logger.e("蓝牙数据通知回调 uuid==" + characteristic.getUuid().toString() + ",data=" + data);
        if (notifyChangedListener != null) {
            //回调给界面做展示
            notifyChangedListener.onNotifyChanged(data);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        Logger.e("蓝牙数据写入回调 status=" + status + "\tcharacteristic:" + characteristic.getUuid().toString());
        if (writeCharacteristicListener != null) {
            writeCharacteristicListener.onWriteCharacter(gatt, characteristic, status);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Logger.e("描述符写入回调 status=" + status + "\tcharacteristic:" + descriptor.getUuid().toString());
        //写入成功之后就删掉
        descriptors.remove(descriptor);
        //   写入剩下的
        if (!descriptors.isEmpty()) {
            mBluetoothLeService.writeDescriptorWorkaround(descriptors.get(0));
        }
        //写完了
        else {
            if (mServiceListener != null) {
                mServiceListener.onServicesDiscovered(gatt.getDevice().getAddress());
            }
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (readRemoteRssiListener != null) {
            readRemoteRssiListener.onReadRemoteRssi(gatt, rssi, status);
        }
    }


    /**
     * 添加校验码
     *
     * @param content byte[]
     * @return 具备校验码的byte[]
     */
    private byte[] getCrc16(byte[] content) {
        /* crc校验码 */
        int crc = CRCUtils.crc16_ccitt(content, content.length);
        byte crcL = (byte) (crc & 0xff);
        byte crcH = (byte) ((crc >> 8) & 0xff);
        byte[] bytes = new byte[content.length + 2];
        System.arraycopy(content, 0, bytes, 0, content.length);
        bytes[content.length] = crcL;
        bytes[content.length + 1] = crcH;
        return bytes;
    }

    /**
     * 写数据
     *
     * @param buffer 指令
     * @return boolean
     */
    public void writeCharacteristic(String uuidStr, final byte[] buffer, int delayMills) {
        mBluetoothLeService.writeCharacteristicWithoutResponse(uuidStr, buffer, delayMills);
    }

    public void writeCharacteristic(String uuidStr, final byte[] buffer) {
        mBluetoothLeService.writeCharacteristicWithoutResponse(uuidStr, buffer, 0);
    }

    /**
     * 写数据
     *
     * @param buffer 指令
     * @return boolean
     */
    public void writeCharacteristicWithoutResponse(String uuidStr, final byte[] buffer, int delayMills) {
        mBluetoothLeService.writeCharacteristicWithoutResponse(uuidStr, buffer, delayMills);
    }

    public void writeCharacteristicWithoutResponse(String uuidStr, final byte[] buffer) {
        mBluetoothLeService.writeCharacteristicWithoutResponse(uuidStr, buffer, 0);
    }


}
