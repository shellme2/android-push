package com.eebbk.bfc.demo.push.basicfunction.defaultflow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.basicfunction.MessageAdapter;
import com.eebbk.bfc.demo.push.basicfunction.MessageReceiver;
import com.eebbk.bfc.demo.push.db.DbUtils;
import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.listener.OnResultListener;

import java.lang.ref.WeakReference;

public class DefaultFlowActivity extends AppCompatActivity {

    private LocalBroadcastManager localBroadcastManager;
    private MyReceiver myReceiver;

    private Button mPushBtn;
    private TextView mPushResultTv;

    private Button mClearBtn;
    private TextView mCountTv;
    private ListView mMessageLv;

    private MessageAdapter mAdapter;

    private Handler mHandler =new MyHandler(this);

    private static final int WHAT_OPEN_PUSH_FAIL=1;
    private static final int WHAT_OPEN_PUSH_SUCCESS =2;
    private static final int WHAT_CLOSE_PUSH_FAIL=3;
    private static final int WHAT_CLOSE_PUSH_SUCCESS =4;
    private static final int WHAT_DATA_CHANGE=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defaut_flow);
        registerBroadcast();
        initView();
        handlerView();
    }

    @Override
    protected void onDestroy() {
        if(myReceiver!=null&&localBroadcastManager!=null){
            localBroadcastManager.unregisterReceiver(myReceiver);
        }
        super.onDestroy();
    }

    private void registerBroadcast(){
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        myReceiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(MessageReceiver.MESSAGE_RECEIVER_ACTION);
        localBroadcastManager.registerReceiver(myReceiver,filter);
    }

    private void initView(){
        mPushBtn = (Button) findViewById(R.id.push_btn);
        mClearBtn = (Button) findViewById(R.id.clear_btn);
        mCountTv = (TextView) findViewById(R.id.push_count_tv);
        mMessageLv = (ListView) findViewById(R.id.push_message_lv);
        mPushResultTv= (TextView) findViewById(R.id.push_result_tv);
    }

    private void handlerView(){
        handerPushSw();
        handerClearBtn();
        handerMessageLv();
        handerCountTv();
    }

    private void handerPushSw(){

        boolean mIsStopPush = EebbkPush.isStopPush();
        if(mIsStopPush){
            mPushResultTv.setText(R.string.push_stop);
        }else {
            mPushResultTv.setText(R.string.push_start);
        }

        mPushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EebbkPush.isStopPush()){
                    EebbkPush.resumePush(new OnResultListener() {
                        @Override
                        public void onSuccess() {
                            mHandler.sendEmptyMessage(WHAT_OPEN_PUSH_SUCCESS);
                        }

                        @Override
                        public void onFail(String errorMsg,String errorCode) {
                            mHandler.sendEmptyMessage(WHAT_CLOSE_PUSH_FAIL);
                        }
                    });
                }else{
                    EebbkPush.stopPush(new OnResultListener()  {
                        @Override
                        public void onSuccess() {
                            mHandler.sendEmptyMessage(WHAT_CLOSE_PUSH_SUCCESS);
                        }

                        @Override
                        public void onFail(String errorMsg,String errorCode) {
                            mHandler.sendEmptyMessage(WHAT_OPEN_PUSH_FAIL);
                        }
                    });
                }
            }
        });

    }

    private void handerClearBtn(){
        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbUtils.deletAllMessage();
                mAdapter.notifyDataSetChanged();
                Message msg=new Message();
                msg.what=WHAT_DATA_CHANGE;
                msg.arg1=0;
                mHandler.sendMessage(msg);

            }
        });
    }

    private void handerCountTv(){
        mCountTv.setText(String.valueOf(mAdapter.getCount()));
    }

    private void handerMessageLv(){
        mAdapter=new MessageAdapter(this);
        mMessageLv.setAdapter(mAdapter);
        mCountTv.setText("0");
    }

    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action=intent.getAction();
            if(MessageReceiver.MESSAGE_RECEIVER_ACTION.equals(action)){
                mAdapter.notifyDataSetChanged();
                Message msg=new Message();
                msg.what=WHAT_DATA_CHANGE;
                msg.arg1=mAdapter.getCount();
                mHandler.sendMessage(msg);
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<Activity > mActivityReference;

        MyHandler(Activity activity) {
            mActivityReference= new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final DefaultFlowActivity act = (DefaultFlowActivity)mActivityReference.get();
            if (act == null) {
                return;
            }
            switch (msg.what){
                case WHAT_OPEN_PUSH_SUCCESS:
                    act.mPushResultTv.setText("打开推送成功！");
                    break;

                case WHAT_OPEN_PUSH_FAIL:
                    act.mPushResultTv.setText("打开推送失败，请重试！");
                    break;

                case WHAT_CLOSE_PUSH_SUCCESS:
                    act.mPushResultTv.setText("关闭推送成功！");
                    break;

                case WHAT_CLOSE_PUSH_FAIL:
                    act.mPushResultTv.setText("关闭推送失败，请重试！");
                    break;

                case WHAT_DATA_CHANGE:
                    act.mCountTv.setText(String.valueOf(msg.arg1));
                    break;
                default:
                    break;
            }
        }
    }
}
