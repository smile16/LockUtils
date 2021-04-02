package com.zkxl.locklibrary.bluetoothlib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static String heart="^[0-9]{1,2}$";
    private static String adress="^[a-fA-F0-9]+$";;
    private static String name="^[a-zA-Z0-9]+$";;

    public static boolean HeartIsLegal(String s){
        Pattern pattern=Pattern.compile(heart);
        Matcher matcher = pattern.matcher(s);
        boolean matches = matcher.matches();
        return matches;
    }
    public static boolean adressIsLegal(String s){
        Pattern pattern=Pattern.compile(adress);
        Matcher matcher = pattern.matcher(s);
        boolean matches = matcher.matches();
        return matches;
    }
    public static boolean nameIsLetter(String s){
        Pattern pattern=Pattern.compile(name);
        Matcher matcher = pattern.matcher(s);
        boolean matches = matcher.matches();
        return matches;
    }

    /**
      * 判断是否为汉字
     */

    public static boolean isChinese(String string) {
        int n = 0;
            n = (int) string.charAt(0);
            if (!(19968 <= n && n < 40869)) {
                return false;
            }
        return true;
    }

}
