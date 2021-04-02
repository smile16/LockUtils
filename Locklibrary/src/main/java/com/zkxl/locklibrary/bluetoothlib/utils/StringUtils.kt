package com.zkxl.locklibrary.bluetoothlib.utils

import java.lang.StringBuilder
import java.util.*

/**
 * Date: 17/11/21
 * Time: 11:17
 * Description: 介绍这个类
 * @author csym_ios_04.
 */
object StringUtils {
    fun stringToUnicode(str: String, delimiter: String = ","): String {
        val srcBuilder = StringBuilder()
        val resultBuilder = StringBuilder()
        for (c in str) {
            val strs = Integer.toHexString(c.toInt())
            if (strs.length <= 2)
                srcBuilder.append("00")
            srcBuilder.append(strs)
        }
        while (srcBuilder.isNotBlank()) {
            resultBuilder.append(srcBuilder.substring(0, 2))
            resultBuilder.append(delimiter)
            srcBuilder.delete(0, 2)
        }
        resultBuilder.deleteCharAt(resultBuilder.length - 1)
        return resultBuilder.toString()
    }

    fun stringToBytes(strings: Array<String>, len: Int): List<ByteArray> {
        val size1 = strings.size / len
        val size2 = strings.size % len
        val list = arrayListOf<ByteArray>()
        for (i in 0 until size1) {
            val arr = ByteArray(len)
            for (j in i * len until (i + 1) * len) {
                arr[j - i * len] = strings[j].toInt(16).toByte()
            }
            list.add(arr)
        }
        val arr = ByteArray(size2)
        for (i in 0 until size2) {
            arr[i] = strings[i + size1 * len].toInt(16).toByte()
        }
        list.add(arr)
        return list
    }

    private fun getUnicode(src: String): String {
        var str = ""
        (0 until src.length)
                .map { src[it].toInt() }
                .forEach { str += String.format("%04X", Integer.valueOf(it)) }
        return str
    }

    fun getPhoneNumber(phoneNumber: String): ByteArray {
        var i = 0
        var count = 0
        //        val n = getUnicode(name)
        val p = getUnicode(phoneNumber)
        //        val nLength = n.length
        val pLength = p.length
        val bytes = ByteArray((pLength) / 2 + 2)
        //        while (i < nLength) {
        //            bytes[l] = Integer.parseInt(n.substring(i, i + 2), 16).toByte()
        //            l++
        //            i += 2
        //        }
        bytes[count] = (-1).toByte()
        bytes[count + 1] = (-1).toByte()
        while (i < pLength) {
            bytes[count + 2] = Integer.parseInt(p.substring(i, i + 2), 16).toByte()
            count++
            i += 2
        }
        return bytes
    }

    fun getSelfPhoneNumber(phoneNumber: String): ByteArray {
        var i = 0
        var count = 0
        //        val n = getUnicode(name)
        val p = getUnicode(phoneNumber)
        //        val nLength = n.length
        val pLength = p.length
        val bytes = ByteArray((pLength) / 2 )
        //        while (i < nLength) {
        //            bytes[l] = Integer.parseInt(n.substring(i, i + 2), 16).toByte()
        //            l++
        //            i += 2
        //        }
        while (i < pLength) {
            bytes[count] = Integer.parseInt(p.substring(i, i + 2), 16).toByte()
            count++
            i += 2
        }
        return bytes
    }

}