package com.zkxl.locklibrary.bluetoothlib.service;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.zkxl.locklibrary.bluetoothlib.event.SendCommandEvent;
import com.zkxl.locklibrary.bluetoothlib.handler.bean.LockMessageBean;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;
import com.zkxl.locklibrary.bluetoothlib.utils.StringUtils;
import com.zkxl.locklibrary.bluetoothlib.utils.Utils;

/**
 * Date: 17/11/1
 * Time: 10:58
 * Description: 介绍这个类
 *
 * @author csym_ios_04.
 */

public class BluetoothCommands {
    public static final byte PRODUCT_ORDER_NUMBER_T = (byte) 0xA5;
    public static final byte PRODUCT_ORDER_WRITE_SUCESS = (byte) 0x79;
    public static final byte CMD = (byte) 0xB3;
    public static final byte lEN = (byte) 0X2E;
    private static String gbk;


    public static void writeLockSetting(LockMessageBean lockMessageBean) {
        byte[] sendMessage = new byte[50];
        byte[] XorMessage = new byte[48];
        byte[] name = new byte[20];
        byte[] apid = new byte[16];
        byte[] addr = new byte[4];
        byte[] ch = new byte[2];
        byte[] allSetting = new byte[46];
        String stationName = lockMessageBean.getNAME();
        //添加数据的索引
        int count = 0;
        for (int i = 0; i < stationName.length(); i++) {
            boolean b = Utils.isChinese(stationName.substring(i, i + 1));
            if (b) {
                try {
                    String gbk = URLEncoder.encode(stationName.substring(i, i + 1), "GBK");
                    String[] split = gbk.substring(1, gbk.length()).split("%");
                    List<byte[]> bytes = StringUtils.INSTANCE.stringToBytes(split, 16);
                    for (int k = 0; k < bytes.size(); k++) {
                        byte[] bytes1 = bytes.get(k);
                        for (int j = 0; j < bytes1.length; j++) {
                            name[count] = bytes1[j];
                            count++;
                        }
                    }
                    Log.e("Anubis", gbk);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                byte[] bytes = stationName.substring(i, i + 1).getBytes();
                System.arraycopy(bytes, 0, name, count, bytes.length);
                count += bytes.length;
            }
        }
//        try {
//            gbk = URLEncoder.encode(stationName, "GBK");
//            Log.e("Anubis", gbk);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
////        String unicode = StringUtils.INSTANCE.stringToUnicode(stationName, ",");
//        String[] split = gbk.substring(1,gbk.length()).split("%");
//        List<byte[]> bytes = StringUtils.INSTANCE.stringToBytes(split, 16);
//        for (int i = 0; i < bytes.size(); i++) {
//            byte[] bytes1 = bytes.get(i);
//            for (int j = 0; j < bytes1.length; j++) {
//                name[i + j] = bytes1[j];
//            }
//        }


//        byte[] stationNameBytes = stationName.getBytes();
//        System.arraycopy(stationNameBytes,0,name,0,stationNameBytes.length);

        String stationNumber = lockMessageBean.getAPID();
        byte[] stationNumberBytes = stationNumber.getBytes();
        System.arraycopy(stationNumberBytes, 0, apid, 0, stationNumberBytes.length);

        String beanADDR = lockMessageBean.getADDR();
        String s = addZero(beanADDR, 8);
        addr[0] = intToByte(s.substring(0, 2));
        addr[1] = intToByte(s.substring(2, 4));
        addr[2] = intToByte(s.substring(4, 6));
        addr[3] = intToByte(s.substring(6, 8));
//        byte[] beanADDRBytes = beanADDR.getBytes();
//        System.arraycopy(beanADDRBytes,0,addr,0,beanADDRBytes.length);

        String messageBeanCH = lockMessageBean.getCH();
        String sCH = addZero(messageBeanCH, 4);
        ch[0] = intToByte(sCH.substring(0, 2));
        ch[1] = intToByte(sCH.substring(2, 4));
//        byte[] chBytes = messageBeanCH.getBytes();
//        System.arraycopy(chBytes,0,ch,0,chBytes.length);

        System.arraycopy(name, 0, allSetting, 0, name.length);
        System.arraycopy(apid, 0, allSetting, name.length, apid.length);
        allSetting[36] = lockMessageBean.getFUN();
        allSetting[37] = lockMessageBean.getBR();
        allSetting[38] = lockMessageBean.getHBT();
        allSetting[39] = lockMessageBean.getMWT();
        System.arraycopy(addr, 0, allSetting, 40, addr.length);
        System.arraycopy(ch, 0, allSetting, 44, ch.length);

        XorMessage[0] = (byte) 0xB3;
        XorMessage[1] = (byte) 0x2E;
        System.arraycopy(allSetting, 0, XorMessage, 2, allSetting.length);
        sendMessage[0] = (byte) 0xA5;
        byte tempXor = 0;
        for (int i = 0; i < XorMessage.length; i++) {
            tempXor = (byte) (tempXor ^ XorMessage[i]);
        }
        sendMessage[1] = tempXor;
        sendMessage[2] = (byte) 0xB3;
        sendMessage[3] = (byte) 0x2E;
        System.arraycopy(allSetting, 0, sendMessage, 4, allSetting.length);
        for (int i = 0; i < 3; i++) {
            byte[] message = new byte[20];
            System.arraycopy(sendMessage, 20 * i, message, 0, sendMessage.length - 20 * i > 20 ? 20 : sendMessage.length - 20 * i);
            EventBusUtils.post(new SendCommandEvent(message));
        }

    }


    public static void writeLockSetting(byte[] send) {
        EventBusUtils.post(new SendCommandEvent(send));
    }

    public static byte intToByte(String s) {
        for (int i = 0; i < 256; i++) {
            String data;
            String s1 = Integer.toHexString(i);
            Log.e("yanchuang",s1);
            if (s1.length()==1){
                StringBuffer buffer=new StringBuffer();
                buffer.append("0");
                buffer.append(s1);
                data=buffer.toString();
            }else {
                data=s1;
            }
            if (s.equals(data)) {
                return (byte) i;
            }
        }
        return 0x00;
    }


    public static String addZero(String s, int length) {
        if (s.length() < length) {
            StringBuffer buffer = new StringBuffer();
            if (s.length() % 2 == 0) {
                buffer.append(s);
                int len = length - s.length();
                for (int i = 0; i < len; i++) {
                    buffer.append("0");
                }
                return buffer.toString();
            } else {
                String substring = s.substring(0, s.length() - 1);
                String substring2 = s.substring(s.length() - 1, s.length());
                StringBuffer append = buffer.append(substring).append("0").append(substring2);
                int len = length - append.length();
                for (int i = 0; i < len; i++) {
                    buffer.append("0");
                }
                return buffer.toString();
            }
        }
        return s;
    }

}
