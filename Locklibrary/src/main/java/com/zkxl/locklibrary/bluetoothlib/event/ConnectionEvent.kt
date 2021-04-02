package com.zkxl.locklibrary.bluetoothlib.event

/**
 * Date: 17/11/10
 * Time: 10:12
 * Description: 蓝牙连接事件
 *
 * @author csym_ios_04.
 */

data class ConnectionEvent(val action: Int) {
    var deviceAddress:String? = null
    var disConnectContext = DIS_CONNECTION_WITH_WORKING

    constructor(action: Int, deviceAddress: String) : this(action){
        this.deviceAddress = deviceAddress
    }
    constructor(action: Int,disconnectContext:Int):this(action){
        this.disConnectContext = disconnectContext
    }

    companion object {
        const val SEARCH_ACTION = 0x00
        const val SEARCH_ACTION_UPGRADE = 0x01
        const val CANCEL_SEARCH_ACTION = 0x02
        const val CONNECTION_ACTION = 0x03
        const val DIS_CONNECTION_ACTION = 0x04

        //正常工作模式下的断开
        const val DIS_CONNECTION_WITH_WORKING = 0x00
        // 进入DFU模式的断开
        const val DIS_CONNECTION_WITH_ENTERING_DFU = 0x01
        //刷完机的断开
        const val DIS_CONNECTION_WITH_COMPLETING_DFU = 0x02
        //解绑断开
        const val DIS_CONNECTION_WITH_UNBOUNDING_DEVICE = 0x03
        //退出登录的断开
        const val DIS_CONNECTION_WITH_LOGOUT = 0x04

    }
}
