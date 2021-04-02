package com.zkxl.locklibrary.bluetoothlib.utils;


import org.greenrobot.eventbus.EventBus;

/**
 * Date: 17/11/1
 * Time: 15:23
 * Description: 介绍这个类
 *
 * @author csym_ios_04.
 */

public class EventBusUtils {
    public static void register(Object object) {
//        if (EventBus.getDefault() == null) {
////            EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
//            EventBus.builder().installDefaultEventBus();
//        }
        if (!isRegistered(object)) {
            EventBus.getDefault().register(object);
        }
    }

    public static void unregister(Object object) {
        if (isRegistered(object)) {
            EventBus.getDefault().unregister(object);
        }
    }

    public static void post(Object object) {
        EventBus.getDefault().post(object);
    }

    public static void postSticky(Object object) {
        EventBus.getDefault().postSticky(object);
    }

    public static void removeStickyEvent(Object object) {
        EventBus.getDefault().removeStickyEvent(object);
    }

    public static void cancelEventDelivery(Object object) {
        EventBus.getDefault().cancelEventDelivery(object);
    }

    private static boolean isRegistered(Object object) {
        return EventBus.getDefault().isRegistered(object);
    }

}
