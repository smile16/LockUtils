package com.zkxl.locklibrary.bluetoothlib.handler.bean;

import java.util.Arrays;

/**
 * Date: 17/11/2
 * Time: 10:20
 * Description: 介绍这个类
 *
 * @author csym_ios_04.
 */

public abstract  class BaseData {
    protected int[] data;

    public BaseData(int[] data) {
        this.data = data;
        handleData();
    }

    /**
     * 处理时间
     */
    protected abstract void handleData();


    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
        handleData();
    }

    @Override
    public String toString() {
        return "BaseData{" + "data=" + Arrays.toString(data) + '}';
    }
}
