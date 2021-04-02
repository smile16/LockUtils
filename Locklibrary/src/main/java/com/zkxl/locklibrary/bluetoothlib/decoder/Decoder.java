package com.zkxl.locklibrary.bluetoothlib.decoder;

import android.content.Context;

/**
 * 解码父类
 *
 * @author zhoupeng
 * @date:2016-8-16 上午9:28:38
 */
public class Decoder implements IDecoder {

    public Decoder(Context context) {
    }

    @Override
    public boolean decode(String code) {
        return true;
    }
}
