package com.zkxl.locklibrary.bluetoothlib.decoder;

/**
 * 解码接口
 *
 * @author zhoupeng
 * @date:2016-8-16 上午9:28:23
 */
public interface IDecoder {
    /**
     * 解码
     *
     * @param code 返回的数据
     */
    boolean decode(String code);
}
