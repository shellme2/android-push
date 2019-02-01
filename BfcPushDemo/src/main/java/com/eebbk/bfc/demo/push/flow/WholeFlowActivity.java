package com.eebbk.bfc.demo.push.flow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.im.push.util.AppUtil;

/**
 * 整体流程的设计
 */
public class WholeFlowActivity extends Activity implements View.OnClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow);
        initView();
    }

    private void initView(){
        findViewById(R.id.default_btn).setOnClickListener(this);
        findViewById(R.id.custom_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.default_btn:
                entryDefaultFlow();
                break;

            case R.id.custom_btn:
                entryCustomFlpow();
                break;
        }
    }

    private void entryDefaultFlow(){
        Intent intent=new Intent(this,DefaultFlowActivity.class);
        startActivity(intent);
    }

    private void entryCustomFlpow(){
        Intent intent=new Intent(this,CustomFlowActivity.class);
        startActivity(intent);
    }
}
