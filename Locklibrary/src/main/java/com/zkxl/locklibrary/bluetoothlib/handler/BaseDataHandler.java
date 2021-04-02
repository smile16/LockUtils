package com.zkxl.locklibrary.bluetoothlib.handler;


import com.zkxl.locklibrary.bluetoothlib.handler.bean.BaseData;

/**
 * Date: 17/11/2
 * Time: 10:42
 * Description: 介绍这个类
 *
 * @author csym_ios_04.
 */

public abstract class BaseDataHandler implements HandlerSelector, DataHandler {
    protected String data;

    public BaseDataHandler(String data) {
        this.data = data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public HandlerSelector getDataHandler() {
        String[] results = data.split("-");
        if (results.length >= 3) {
            int b = Integer.parseInt(results[4], 16);
            return getHandlerByFunc(b);
        } else {
            return null;
        }
    }

    protected HandlerSelector getHandlerByFunc(int b) {
        return null;
    }

    @Override
    public BaseData handle() {
        int[] b = preHandle();
        if (b == null) {
            return null;
        } else {
            return resolve(b);
        }
    }

    /**
     * 子类用来进行常见对象用的
     *
     * @param b 解析后的数据
     * @return
     */
    protected abstract BaseData resolve(int[] b);

    private int[] preHandle() {
        String[] results = data.split("-");
        int len = results.length - 2;
        if (len > 0) {
            int[] data = new int[len];
            for (int i = 2; i < results.length; i++) {
                int b = Integer.parseInt(results[i], 16);
                data[i - 2] = b;
            }
            return data;
        }
        return null;
    }
}
