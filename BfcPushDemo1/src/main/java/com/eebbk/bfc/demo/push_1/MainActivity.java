package com.eebbk.bfc.demo.push_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.im.push.util.AppUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG=MainActivity.class.getName();

    private TextView result;
    private TextView count;

    private MessageReciever messageReciever;

    public static final String MESSAGE_RECIEVER_ACTION="com.eebbk.bfc.im_message_reciever_action1";

    public class MessageReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MESSAGE_RECIEVER_ACTION)){
                Message msg=new Message();
                msg.what=1;

                String temp=intent.getStringExtra("message");
                msg.obj=temp;
                myHandler.sendMessage(msg);
            }
        }
    }
    private int mCount=0;

    private Handler myHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String temp= (String) msg.obj;
            mCount++;
            result.setText(temp);
            count.setText(String.valueOf(mCount));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        listenDataSource();
//        upline();

//        SyncPushClient.setmOnPushStatusListener(new OnPushStatusListener() {
//            @Override
//            public void onConnectStatus(int connectStatus) {
//                Log.i(TAG, "connectStatus:" + connectStatus);
//            }
//
//            @Override
//            public void onLogin(long registerId) {
//                Log.i(TAG, "onLogin registerId:" + registerId);
//            }
//        });

        messageReciever = new MessageReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_RECIEVER_ACTION);
        registerReceiver(messageReciever, intentFilter);
    }

    private void initView() {
        result= (TextView) findViewById(R.id.result);
        count=(TextView) findViewById(R.id.num);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtil.isAppRunOnBackground(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppUtil.isAppRunOnBackground(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppUtil.isAppRunOnBackground(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(messageReciever!=null){
            unregisterReceiver(messageReciever);
        }
    }

    @Override
    public void onClick(View v) {

    }

//    private void init() {
//        String host = "testgw.im.okii.com";
//        int port = 28000;
////        SyncPushClient.setDebug(true); // 打开调试模式，有日志输出
//        SyncPushClient.init(this, host, port, new OnInitSateListener() {
//            @Override
//            public void onSuccess() {
//                Log.i("SyncTestApplication", "sync push init success!!!");
//            }
//        });
//    }

//    /**
//     * 数据源监听，数据源的信息应该是从业务系统获取下来的
//     */
//    private void listenDataSource() {
//        DataSource dataSource = new DataSource();
////        long dialogId = 0; // 从业务服务器获取
//        long syncKey = 0; // 从业务服务器获取
//        int order = FetchOrder.LAST; // 根据需求设置取消息的模式，顺序，逆序，拉去历史
//        int pageSize = 20; // 一次拉去消息的条数
//        dataSource.addDataSourceConfig(new DataSourceConfig(dialogId, syncKey, order, pageSize));
//        SyncPushClient.listen(dataSource);
//    }

}
