package com.eebbk.bfc.demo.push.flow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.db.DbUtils;
import com.eebbk.bfc.im.push.EebbkPush;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DefaultFlowActivity extends Activity {

    private LocalBroadcastManager localBroadcastManager;
    private MyReceiver myReceiver;

    private Switch mPushSw;
    private Button mClearBtn;
    private TextView mCountTv;
    private ListView mMessageLv;

    private MessageAdapter mAdapter;

//    private List<MessageInfo> mData =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defaut_flow);
        registerBroadcast();
        initView();
//        initData();
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
        mPushSw = (Switch) findViewById(R.id.push_sw);
        mClearBtn = (Button) findViewById(R.id.clear_btn);
        mCountTv = (TextView) findViewById(R.id.push_count_tv);
        mMessageLv = (ListView) findViewById(R.id.push_message_lv);
    }

//    private void initData(){
//        mData=DbUtils.getAllMessage();
//    }

    private void handlerView(){
        handerPushSw();
        handerClearBtn();
        handerMessageLv();
        handerCountTv();
    }

    private void handerPushSw(){
        mPushSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mPushSw.setText(R.string.push_start);
                    EebbkPush.stopPush(null);
                }else{
                    mPushSw.setText(R.string.push_stop);
                    EebbkPush.resumePush(null);
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
            String action=intent.getAction();
            if(MessageReceiver.MESSAGE_RECEIVER_ACTION.equals(action)){
                //String msg=intent.getStringExtra(MessageReceiver.EXTRA_MESSAGE_NAME);
                mAdapter.notifyDataSetChanged();
                Message msg=new Message();
                msg.what=1;
                msg.arg1=mAdapter.getCount();
                myHnadler.sendMessage(msg);
            }
        }
    }

    private Handler myHnadler=new MyHandler(this);

    static class MyHandler extends Handler {
        WeakReference<Activity > mActivityReference;

        MyHandler(Activity activity) {
            mActivityReference= new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final DefaultFlowActivity activity = (DefaultFlowActivity)mActivityReference.get();
            if (activity != null) {
                activity.mCountTv.setText(String.valueOf(msg.arg1));
            }
        }
    }
}
