package com.eebbk.bfc.demo.push.basicfunction.customflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.basicfunction.MessageAdapter;
import com.eebbk.bfc.demo.push.basicfunction.MessageReceiver;
import com.eebbk.bfc.demo.push.db.DbUtils;
import com.eebbk.bfc.demo.push.util.StringUtils;
import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class CustomFlowActivity extends AppCompatActivity {
    private static final String TAG = "CustomFlowActivity";

    private Button mPushBtn;
    private Button mClearBtn;
    private TextView mPushResultTv;

    private EditText mTagsEt;
    private Button mAliasAndTagsBtn;
    private TextView mAliasAndTagsResultTv;

    private Button mDebugOpenBtn;
    private Button mDebugCloseBtn;

    private TextView mCountTv;
    private ListView mMessageLv;

    private MessageAdapter mAdapter;

    private LocalBroadcastManager localBroadcastManager;
    private CustomFlowActivity.MyReceiver myReceiver;

    private MyHandler mHandler=new MyHandler(this);
    private static final int WHAT_OPEN_PUSH_FAIL=1;
    private static final int WHAT_OPEN_PUSH_SUCCESS =2;
    private static final int WHAT_CLOSE_PUSH_FAIL=3;
    private static final int WHAT_CLOSE_PUSH_SUCCESS=4;
    private static final int WHAT_DATA_CHANGE=5;
    private static final int WHAT_ALIAS_TAGS_SET_SUCCESS=6;
    private static final int WHAT_ALIAS_TAGS_SET_FAIL=7;
    private static final int WHAT_ALIAS_TAGS_ILLEGAL_FAIL =8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_flow);
        registerBroadcast();

        initView();
        initViewData();
    }

    private void registerBroadcast(){
        localBroadcastManager= LocalBroadcastManager.getInstance(this);
        myReceiver=new CustomFlowActivity.MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(MessageReceiver.MESSAGE_RECEIVER_ACTION);
        localBroadcastManager.registerReceiver(myReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        if(myReceiver!=null&&localBroadcastManager!=null){
            localBroadcastManager.unregisterReceiver(myReceiver);
        }
        super.onDestroy();
    }

    private void initView(){
        mPushBtn = (Button) findViewById(R.id.push_btn);
        mClearBtn= (Button) findViewById(R.id.clear_btn);
        mPushResultTv= (TextView) findViewById(R.id.push_result_tv);

        mTagsEt= (EditText) findViewById(R.id.tags_et);
        mAliasAndTagsBtn= (Button) findViewById(R.id.alias_and_tags_btn);
        mAliasAndTagsResultTv= (TextView) findViewById(R.id.alias_and_tags_result_tv);

        mDebugCloseBtn= (Button) findViewById(R.id.debug_mode_close_btn);
        mDebugOpenBtn=(Button) findViewById(R.id.debug_mode_open_btn);

        mCountTv= (TextView) findViewById(R.id.push_count_tv);
        mMessageLv= (ListView) findViewById(R.id.push_message_lv);

    }

    private void initViewData(){
        initPushSw();
        initAliasAndTags();
        initDebug();
        initListView();
    }

    private void initPushSw(){
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
                    EebbkPush.resumePush(new OnResultListener(){
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
                    EebbkPush.stopPush(new OnResultListener() {
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

        mPushResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mPushResultTv.getText().toString());
            }
        });

    }

    private void initAliasAndTags(){
        mAliasAndTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tags=mTagsEt.getText().toString();
                List<String> tagsList= StringUtils.stringToList(tags);

                LogUtils.d(TAG,"tags--->"+tags);

                try{
                    EebbkPush.setTags(tagsList, new OnAliasAndTagsListener() {

                        @Override
                        public void onSuccess(String alias, List<String> tags) {
                            mHandler.sendEmptyMessage(WHAT_ALIAS_TAGS_SET_SUCCESS);
                        }

                        @Override
                        public void onFail(String alias, List<String> tags, String errorMsg, String errorCode) {
                            mHandler.sendEmptyMessage(WHAT_ALIAS_TAGS_SET_FAIL);
                        }
                    });
                }catch (Exception e){
                    Message msg=new Message();
                    msg.what= WHAT_ALIAS_TAGS_ILLEGAL_FAIL;
                    Bundle bundle=new Bundle();
                    bundle.putString("errorMsg",e.getMessage());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }

            }
        });

        mAliasAndTagsResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mAliasAndTagsResultTv.getText().toString());
            }
        });
    }

    private void initDebug(){
        mDebugOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EebbkPush.setDebugMode(true);
            }
        });
        mDebugCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EebbkPush.setDebugMode(false);
            }
        });

    }

    private void initListView(){
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
        mAdapter=new MessageAdapter(this);
        mMessageLv.setAdapter(mAdapter);
        mCountTv.setText(String.valueOf(mAdapter.getCount()));
    }

    private void showTextWhole(String msg){
        AlertDialog dialog= new AlertDialog.Builder(CustomFlowActivity.this)
                .setTitle("结果信息")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                })
                .create();
        dialog.show();
    }

    static class MyHandler extends Handler {
        WeakReference<Activity> mActivityReference;

        MyHandler(Activity act) {
            mActivityReference = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomFlowActivity act = (CustomFlowActivity) mActivityReference.get();
            if(act==null){
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

                case WHAT_ALIAS_TAGS_SET_SUCCESS:
                    act.mAliasAndTagsResultTv.setText("成功！");
                    act.mPushResultTv.setText("打开推送成功！");
                    break;

                case WHAT_ALIAS_TAGS_SET_FAIL:
                    act.mAliasAndTagsResultTv.setText("失败！");
                    break;

                case WHAT_ALIAS_TAGS_ILLEGAL_FAIL:
                    Bundle bundle=msg.getData();
                    String errorMsg="失败:"+bundle.getString("errorMsg");
                    act.mAliasAndTagsResultTv.setText(errorMsg);
                    break;
                default:
                    break;
            }
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LogUtils.e(TAG, "intent is null , then do nothing !!!");
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

}
