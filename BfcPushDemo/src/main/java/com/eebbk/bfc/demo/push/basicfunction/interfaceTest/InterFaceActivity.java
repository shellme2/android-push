package com.eebbk.bfc.demo.push.basicfunction.interfaceTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.util.StringUtils;
import com.eebbk.bfc.demo.push.util.TimeUtils;
import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class InterFaceActivity extends AppCompatActivity {

    private static final int WHAT_SUCCESS=0;
    private static final int WHAT_FAIL=1;
    private static final int WHAT_CLEAR_VIEW=2;

    private static final int ARG1_VERSION=11;
    private static final int ARG1_ALIAS_AND_TAGS=12;
    private static final int ARG1_ALIAS=13;
    private static final int ARG1_TAGS=14;
    private static final int ARG1_TRIGGER=15;
    private static final int ARG1_DEBUG=16;
    private static final int ARG1_DEBUG_VALUE=17;
    private static final int ARG1_STOP=18;
    private static final int ARG1_RESUME=19;
    private static final int ARG1_IS_STOP=20;

    private MyHandler myhandler =new MyHandler(this);

    private Button mVersionBtn;

    private Button mTagsBtn;
    private TextView mTagsTimeTv;
    private TextView mTagsResultTv;
    private EditText mTagsEtTags;

    private Button mTriggerBtn;
    private TextView mTriggerTimeTv;
    private TextView mTriggerResultTv;

    private Button mDebugOpenBtn;
    private Button mDebugCloseBtn;
    private TextView mDebugResultTv;

    private Button mStopBtn;
    private TextView mStopTimeTv;
    private TextView mStopResultTv;

    private Button mResumeBtn;
    private TextView mResumeTimeTv;
    private TextView mResumeResultTv;

    private Button mIsStopBtn;
    private TextView mIsStopTimeTv;
    private TextView mIsStopResultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter_face);
        initView();
        handlerView();
    }

    private void initView(){
        mVersionBtn= (Button) findViewById(R.id.version_btn);

        mTagsBtn= (Button) findViewById(R.id.tags_btn);
        mTagsTimeTv= (TextView) findViewById(R.id.tags_time_tv);
        mTagsResultTv= (TextView) findViewById(R.id.tags_result_tv);
        mTagsEtTags= (EditText) findViewById(R.id.tags_et_tags);

        mTriggerBtn= (Button) findViewById(R.id.trigger_btn);
        mTriggerTimeTv= (TextView) findViewById(R.id.trigger_time_tv);
        mTriggerResultTv= (TextView) findViewById(R.id.trigger_result_tv);

        mDebugOpenBtn = (Button) findViewById(R.id.debug_open_btn);
        mDebugCloseBtn = (Button) findViewById(R.id.debug_close_btn);
        mDebugResultTv= (TextView) findViewById(R.id.debug_result_tv);

        mStopBtn= (Button) findViewById(R.id.stop_btn);
        mStopTimeTv= (TextView) findViewById(R.id.stop_time_tv);
        mStopResultTv= (TextView) findViewById(R.id.stop_result_tv);

        mResumeBtn= (Button) findViewById(R.id.resume_btn);
        mResumeTimeTv= (TextView) findViewById(R.id.resume_time_tv);
        mResumeResultTv= (TextView) findViewById(R.id.resume_result_tv);

        mIsStopBtn= (Button) findViewById(R.id.is_stop_btn);
        mIsStopTimeTv= (TextView) findViewById(R.id.is_stop_time_tv);
        mIsStopResultTv= (TextView) findViewById(R.id.is_stop_result_tv);

    }

//    }

    private void handlerView(){
        handlerVersionView();
//        handlerAliasAndTagsView();
//        handlerAliasView();
        handlerTagsView();
        handlerTriggerView();
        handlerDebugView();
//        handlerDebugValueView();
        handlerStopView();
        handlerResumeView();
        handlerIsStopView();
    }

    private void showTextWhole(String msg){
        AlertDialog dialog= new AlertDialog.Builder(InterFaceActivity.this)
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

    private void handlerVersionView(){
        mVersionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InterFaceActivity.this,VersionActivity.class);
                startActivity(intent);
            }
        });

    }
    private void handlerTagsView(){
        mTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message msg1=new Message();
                msg1.what=WHAT_CLEAR_VIEW;
                msg1.arg1=ARG1_TAGS;
                myhandler.sendMessage(msg1);

                final long startTime= System.currentTimeMillis();
                String tags=mTagsEtTags.getText().toString();
                List<String> tagsTemp=StringUtils.stringToList(tags);

                try{
                    EebbkPush.setTags(tagsTemp, new OnAliasAndTagsListener() {

                        @Override
                        public void onSuccess(String alias, List<String> tags) {
                            long endTime= System.currentTimeMillis();
                            Message msg=new Message();
                            msg.what=WHAT_SUCCESS;
                            msg.arg1=ARG1_TAGS;
                            Bundle bundle=new Bundle();
                            bundle.putLong("time",endTime-startTime);
                            bundle.putString("tags", StringUtils.listToString(tags));
                            msg.setData(bundle);
                            myhandler.sendMessage(msg);
                        }

                        @Override
                        public void onFail(String alias, List<String> tags, String errorMsg, String errorCode) {
                            long endTime= System.currentTimeMillis();
                            Message msg=new Message();
                            msg.what=WHAT_FAIL;
                            msg.arg1=ARG1_TAGS;
                            Bundle bundle=new Bundle();
                            bundle.putLong("time",endTime-startTime);
                            bundle.putString("tags", StringUtils.listToString(tags));
                            bundle.putString("errorMsg",errorCode+"::"+ errorMsg);
                            msg.setData(bundle);
                            myhandler.sendMessage(msg);
                        }
                    });
                }catch (Exception e){
                    long endTime= System.currentTimeMillis();
                    Message msg=new Message();
                    msg.what=WHAT_FAIL;
                    msg.arg1=ARG1_TAGS;
                    Bundle bundle=new Bundle();
                    bundle.putLong("time",endTime-startTime);
                    bundle.putString("tags", tags);
                    bundle.putString("errorMsg",e.getMessage());
                    msg.setData(bundle);
                    myhandler.sendMessage(msg);
                }

            }
        });

        mTagsResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mTagsResultTv.getText().toString());
            }
        });
    }

    private void handlerTriggerView(){
        mTriggerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message msg1=new Message();
                msg1.what=WHAT_CLEAR_VIEW;
                msg1.arg1=ARG1_TRIGGER;
                myhandler.sendMessage(msg1);

                final long startTime= System.currentTimeMillis();
                EebbkPush.sendPushSyncTrigger(new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_TRIGGER;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg, String errorCode) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_TRIGGER;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("errorMsg",errorCode+"::"+errorMsg);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }
                });
            }
        });

        mTriggerResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mTriggerResultTv.getText().toString());
            }
        });
    }

    private void handlerDebugView(){
        mDebugOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EebbkPush.setDebugMode(true);
                Message msg=new Message();
                msg.what=WHAT_SUCCESS;
                msg.arg1=ARG1_DEBUG;
                Bundle bundle=new Bundle();
                bundle.putString("mode","开启调试模式");
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }
        });
        mDebugCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EebbkPush.setDebugMode(false);
                Message msg=new Message();
                msg.what=WHAT_SUCCESS;
                msg.arg1=ARG1_DEBUG;
                Bundle bundle=new Bundle();
                bundle.putString("mode","关闭调试模式");
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }
        });
    }

    private void handlerStopView(){
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message msg1=new Message();
                msg1.what=WHAT_CLEAR_VIEW;
                msg1.arg1=ARG1_STOP;
                myhandler.sendMessage(msg1);

                final long startTime= System.currentTimeMillis();
                EebbkPush.stopPush(new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_STOP;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg,String errorCode) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_STOP;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("errorMsg",errorCode+"::"+errorMsg);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }
                });

            }
        });

        mStopResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mStopResultTv.getText().toString());
            }
        });
    }
    private void handlerResumeView(){
        mResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message msg1=new Message();
                msg1.what=WHAT_CLEAR_VIEW;
                msg1.arg1=ARG1_RESUME;
                myhandler.sendMessage(msg1);

                final long startTime= System.currentTimeMillis();
                EebbkPush.resumePush(new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_RESUME;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg,String errorCode) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_RESUME;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("errorMsg",errorCode+"::"+errorMsg);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }
                });
            }
        });

        mResumeResultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextWhole(mResumeResultTv.getText().toString());
            }
        });
    }
    private void handlerIsStopView(){
        mIsStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime= System.currentTimeMillis();
                boolean isStopPush=EebbkPush.isStopPush();
                Message msg=new Message();
                msg.what=WHAT_SUCCESS;
                msg.arg1=ARG1_IS_STOP;
                Bundle bundle=new Bundle();
                bundle.putLong("time",System.currentTimeMillis()-startTime);
                if(isStopPush){
                    bundle.putString("result","推送关闭");
                }else{
                    bundle.putString("result","推送开启");
                }
                msg.setData(bundle);
                myhandler.sendMessage(msg);
            }
        });
    }


    static class MyHandler extends Handler{
        WeakReference<Activity> mActivityReference;
        MyHandler(Activity act){
            mActivityReference=new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            InterFaceActivity act=(InterFaceActivity)mActivityReference.get();
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

        private void handleSuccess(InterFaceActivity act,Message msg){
            Bundle bundle=msg.getData();
            switch (msg.arg1){

                case ARG1_TAGS:
                    act.mTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    String tags="成功! "+"标签："+bundle.getString("tags");
                    act.mTagsResultTv.setText(tags);
                    break;
                case ARG1_DEBUG:
                    act.mDebugResultTv.setText(bundle.getString("mode"));
                    break;
                case ARG1_STOP:
                    act.mStopTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mStopResultTv.setText("成功!");
                    break;
                case ARG1_RESUME:
                    act.mResumeTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mResumeResultTv.setText("成功!");
                    break;
                case ARG1_IS_STOP:
                    act.mIsStopTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mIsStopResultTv.setText(bundle.getString("result"));
                    break;
                case ARG1_TRIGGER:
                    act.mTriggerTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mTriggerResultTv.setText("成功!");
                    break;
            }
        }

        private void handleFail(InterFaceActivity act,Message msg){
            Bundle bundle=msg.getData();
            String str="失败："+bundle.getString("errorMsg");
            switch (msg.arg1){
                case ARG1_TAGS:
                    act.mTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mTagsResultTv.setText(str);
                    break;
                case ARG1_STOP:
                    act.mStopTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mStopResultTv.setText(str);
                    break;
                case ARG1_RESUME:
                    act.mResumeTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mResumeResultTv.setText(str);
                    break;
                case ARG1_TRIGGER:
                    act.mTriggerTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mTriggerResultTv.setText(str);
                    break;
            }
        }

        private void handleClear(InterFaceActivity act,Message msg) {
            switch (msg.arg1) {
                case ARG1_TAGS:
                    act.mTagsTimeTv.setText("");
                    act.mTagsResultTv.setText("");
                    break;
                case ARG1_DEBUG:
                    act.mDebugResultTv.setText("");
                    break;
                case ARG1_STOP:
                    act.mStopTimeTv.setText("");
                    act.mStopResultTv.setText("");
                    break;
                case ARG1_RESUME:
                    act.mResumeTimeTv.setText("");
                    act.mResumeResultTv.setText("");
                    break;
                case ARG1_IS_STOP:
                    act.mIsStopTimeTv.setText("");
                    act.mIsStopResultTv.setText("");
                    break;
                case ARG1_TRIGGER:
                    act.mTriggerTimeTv.setText("");
                    act.mTriggerResultTv.setText("");
                    break;
            }
        }
    }

}
