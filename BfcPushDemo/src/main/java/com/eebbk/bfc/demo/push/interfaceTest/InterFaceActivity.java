package com.eebbk.bfc.demo.push.interfaceTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.util.StringUtils;
import com.eebbk.bfc.demo.push.util.TimeUtils;
import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.listener.OnStopResumeListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

import java.lang.ref.WeakReference;
import java.util.List;

public class InterFaceActivity extends Activity {

    private static final String TAG=InterFaceActivity.class.getName();

    private static final int WHAT_SUCCESS=0;
    private static final int WHAT_FAIL=1;

    private static final int ARG1_INIT=10;
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

    private MyHandler mMyhandler=new MyHandler(this);

    private Button mInitBtn;
    private TextView mInitTimeTv;
    private TextView mInitResultTv;

    private Button mVersionBtn;
    private TextView mVersionTimeTv;
    private TextView mVersionResultTv;

    private Button mAliasAndTagsBtn;
    private TextView mAliasAndTagsTimeTv;
    private TextView mAliasAndTagsResultTv;
    private EditText mAliasAndTagsEtAlias;
    private EditText mAliasAndTagsEtTags;

    private Button mAliasBtn;
    private TextView mAliasTimeTv;
    private TextView mAliasResultTv;
    private EditText mAliasEtAlias;

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

    private Button mDebugValueBtn;
    private TextView mDebugValueResultTv;

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
        mInitBtn= (Button) findViewById(R.id.init_btn);
        mInitTimeTv= (TextView) findViewById(R.id.init_time_tv);
        mInitResultTv= (TextView) findViewById(R.id.init_result_tv);

        mVersionBtn= (Button) findViewById(R.id.version_btn);
        mVersionTimeTv= (TextView) findViewById(R.id.version_time_tv);
        mVersionResultTv= (TextView) findViewById(R.id.version_result_tv);

        mAliasAndTagsBtn= (Button) findViewById(R.id.alias_and_tags_btn);
        mAliasAndTagsTimeTv= (TextView) findViewById(R.id.alias_and_tags_time_tv);
        mAliasAndTagsResultTv= (TextView) findViewById(R.id.alias_and_tags_result_tv);
        mAliasAndTagsEtAlias= (EditText) findViewById(R.id.alias_and_tags_et_alias);
        mAliasAndTagsEtTags= (EditText) findViewById(R.id.alias_and_tags_et_tags);

        mAliasBtn= (Button) findViewById(R.id.alias_btn);
        mAliasTimeTv= (TextView) findViewById(R.id.alias_time_tv);
        mAliasResultTv= (TextView) findViewById(R.id.alias_result_tv);
        mAliasEtAlias= (EditText) findViewById(R.id.alias_et_alias);

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

        mDebugValueBtn= (Button) findViewById(R.id.debug_value_btn);
        mDebugValueResultTv= (TextView) findViewById(R.id.debug_value_result_tv);

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

    private void handlerView(){
        handlerInitView();
        handlerVersionView();
        handlerAliasAndTagsView();
        handlerAliasView();
        handlerTagsView();
        handlerTriggerView();
        handlerDebugView();
        handlerDebugValueView();
        handlerStopView();
        handlerResumeView();
        handlerIsStopView();
    }

    private void handlerInitView(){
        mInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                EebbkPush.init(getApplicationContext(),new OnInitSateListener(){

                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_INIT;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_INIT;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("errorMsg",errorMsg);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });
            }
        });
    }
    private void handlerVersionView(){
        mVersionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                String str=EebbkPush.getSDKVersion();
                long endTime= System.currentTimeMillis();
                Message msg=new Message();
                msg.what=WHAT_SUCCESS;
                msg.arg1=ARG1_VERSION;
                Bundle bundle=new Bundle();
                bundle.putLong("time",endTime-startTime);
                bundle.putString("msg",str);
                msg.setData(bundle);
                mMyhandler.sendMessage(msg);
            }
        });
    }
    private void handlerAliasAndTagsView(){
        mAliasAndTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                String alias=mAliasAndTagsEtAlias.getText().toString();
                String tags=mAliasAndTagsEtTags.getText().toString();
                List<String> tagsList=StringUtils.stringtoList(tags);

                Log.d(TAG,"alias and tags--->"+alias+"::"+tags);

                EebbkPush.setAliasAndTags(alias, tagsList, new OnAliasAndTagsListener() {

                    @Override
                    public void onSuccess(String alias, List<String> tags) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_ALIAS_AND_TAGS;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("alias",alias);
                        bundle.putString("tags", StringUtils.listToString(tags));
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String alias, List<String> tags, String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_ALIAS_AND_TAGS;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("alias",alias);
                        bundle.putString("tags", StringUtils.listToString(tags));
                        bundle.putString("errorMsg", errorMsg);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });
            }
        });
    }



    private void handlerAliasView(){
        mAliasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                String alias=mAliasEtAlias.getText().toString();
                EebbkPush.setAlias(alias, new OnAliasAndTagsListener() {

                    @Override
                    public void onSuccess(String alias, List<String> tags) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_ALIAS;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("alias",alias);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String alias, List<String> tags, String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_ALIAS;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("alias",alias);
                        bundle.putString("errorMsg", errorMsg);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });
            }
        });
    }
    private void handlerTagsView(){
        mTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                String tags=mTagsEtTags.getText().toString();
                List<String> tagsTemp=StringUtils.stringtoList(tags);
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
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String alias, List<String> tags, String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_TAGS;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        bundle.putString("tags", StringUtils.listToString(tags));
                        bundle.putString("errorMsg", errorMsg);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });
            }
        });
    }

    private void handlerTriggerView(){
        mTriggerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                EebbkPush.sendPushSyncTrigger(new OnReceiveListener() {
                    @Override
                    public void onReceive(Request request, Response response) {
                        long endTime= System.currentTimeMillis();
                        if(response.isSuccess()){
                            Message msg=new Message();
                            msg.what=WHAT_SUCCESS;
                            msg.arg1=ARG1_TRIGGER;
                            Bundle bundle=new Bundle();
                            bundle.putLong("time",endTime-startTime);
                            msg.setData(bundle);
                            mMyhandler.sendMessage(msg);
                        }else{
                            Message msg=new Message();
                            msg.what=WHAT_FAIL;
                            msg.arg1=ARG1_TRIGGER;
                            Bundle bundle=new Bundle();
                            bundle.putLong("time",endTime-startTime);
                            bundle.putString("errorMsg",response.getDesc());
                            msg.setData(bundle);
                            mMyhandler.sendMessage(msg);
                        }
                    }
                });
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
                mMyhandler.sendMessage(msg);
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
                mMyhandler.sendMessage(msg);
            }
        });
    }

    private void handlerDebugValueView(){
        mDebugValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetting();
            }
        });
    }

    private void handlerStopView(){
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                EebbkPush.stopPush(new OnStopResumeListener() {
                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_STOP;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_STOP;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });

            }
        });
    }
    private void handlerResumeView(){
        mResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long startTime= System.currentTimeMillis();
                EebbkPush.resumePush(new OnStopResumeListener() {
                    @Override
                    public void onSuccess() {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_SUCCESS;
                        msg.arg1=ARG1_RESUME;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }

                    @Override
                    public void onFail(String errorMsg) {
                        long endTime= System.currentTimeMillis();
                        Message msg=new Message();
                        msg.what=WHAT_FAIL;
                        msg.arg1=ARG1_RESUME;
                        Bundle bundle=new Bundle();
                        bundle.putLong("time",endTime-startTime);
                        msg.setData(bundle);
                        mMyhandler.sendMessage(msg);
                    }
                });

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
                mMyhandler.sendMessage(msg);
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
            switch (msg.what){
                case WHAT_SUCCESS:
                    handleSuccess(act,msg);
                    break;
                case WHAT_FAIL:
                    handleFail(act,msg);
                    break;
            }
        }

        private void handleSuccess(InterFaceActivity act,Message msg){
            Bundle bundle=msg.getData();
            switch (msg.arg1){
                case ARG1_INIT:
                    act.mInitTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mInitResultTv.setText("成功");
                    break;
                case ARG1_VERSION:
                    act.mVersionTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mVersionResultTv.setText(bundle.getString("msg"));
                    break;
                case ARG1_ALIAS_AND_TAGS:
                    act.mAliasAndTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    String aliasAndTags="成功! "+"别名："+bundle.getString("alias")+"； 标签："+bundle.getString("tags");
                    act.mAliasAndTagsResultTv.setText(aliasAndTags);
                    break;
                case ARG1_ALIAS:
                    act.mAliasTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    String alias="成功! "+"别名："+bundle.getString("alias");
                    act.mAliasResultTv.setText(alias);
                    break;
                case ARG1_TAGS:
                    act.mTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    String tags="成功! "+"标签："+bundle.getString("tags");
                    act.mTagsResultTv.setText(tags);
                    break;
                case ARG1_DEBUG:
                    act.mDebugResultTv.setText(bundle.getString("mode"));
                    break;
                case ARG1_DEBUG_VALUE:
                    act.mDebugValueResultTv.setText(bundle.getString("mode"));
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
                case ARG1_INIT:
                    act.mInitTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mInitResultTv.setText(str);
                    break;
                case ARG1_ALIAS_AND_TAGS:
                    act.mAliasAndTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mAliasAndTagsResultTv.setText(str);
                    break;
                case ARG1_ALIAS:
                    act.mAliasTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mAliasResultTv.setText(str);
                    break;
                case ARG1_TAGS:
                    act.mTagsTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mTagsResultTv.setText(str);
                    break;
//                case ARG1_DEBUG:
//                    act.mDebugTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
//                    act.mDebugResultTv.setText(str);
//                    break;
//                case ARG1_DEBUG_VALUE:
//                    act.mDebugValueTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
//                    act.mDebugValueResultTv.setText(str);
//                    break;
                case ARG1_STOP:
                    act.mStopTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mStopResultTv.setText(str);
                    break;
                case ARG1_RESUME:
                    act.mResumeTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mResumeResultTv.setText(str);
                    break;
//                case ARG1_IS_STOP:
//                    act.mIsStopTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
//                    act.mIsStopResultTv.setText(str);
//                    break;
                case ARG1_TRIGGER:
                    act.mTriggerTimeTv.setText(TimeUtils.formatTime(bundle.getLong("time")));
                    act.mTriggerResultTv.setText(str);
                    break;
            }
        }
    }

    private void startSetting(){
        Intent intent=new Intent(this,DebugValueSettingActivity.class);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            Message msg=new Message();
            msg.what=WHAT_SUCCESS;
            msg.arg1=ARG1_DEBUG_VALUE;
            Bundle bundle=new Bundle();
            bundle.putString("mode",data.getStringExtra("mode"));
            msg.setData(bundle);
            mMyhandler.sendMessage(msg);
        }
    }
}
