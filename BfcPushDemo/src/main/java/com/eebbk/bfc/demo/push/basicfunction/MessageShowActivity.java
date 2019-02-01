package com.eebbk.bfc.demo.push.basicfunction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.im.push.util.LogUtils;

public class MessageShowActivity extends AppCompatActivity {

    private static final String TAG = "MessageShowActivity";

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_show);
        initView();
        handler();
    }

    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume----> ");
//        handler();
        super.onResume();
    }

    private void initView(){
        message= (TextView) findViewById(R.id.message_tv);
    }

    private void handler(){
        Intent intent=getIntent();
        String msg=intent.getStringExtra("message");
        LogUtils.d(TAG, "msg----> "+msg);
        message.setText(msg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        handler();
        LogUtils.d(TAG, "onNewIntent----> ");
        super.onNewIntent(intent);
    }
}
