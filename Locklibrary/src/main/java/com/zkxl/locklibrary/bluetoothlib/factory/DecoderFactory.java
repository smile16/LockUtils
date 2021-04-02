package com.zkxl.locklibrary.bluetoothlib.factory;

import android.content.Context;
import android.text.TextUtils;

import com.zkxl.locklibrary.bluetoothlib.decoder.IDecoder;

import java.util.HashMap;


/**
 * 解码工厂
 *
 * @author zhoupeng
 * @date:2016-8-16 上午9:38:47
 */
public class DecoderFactory {

    /**
     * 避免重复创建对象，将解码对象缓存起来
     */
    private HashMap<String, IDecoder> cacheHashMap = new HashMap<>();

    private Context context;

    public DecoderFactory(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    /**
     * 根据uuid来获取不同的解码对象
     *
     * @param data 手环返回经过转换的16进制数据
     * @return 解码对象
     */
    public IDecoder getDecoder(String data) {
        //如果返回数据为空，表示失败，直接返回null
        if (data == null || TextUtils.isEmpty(data)) {
            return null;
        }
        String index = data.substring(0, 2);//头标识符
        //缓存解码对象,避免不断创建新的对象
        if (cacheHashMap == null) {
            cacheHashMap = new HashMap<>();
        }
        IDecoder iDecoder = cacheHashMap.get(index);
        if (iDecoder != null) {
            return iDecoder;
        }
        //根据头标识符来区分不同的解码类
        //此处可以根据实际情况来做区分，以下是本人在项目中根据指令数据的头标识符来区分
//        if (FF.equals(index)) {//ack
//            iDecoder = new AckDecoder(getContext());
//        }
        if (iDecoder == null) {
            return null;
        }
        cacheHashMap.put(data, iDecoder);
        return iDecoder;
    }
}
