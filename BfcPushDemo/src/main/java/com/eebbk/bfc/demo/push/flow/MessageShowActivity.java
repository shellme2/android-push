package com.eebbk.bfc.demo.push.flow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;

public class MessageShowActivity extends Activity {

    private static final String TAG=MessageShowActivity.class.getName();

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_show);
        initView();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume----> ");
        handler();
        super.onResume();
    }

    private void initView(){
        message= (TextView) findViewById(R.id.message_tv);
    }

    private void handler(){
        Intent intent=getIntent();
        String msg=intent.getStringExtra("message");
        Log.d(TAG, "msg----> "+msg);
        message.setText(msg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handler();
        Log.d(TAG, "onNewIntent----> ");
        super.onNewIntent(intent);
    }
}
