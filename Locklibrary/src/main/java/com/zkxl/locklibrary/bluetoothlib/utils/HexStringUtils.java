package com.zkxl.locklibrary.bluetoothlib.utils;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据转换工具类
 *
 * @author zhoupeng <a href="http://www.chusemean.com">深圳市创世易明科技有限公司</a>
 *         Created on 2017/5/31.
 */
public class HexStringUtils {
    /**
     * byte——>hexString
     *
     * @param b byte[]
     * @return string
     */
    public static String printHexString(byte[] b) {
        String a = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a = a + hex;
        }
        return a;
    }

    /**
     * HexString——>byte
     *
     * @param hexString string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) hexString.indexOf(c);
    }

    /**
     * 16进制数字字符集
     */
    private static String hexString = "0123456789ABCDEF";

    /**
     * byte——>String
     *
     * @param src byte[]
     * @return string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            //最后一位不加上"-"
            if (i < src.length - 1) {
                stringBuilder.append("-");
            }
        }
        return stringBuilder.toString();
    }

    public static String bytesToHexString(int[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            //最后一位不加上"-"
            if (i < src.length - 1) {
                stringBuilder.append("-");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        //根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        //将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    /**
     * 调换位置，4位16进制转字符串
     *
     * @param str 输入的16进制字符串
     * @return 转换后的字符串
     */
    public static String str2unicode(String str) {
        StringBuilder builder = new StringBuilder();
        int size = str.length() / 4;
        for (int i = 0; i < size; i++) {
            String charStr = str.subSequence(i * 4 + 2, i * 4 + 4).toString() + str.subSequence(i * 4, i * 4 + 2).toString();
            char letter = (char) Integer.parseInt(charStr, 16);
            builder.append(letter);
        }
        return builder.toString();
    }

    /**
     * 十进制转换为十六进制字符串
     *
     * @param algorism int 十进制的数字
     * @return String 对应的十六进制字符串
     */
    public static String algorismToHEXString(long algorism) {
        String result = Long.toHexString(algorism);
        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        //变大写
//        result = result.toUpperCase();
        return result;
    }

    /**
     * 十六进制字符串装十进制
     *
     * @param hex 十六进制字符串
     * @return 十进制数值
     */
    public static long hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        long result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 二进制字符串转十进制
     *
     * @param binary 二进制字符串
     * @return 十进制数值
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十进制转二进制
     *
     * @param length 需要转换的长度
     * @param value  数值
     * @return 二进制字符串
     */
    public static String algorismToBinary(int length, int value) {
        String str = Integer.toBinaryString(value);
        int l = str.length();
        for (int i = 0; i < length - l; i++) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * 十六转二进制
     *
     * @param hex 十六进制字符串
     * @return 二进制字符串
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result += "0000";
                    break;
                case '1':
                    result += "0001";
                    break;
                case '2':
                    result += "0010";
                    break;
                case '3':
                    result += "0011";
                    break;
                case '4':
                    result += "0100";
                    break;
                case '5':
                    result += "0101";
                    break;
                case '6':
                    result += "0110";
                    break;
                case '7':
                    result += "0111";
                    break;
                case '8':
                    result += "1000";
                    break;
                case '9':
                    result += "1001";
                    break;
                case 'A':
                    result += "1010";
                    break;
                case 'B':
                    result += "1011";
                    break;
                case 'C':
                    result += "1100";
                    break;
                case 'D':
                    result += "1101";
                    break;
                case 'E':
                    result += "1110";
                    break;
                case 'F':
                    result += "1111";
                    break;
            }
        }
        return result;
    }

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * unicode 转字符串
     */
    public static String unicode2String(String unicode) {
        StringBuilder string = new StringBuilder();
        int size = unicode.length() / 4;
        for (int i = 0; i < size; i++) {
            String charStr = unicode.subSequence(i * 4, i * 4 + 4).toString();
            char letter = (char) Integer.parseInt(charStr, 16);
            string.append(letter);
        }
        return string.toString();
    }

    /**
     * 把十六进制Unicode编码字符串转换为中文字符串, 将\u848B\u4ECB\u77F3转化成蒋介石，注意格式
     *
     * @param str eg:\u848B\u4ECB\u77F3
     * @return 蒋介石
     */
    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");

        Matcher matcher = pattern.matcher(str);

        char ch;

        while (matcher.find()) {
            System.err.println(matcher.group(1) + "," + matcher.group(2));
            ch = (char) Integer.parseInt(matcher.group(2), 16);

            str = str.replace(matcher.group(1), ch + "");

            System.err.println(str);
        }

        return str;

    }

    public static String gbEncoding(final String gbString) {   //gbString = "测试"
        char[] utfBytes = gbString.toCharArray();   //utfBytes = [测, 试]
        String unicodeBytes = "";
        for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
            String hexB = Integer.toHexString(utfBytes[byteIndex]);   //转换为16进制整型字符串
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        System.out.println("unicodeBytes is: " + unicodeBytes);
        return unicodeBytes;
    }

//    public static void main(String[] args) {
    //05-08-06-00-20-33-5e-74-7e-a7-00-32-73-ed-5f-20-4e-8c-6b-db
//        String str = "6DF157335B9E9A8C5C0F5B665F204E094E095E747EA74E0073ED";
//        System.err.println(unicode2String(str));
//        String str2 = "5b-66-68-21-54-0d-79-f0-77-1f-5b-9e-59-d3-54-0d-00-31-00-31-00-30-00-33";
//        int[] str8 = {32, 51, 94, 116, 126, 167, 0, 50, 115, 237, 95, 32, 78, 140, 107, 219};
//        System.err.println(unicode2String(bytesToHexString(str8).replace("-","")));
//
//        str2 = str2.replace("-", "");
//        System.err.println(unicode2String(str2));

//        byte[] str3  = { 91, 102, 104, 33, 84, 13, 121, -16, 119, 31, 91, -98, 89, -45, 84, 13, 0, 49, 0, 49, 0, 48, 0, 51};
//        System.err.println(unicode2String(bytesToHexString(str3).replace("-","")));


//        int[] allData = new int[20];
//        int[] data = {8, 109, 241, 87, 51, 89, 39, 91, 102, 4, 95, 32, 78, 9, 8, 0, 49};
//        System.arraycopy(data, 0, allData, 0, data.length);
//        System.err.println(Arrays.toString(allData));

//    }

    private static String schoolName;
    private static String realName;
    private static String className;
    private static int mTotalCounts = -1;
    private static int[] mAllData;
    private static int mTempLen = 0;

    public static void handleDatas(int[] data) {

        //05-08-06-00-02-17
        //05-08-06-01-08-6d-f1-57-33-59-27-5b-66-04-5f-20-4e-09-08-00
        //05-08-06-02-31-00-31-00-30-00-33
        //05-08-06-aa

        //[0, 2, 23]
        //[1, 8, 109, 241, 87, 51, 89, 39, 91, 102, 4, 95, 32, 78, 9, 8, 0]
        //[2, 49, 0, 49, 0, 48, 0, 51]
        //[170]
//        Logger.d("获取学生数据 = " + data[0]);
        //首先判断是否为第一包，获取总包数和总包长度
        if (data[0] == 0) {
            mTotalCounts = data[1];
            mAllData = new int[data[2]];
//                Logger.d("获取学生数据 = " + 0000000 + ", mTotalCounts = " + mTotalCounts);
            return;
        }
//            Logger.d("获取学生数据 = " + 111111 + ", mTotalCounts = " + mTotalCounts);
        //如果当前包数少于或等于0则返回
        if (mTotalCounts <= 0) return;
//            Logger.d("获取学生数据 = " + 222222);
        //结束包 如果为最后一包则解析数据
        if (data[0] == 170) {
//                Logger.d("获取学生数据 = " + 4444444);
            //学校名称
            int schoolLen = mAllData[0];
            int[] schoolByte = new int[schoolLen];
            System.arraycopy(mAllData, 1, schoolByte, 0, schoolLen);
            System.out.println(HexStringUtils.bytesToHexString(schoolByte).replace("-",""));
            schoolName = HexStringUtils.unicode2String(HexStringUtils.bytesToHexString(schoolByte).replace("-",""));
            //真实姓名
            int realLen = mAllData[schoolLen + 1];
            int[] realByte = new int[realLen];
            System.arraycopy(mAllData, schoolLen + 2, realByte, 0, realLen);
            realName = HexStringUtils.unicode2String(HexStringUtils.bytesToHexString(realByte).replace("-",""));

            //班级号
            int classLen = mAllData[realLen + schoolLen + 2];
            int[] classByte = new int[classLen];
            System.arraycopy(mAllData, realLen + schoolLen + 3, classByte, 0, classLen);
            className = HexStringUtils.unicode2String(HexStringUtils.bytesToHexString(classByte).replace("-",""));

            //重置数据
            mTotalCounts = -1;
            mTempLen = 0;
            mAllData = null;

            System.err.println(Arrays.toString(mAllData)
                    + ", schoolName = " + schoolName
                    + ", realName = " + realName
                    + ", className = " + className);
            return;
        }
        //如果当前包序号大于总包数则返回
        if (data[0] > mTotalCounts) return;
//            Logger.d("获取学生数据 = " + 3333333);
        System.out.println(Arrays.toString(mAllData));
        System.out.println(mAllData.length);
        System.out.println(Arrays.toString(data));
        System.out.println(data.length);
        System.out.println(mTempLen);
        System.arraycopy(data, 1, mAllData, mTempLen, data.length-1);
//            Logger.d("获取学生数据 = 5555555 " + Arrays.toString(mAllData));
        mTempLen += data.length - 1;
        System.out.println(Arrays.toString(mAllData));
        System.out.println(mTempLen);

//        Logger.d("获取学生数据 = " + Arrays.toString(mAllData)
//                + ", schoolName = " + schoolName
//                + ", realName = " + realName
//                + ", className = " + className);
    }

    public static void main(String[] args) {
        //[0, 2, 23]
        //[1, 8, 109, 241, 87, 51, 89, 39, 91, 102, 4, 95, 32, 78, 9, 8, 0]
        //[2, 49, 0, 49, 0, 48, 0, 51]
        //[170]
        handleDatas(new int[]{0, 2, 23});
        handleDatas(new int[]{1, 8, 109, 241, 87, 51, 89, 39, 91, 102, 4, 95, 32, 78, 9, 8, 0});
        handleDatas(new int[]{2, 49, 0, 49, 0, 48, 0, 51});
        handleDatas(new int[]{170});
        handleDatas(new int[]{3, 49, 0, 49, 0, 48, 0, 51});

        System.err.println(HexStringUtils.unicode2String("6df1573359275b66"));
    }

}
