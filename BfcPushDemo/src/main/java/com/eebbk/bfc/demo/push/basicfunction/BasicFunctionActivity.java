package com.eebbk.bfc.demo.push.basicfunction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.eebbk.bfc.common.app.ToastUtils;
import com.eebbk.bfc.demo.push.PushTestApplication;
import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.basicfunction.customflow.CustomFlowActivity;
import com.eebbk.bfc.demo.push.basicfunction.defaultflow.DefaultFlowActivity;
import com.eebbk.bfc.demo.push.basicfunction.function.FunctionActivity;
import com.eebbk.bfc.demo.push.basicfunction.interfaceTest.InterFaceActivity;
import com.eebbk.bfc.demo.push.util.TimeUtils;
import com.eebbk.bfc.im.push.BfcPush;
import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.lang.ref.WeakReference;

/**
 * 整体流程的设计
 */
public class BasicFunctionActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mDefaultBtn;
    private Button mCustomBtn;
    private Button mInterfaceBtn;
    private Button mFunctionBtn;

    private Button mInitBtn;
    private Switch mSwitchBtn;
    private TextView mInitTimeTv;
    private TextView mInitResultTv;
    private TextView mAnalysisTv;

    private static final int WHAT_SUCCESS=0;
    private static final int WHAT_FAIL=1;
    private static final int WHAT_CLEAR_VIEW=2;

    private static final int ARG1_INIT=10;

    private int urlMode = BfcPush.Settings.URL_MODE_RELEASE;
    private BfcPush bfcPush;

    private MyHandler myhandler =new MyHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_function);
        initView();
        initData();
    }

    private void initView(){
        mInitBtn= (Button) findViewById(R.id.init_btn);
        mSwitchBtn = (Switch) findViewById(R.id.switch_btn);
        mInitTimeTv= (TextView) findViewById(R.id.init_time_tv);
        mInitResultTv= (TextView) findViewById(R.id.init_result_tv);
        mAnalysisTv = (TextView) findViewById(R.id.analyze_tv);

        mDefaultBtn= (Button) findViewById(R.id.default_btn);
        mCustomBtn= (Button) findViewById(R.id.custom_btn);
        mInterfaceBtn= (Button) findViewById(R.id.interface_btn);
        mFunctionBtn= (Button) findViewById(R.id.function_btn);
        findViewById(R.id.analyze_btn).setOnClickListener(this);

        mInitResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mInitResultTv.getText().toString());
            }
        });
        mSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    urlMode = BfcPush.Settings.URL_MODE_TEST;
                }else {
                    urlMode = BfcPush.Settings.URL_MODE_RELEASE;
                }
            }
        });
    }

    private void showTextWhole(String msg){
        AlertDialog dialog= new AlertDialog.Builder(BasicFunctionActivity.this)
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

    private void initData(){
        mDefaultBtn.setOnClickListener(this);
        mCustomBtn.setOnClickListener(this);
        mInterfaceBtn.setOnClickListener(this);
        mFunctionBtn.setOnClickListener(this);
        mInitBtn.setOnClickListener(this);

        if(!PushTestApplication.isInit){
            disableButton();
        }
    }


    private void handlerInitView(){
        Message msg1 = new Message();
        msg1.what = WHAT_CLEAR_VIEW;
        msg1.arg1 = ARG1_INIT;
        myhandler.sendMessage(msg1);

        final long startTime = System.currentTimeMillis();
       /* EebbkPush.setDebugMode(true);
        EebbkPush.setUrlDebugMode(true);
        EebbkPush.init(getApplicationContext(), new OnInitSateListener() {
            @Override
            public void onSuccess() {
                Log.d("BasicFunctionActivity", "onSuccess: handlerInitView");
                long endTime = System.currentTimeMillis();
                Message msg = new Message();
                msg.what = WHAT_SUCCESS;
                msg.arg1 = ARG1_INIT;
                Bundle bundle = new Bundle();
                bundle.putLong("time", endTime - startTime);
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }

            @Override
            public void onFail(String errorMsg, String errorCode) {
                long endTime = System.currentTimeMillis();
                Message msg = new Message();
                msg.what = WHAT_FAIL;
                msg.arg1 = ARG1_INIT;
                Bundle bundle = new Bundle();
                bundle.putLong("time", endTime - startTime);
                bundle.putString("errorMsg", errorCode + "::" + errorMsg);
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }
        });*/


        if (bfcPush == null) {
             bfcPush= new BfcPush.Builder().setDebug(true).setUrlMode(BfcPush.Settings.URL_MODE_RELEASE).build();
        }
        bfcPush.init(getApplicationContext(), new OnInitSateListener() {
            @Override
            public void onSuccess() {
                Log.d("BasicFunctionActivity", "onSuccess: handlerInitView");
                long endTime = System.currentTimeMillis();
                Message msg = new Message();
                msg.what = WHAT_SUCCESS;
                msg.arg1 = ARG1_INIT;
                Bundle bundle = new Bundle();
                bundle.putLong("time", endTime - startTime);
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }

            @Override
            public void onFail(String errorMsg, String errorCode) {
                long endTime = System.currentTimeMillis();
                Message msg = new Message();
                msg.what = WHAT_FAIL;
                msg.arg1 = ARG1_INIT;
                Bundle bundle = new Bundle();
                bundle.putLong("time", endTime - startTime);
                bundle.putString("errorMsg", errorCode + "::" + errorMsg);
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }
        }, new OnPushStatusListener() {
            @Override
            public void onPushStatus(int status, Object... values) {
                LogUtils.i("BasicFunctionActivity", "onPushStatus status:" + status);
                if(status == Status.RECEIVE){
                    // 收到推送消息
                    SyncMessage syncMessage = (SyncMessage)values[0];
                    LogUtils.i("BasicFunctionActivity", "onPushStatus syncMessage:" + syncMessage.toString());
                }
            }
        });

    }


    private void enableButton(){
        mDefaultBtn.setEnabled(true);
        mCustomBtn.setEnabled(true);
        mInterfaceBtn.setEnabled(true);
        mFunctionBtn.setEnabled(true);
    }

    private void disableButton(){
        mDefaultBtn.setEnabled(false);
        mCustomBtn.setEnabled(false);
        mInterfaceBtn.setEnabled(false);
        mFunctionBtn.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.default_btn:
                entryDefaultFlow();
                break;

            case R.id.custom_btn:
                entryCustomFlow();
                break;

            case R.id.interface_btn:
                entryInterface();
                break;

            case R.id.function_btn:
                entryFunction();
                break;

            case R.id.init_btn:
                handlerInitView();
                break;
            case R.id.analyze_btn:
                analyzeBtn();
                break;
            default:
                break;
        }
    }

    private void analyzeBtn() {
        if(bfcPush == null){
            ToastUtils.getInstance(this.getApplicationContext()).l("请先初始化接口,再PUSH分析");
           return;
        }
        AppUtil.isAppRunOnBackground(getApplicationContext());
        String result = bfcPush.analyzePush();
        mAnalysisTv.setText(result);

    }

    private void entryDefaultFlow(){
        Intent intent=new Intent(this,DefaultFlowActivity.class);
        startActivity(intent);
    }

    private void entryCustomFlow(){
        Intent intent=new Intent(this,CustomFlowActivity.class);
        startActivity(intent);
    }

    private void entryInterface(){
        Intent intent=new Intent(this,InterFaceActivity.class);
        startActivity(intent);
    }

    private void entryFunction(){
        Intent intent=new Intent(this,FunctionActivity.class);
        startActivity(intent);
    }


    static class MyHandler extends Handler {
        WeakReference<Activity> mActivityReference;
        MyHandler(Activity act){
            mActivityReference=new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            BasicFunctionActivity act=(BasicFunctionActivity)mActivityReference.get();
            if(act==null){
                return;
            }
            switch (msg.what){
                case WHAT_SUCCESS:
                    handleSuccess(act,msg);
                    break;
                case WHAT_FAIL:
                    handleFail(act,msg);
                    break;
                case WHAT_CLEAR_VIEW:
                    handleClear(act,msg);
                    break;
            }
        }

        private void handleSuccess(BasicFunctionActivity act,Message msg){
            Bundle bundle=msg.getData();
            switch (msg.arg1){
                case ARG1_INIT:
                    act.mInitTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mInitResultTv.setText("成功");
                    break;
            }
            act.enableButton();
            PushTestApplication.isInit=true;
        }

        private void handleFail(BasicFunctionActivity act,Message msg){
            Bundle bundle=msg.getData();
            String str="失败："+bundle.getString("errorMsg");
            switch (msg.arg1){
                case ARG1_INIT:
                    act.mInitTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mInitResultTv.setText(str);
                    break;
            }
            act.disableButton();
        }

        private void handleClear(BasicFunctionActivity act,Message msg) {
            switch (msg.arg1) {
                case ARG1_INIT:
                    act.mInitTimeTv.setText("");
                    act.mInitResultTv.setText("");
                    break;
            }
            act.disableButton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
