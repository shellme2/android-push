package com.eebbk.bfc.demo.push.performance;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.Constant;
import com.eebbk.bfc.demo.push.PushTestApplication;
import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.debug.DebugPushSendTool;
import com.eebbk.bfc.demo.push.debug.PushAnalysisTool;
import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.debug.DebugEventInfo;
import com.eebbk.bfc.im.push.debug.DebugEventTool;

public class PerformanceActivity extends Activity implements View.OnClickListener ,IView, CompoundButton.OnCheckedChangeListener {
    private TextView mBasic;

    private Switch mDebugSwitch;
    private TextView mDebugState;

    private TextView mSuccessCount;
    private TextView mFailedCount;
    private TextView mReceviedCount;
    private TextView mReceivedTimeOutCount;

    private TextView mTCPConnState;

    private TextView mReceviedRate;

    private Spinner mAppListSpinner;
    private EditText mCountET;
    private Spinner mTimeList;

    private ArrayAdapter<String> adapter = null;
    private ArrayAdapter<Integer> timeAdapter = null;
    private static final String [] appList ={Constant.SendRelease.APP_NAME, Constant.SendDebug.APP_NAME};
    private static final Integer [] timeList ={10,20,40,60,100};

    private Button startBtn;
    private Button endBtn;
    private Button exportBtn;
    private Button pauseBtn;

    private String currentAppName = appList[0];
    private int currentPeroidTime = timeList[0];

    PushAnalysisTool mAnalysisTool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_performance);
        initView();
        mAnalysisTool = new PushAnalysisTool(this.getApplicationContext(),this);
    }

    private void initView() {
        startBtn= (Button) findViewById(R.id.p_test_start);
        startBtn.setOnClickListener(this);
        endBtn= (Button) findViewById(R.id.p_test_end);
        endBtn.setOnClickListener(this);

        pauseBtn= (Button) findViewById(R.id.p_test_pause);
        pauseBtn.setOnClickListener(this);

        exportBtn= (Button) findViewById(R.id.p_test_export);
        exportBtn.setOnClickListener(this);

        mBasic = (TextView) findViewById(R.id.p_test_basic_info);
        mDebugState = (TextView) findViewById(R.id.p_test_debug_state);

        mSuccessCount = (TextView) findViewById(R.id.p_test_send_count);
        mFailedCount = (TextView) findViewById(R.id.p_test_send_failed_count);
        mReceviedCount = (TextView) findViewById(R.id.p_test_receive_count);
        mReceivedTimeOutCount = (TextView) findViewById(R.id.p_test_receive_timeout_count);

        mTCPConnState = (TextView) findViewById(R.id.p_test_tcp_conn_state);

        mReceviedRate = (TextView) findViewById(R.id.p_test_receive_rate);

        mDebugSwitch= (Switch) findViewById(R.id.p_test_debug_switch);
        mDebugSwitch.setOnCheckedChangeListener(this);

        mAppListSpinner = (Spinner) findViewById(R.id.p_test_app_list_sp);
        mTimeList = (Spinner) findViewById(R.id.p_test_time_list_sp);

        adapter =   new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,appList);
        timeAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, timeList);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到spinner中去
        mAppListSpinner.setAdapter(adapter);
        mAppListSpinner.setVisibility(View.VISIBLE);//设置默认显示
        mAppListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                currentAppName = appList[arg2];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mTimeList.setAdapter(timeAdapter);
        mTimeList.setVisibility(View.VISIBLE);
        mTimeList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPeroidTime = timeList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCountET = (EditText) findViewById(R.id.p_test_send_count_each_et);
        //初始化数据
        if (PushTestApplication.sBfcPush != null) {
            DebugBasicInfo debugBasicInfo = PushTestApplication.sBfcPush.getDebugBasicInfo();
            mBasic.setText(debugBasicInfo.toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.p_test_start:
                String count = mCountET.getText().toString();
                int countInt = 1;
                try {
                    countInt = Integer.parseInt(count);
                    countInt = countInt > 15 ? 15 : countInt;
                } catch (Exception e) {
                    countInt = 1;
                }
                //todo
                mAnalysisTool.setPushDebugMode(DebugPushSendTool.URL_MODE_RELEASE,currentAppName,countInt,currentPeroidTime*1000);
                mAnalysisTool.startTest();
                break;
            case R.id.p_test_pause:
                mAnalysisTool.pauseTest();
                break;
            case R.id.p_test_export:
                mAnalysisTool.saveTestReport();
                break;
            case R.id.p_test_end:
                mAnalysisTool.endTest();
                break;
            default:
                break;
        }
    }


    @Override
    public void onEvent(DebugEventInfo debugEventInfo) {

    }

    @Override
    public void onDebugModeUpdate(String state) {
        mDebugState.setText(state);
        if (DebugEventTool.PUSH_DEBUG_ON.equals(state)) {
            startBtn.setEnabled(true);
            endBtn.setEnabled(true);
        }else{
            startBtn.setEnabled(false);
            endBtn.setEnabled(false);
        }
    }

    @Override
    public void onSendUpdate(int sendSuccessCount, int sendFailedCount,  int receivedCount,int receivedTimeoutCount) {
        mSuccessCount.setText(sendSuccessCount + "");
        mFailedCount.setText(sendFailedCount + "");
        mReceviedCount.setText(receivedCount + "");
        mReceivedTimeOutCount.setText(receivedTimeoutCount+"");
        int all = sendSuccessCount + sendFailedCount;
        float rate = 0;
        if (sendSuccessCount != 0) {
            rate = ((float)receivedCount * 100 / (float)sendSuccessCount);
        }
        mReceviedRate.setText(sendSuccessCount+"+"+sendFailedCount+"="+all+" , "+receivedCount+"/"+sendSuccessCount+"="+rate+"%");
    }

    @Override
    public void onTcpConnStateUpdate(String connState) {
        mTCPConnState.setText(connState + "");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            //开
            mAnalysisTool.turnOnDebugMode();
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(true);
            exportBtn.setEnabled(true);
            endBtn.setEnabled(true);
        }else {
            //关
            mAnalysisTool.pauseTest();
            mAnalysisTool.turnOffDebugMode();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(false);
            exportBtn.setEnabled(false);
            endBtn.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnalysisTool.destroy();
    }
}
