package com.zkxl.intelligentlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zkxl.locklibrary.bluetoothlib.event.ConnectSucessEvent;
import com.zkxl.locklibrary.bluetoothlib.event.ConnectionEvent;
import com.zkxl.locklibrary.bluetoothlib.event.LockWriteSucessEvent;
import com.zkxl.locklibrary.bluetoothlib.event.SearchDeviceEvent;
import com.zkxl.locklibrary.bluetoothlib.handler.bean.LockMessageBean;
import com.zkxl.locklibrary.bluetoothlib.interf.BlueToothListener;
import com.zkxl.locklibrary.bluetoothlib.model.ExtendedBluetoothDevice;
import com.zkxl.locklibrary.bluetoothlib.service.ConnectionService;
import com.zkxl.locklibrary.bluetoothlib.updateLock;
import com.zkxl.locklibrary.bluetoothlib.utils.EventBusUtils;
import com.zkxl.locklibrary.bluetoothlib.utils.SharedPrefUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;



public class MainActivity extends AppCompatActivity implements BlueToothListener {
    /**
     * 保存那些处于正常工作模式的设备
     */
    ArrayList<BluetoothDevice> normalDevices = new ArrayList<>();
    private RecyclerView recyclerView;
    private FindBandRecycleViewAdapter mAdapter;
    private EditText stationName;
    private EditText stationNumber;
    private AppCompatSpinner lockFuction;
    private AppCompatSpinner lockSpeed;
    private EditText heartbeatInterval;
    private EditText openLockTime;
    private EditText lockAdress;
    private EditText frequencyPoint;
    private static final String[] fuction = {"修改锁体编号", "修改锁体空口配置"};
    private static final String[] speed = {"2MBPS", "1MBPS"};
    private String lockFuctionS;
    private String lockSpeedS;
    private String lockName;
    private String lockNumber;
    private String heartIntervalS;
    private String openLockTimeS;
    private String lockAdressS;
    private String frequencyPointS;
    private LockMessageBean lockMessageBean;
    private ContentLoadingProgressBar progressCircular;
    private RelativeLayout progress;
    private Timer timer;
    private boolean isSendMessageSucess=false;
    private ProgressBar progressHorizontal;
    private TextView showLockState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initView();
        initData();
        initListener();
        EventBusUtils.register(this);
        initRequestPermission();
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLockStationState("开始搜索蓝牙设备");
                progressHorizontal.setIndeterminate(true);
                progressHorizontal.setVisibility(View.VISIBLE);
                normalDevices.clear();
                if (mAdapter!=null){
                    mAdapter.notifyDataSetChanged();
                }
                EventBusUtils.post(new ConnectionEvent(ConnectionEvent.SEARCH_ACTION));
            }
        });
    }

    private void initListener() {
        lockFuction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Anubis", fuction[position]);
                lockFuctionS=(position+1)+"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lockSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Anubis", speed[position]);
                lockSpeedS=(position)+"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initData() {
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fuction);
        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, speed);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        lockFuction.setAdapter(nameAdapter);
        lockSpeed.setAdapter(speedAdapter);
        lockFuction.setVisibility(View.VISIBLE);
        lockSpeed.setVisibility(View.VISIBLE);
    }

    private void initView() {
        recyclerView = findViewById(R.id.show_lock_recycle);
        stationName = findViewById(R.id.station_name);
        stationNumber = findViewById(R.id.station_number);
        lockFuction = findViewById(R.id.lock_fuction);
        lockSpeed = findViewById(R.id.lock_speed);
        heartbeatInterval = findViewById(R.id.heartbeat_interval);
        openLockTime = findViewById(R.id.open_lock_time);
        lockAdress = findViewById(R.id.lock_adress);
        frequencyPoint = findViewById(R.id.frequency_point);
        progressCircular = findViewById(R.id.progress_circular);
        progress = findViewById(R.id.progress);
        progressHorizontal = findViewById(R.id.progress_horizontal);
        showLockState = findViewById(R.id.show_lock_state);
        progressCircular.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.red), PorterDuff.Mode.MULTIPLY);
//        progressHorizontal.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.red), PorterDuff.Mode.MULTIPLY);
        //基站名称、基站编号、从机工作模式、锁体空口速率、锁体空口心跳间隔、锁体开锁电机工作时间、锁体空口地址、频点
        String userStationName = SharedPrefUtils.INSTANCE.getUserStationName(this);
        String userStationNumber = SharedPrefUtils.INSTANCE.getUserStationNumber(this);
        String userWorkMode = SharedPrefUtils.INSTANCE.getUserWorkMode(this);
        String userWorkSpeed = SharedPrefUtils.INSTANCE.getUserWorkSpeed(this);
        String userWorkHeart = SharedPrefUtils.INSTANCE.getUserWorkHeart(this);
        String userWorkOpenLockTime = SharedPrefUtils.INSTANCE.getUserWorkOpenLockTime(this);
        String userLockAdress = SharedPrefUtils.INSTANCE.getUserLockAdress(this);
        String userFrequencyPoint = SharedPrefUtils.INSTANCE.getUserFrequencyPoint(this);
        setViewText(stationName,userStationName);
        setViewText(stationNumber,userStationNumber);
        setSpinnerView(lockFuction,userWorkMode);
        setSpinnerView(lockSpeed,userWorkSpeed);
        setViewText(heartbeatInterval,userWorkHeart);
        setViewText(openLockTime,userWorkOpenLockTime);
        setViewText(lockAdress,userLockAdress);
        setViewText(frequencyPoint,userFrequencyPoint);
    }


    private void setViewText(EditText view,String s){
        if (!TextUtils.isEmpty(s)){
            view.setText(s);
        }
    }

    private void setSpinnerView(AppCompatSpinner view,String s){
        if (!TextUtils.isEmpty(s)){
            view.setSelection(Integer.parseInt(s));
        }
    }

    private void setLockStationState(String state){
        showLockState.setText("当前设备状态："+state);
    }

    private void initRequestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe(granted -> {
            if (granted) {
//                EventBusUtils.post(new ConnectionEvent(ConnectionEvent.SEARCH_ACTION));
                Intent intent = new Intent(this, ConnectionService.class);
                startService(intent);
            } else {
                Toast.makeText(this, "请开启手机位置权限", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*******事件*****/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchDeviceEvent(SearchDeviceEvent deviceEvent) {
        //如果列表为空,说明是
        TreeSet<ExtendedBluetoothDevice> deviceSet = deviceEvent.getDevices();
        normalDevices.clear();
        for (ExtendedBluetoothDevice device : deviceSet) {
            normalDevices.add(device.device);
            dealDevices(normalDevices);
        }
    }


    /**
     * 对设备处理
     *
     * @param normalDevices 正常工作的设备
     */
    private void dealDevices(ArrayList<BluetoothDevice> normalDevices) {
//        tvSearching.setText("搜索完成，请点击设备完成连接");
        initRecycle();
        onAdapterOnListener();
    }


    private void onAdapterOnListener() {
        mAdapter.setOnItemClick(new FindBandRecycleViewAdapter.OnItemClick() {
            @Override
            public void onItemClick(BluetoothDevice device) {
                progressHorizontal.setVisibility(View.GONE);
                progressHorizontal.setIndeterminate(false);
                lockName = stationName.getText().toString();
                lockNumber = stationNumber.getText().toString();
                heartIntervalS = heartbeatInterval.getText().toString();
                openLockTimeS = openLockTime.getText().toString();
                lockAdressS = lockAdress.getText().toString();
                frequencyPointS = frequencyPoint.getText().toString();
                saveStationSetting();
                if (!TextUtils.isEmpty(lockName)&&!TextUtils.isEmpty(lockNumber)&&!TextUtils.isEmpty(heartIntervalS)
                &&!TextUtils.isEmpty(openLockTimeS)&&!TextUtils.isEmpty(lockAdressS)&&!TextUtils.isEmpty(frequencyPointS)
                        &&!TextUtils.isEmpty(lockFuctionS)&&!TextUtils.isEmpty(lockSpeedS)
                ){
                    progress.setVisibility(View.VISIBLE);
                    lockMessageBean = new LockMessageBean(lockName,lockNumber,(byte) Integer.parseInt(lockFuctionS,16),
                            (byte)Integer.parseInt(lockSpeedS,16),(byte)Integer.parseInt(heartIntervalS,16),(byte)Integer.parseInt(openLockTimeS,16),
                            lockAdressS,frequencyPointS
                            );
                    boolean b = updateLock.getInstance(MainActivity.this).connectLockBlueTooth(device.getAddress(), MainActivity.this, MainActivity.this, lockMessageBean);
                    if (!b){
                        progress.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,"数据不合法，请重新输入基站数据！",Toast.LENGTH_SHORT).show();
                    }else {
                        //计时器开始计时
                        setLockStationState("开始连接蓝牙设备");
                        timer = new Timer();
                        timer.schedule(new RemindTask(),15*1000);
                    }

                }else {
                    Toast.makeText(getBaseContext(), "请先输入完整的基站信息，再进行蓝牙连接！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveStationSetting() {
        if (!TextUtils.isEmpty(lockName)){
            SharedPrefUtils.INSTANCE.saveUserStationName(MainActivity.this,lockName);
        }
        if (!TextUtils.isEmpty(lockNumber)){
            SharedPrefUtils.INSTANCE.saveUserStationNumber(MainActivity.this,lockNumber);
        }
        if (!TextUtils.isEmpty(lockFuctionS)){
            SharedPrefUtils.INSTANCE.saveUserWorkMode(MainActivity.this,lockFuctionS);
        }
        if (!TextUtils.isEmpty(lockSpeedS)){
            SharedPrefUtils.INSTANCE.saveUserWorkSpeed(MainActivity.this,lockSpeedS);
        }
        if (!TextUtils.isEmpty(heartIntervalS)){
            SharedPrefUtils.INSTANCE.saveUserHeart(MainActivity.this,heartIntervalS);
        }
        if (!TextUtils.isEmpty(openLockTimeS)){
            SharedPrefUtils.INSTANCE.saveUserOpenLockTime(MainActivity.this,openLockTimeS);
        }
        if (!TextUtils.isEmpty(lockAdressS)){
            SharedPrefUtils.INSTANCE.saveUserLockAdress(MainActivity.this,lockAdressS);
        }
        if (!TextUtils.isEmpty(frequencyPointS)){
            SharedPrefUtils.INSTANCE.saveUserFrequencyPoint(MainActivity.this,frequencyPointS);
        }
    }

    private void initRecycle() {
        if (recyclerView != null) {
            if (mAdapter == null) {
                mAdapter = new FindBandRecycleViewAdapter(this, normalDevices);
            }
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(mAdapter);
            //注释说当知道Adapter内Item的改变不会影响RecyclerView宽高的时候，可以设置为true让RecyclerView避免重新计算大小。
            recyclerView.setHasFixedSize(true);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }


    /*******事件*****/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWriteResult(LockWriteSucessEvent lockWriteSucessEvent) {
        isSendMessageSucess=true;
        progress.setVisibility(View.GONE);

        if (lockWriteSucessEvent.isWriteSucess()) {
            //写入成功
            setLockStationState("配置写入成功");
            Log.e("Anubis", "配置写入成功");
            Toast.makeText(this,"配置写入成功",Toast.LENGTH_SHORT).show();
            EventBusUtils.post(new ConnectionEvent(ConnectionEvent.DIS_CONNECTION_ACTION));
        } else {
            //写入失败
            Log.e("Anubis", "配置写入失败");
            Toast.makeText(this,"配置写入失败",Toast.LENGTH_SHORT).show();
            setLockStationState("配置写入失败");
            EventBusUtils.post(new ConnectionEvent(ConnectionEvent.DIS_CONNECTION_ACTION));
        }

    }

    /*******事件*****/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBlueToothConnectState(ConnectSucessEvent connectSucessEvent) {
        Log.e("Anubis", "post过来的数据收到了");
        updateLock.getInstance(this).sendLockMessage(lockMessageBean);
    }

    @Override
    public void onBlueToothConnectSucess() {
        Log.e("Anubis", "蓝牙连接成功咯");
        setLockStationState("蓝牙连接成功");
        EventBusUtils.post(new ConnectionEvent(ConnectionEvent.CANCEL_SEARCH_ACTION));
    }

    @Override
    public void onBlueToothConnectFail() {
        //蓝牙连接失败的回调
        progress.setVisibility(View.GONE);
        setLockStationState("蓝牙连接已断开");
        Log.e("Anubis", "蓝牙连接已断开");
    }

    class RemindTask extends TimerTask{

        @Override
        public void run() {
            if (!isSendMessageSucess){
                setLockStationState("蓝牙通信失败");
                EventBusUtils.post(new ConnectionEvent(ConnectionEvent.CANCEL_SEARCH_ACTION));
            }
            timer.cancel();
        }
    }

    private void forStopServices(){
        Intent intent=new Intent(this,ConnectionService.class);
        stopService(intent);
    }

}