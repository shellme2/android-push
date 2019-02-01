package com.eebbk.bfc.demo.push.basicfunction.function;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.im.push.communication.SyncAction;

public class FunctionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);

        initView();
    }

    private void initView(){
        findViewById(R.id.start_service1).setOnClickListener(this);
        findViewById(R.id.start_service2).setOnClickListener(this);
        findViewById(R.id.start_service3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_service1:
                break;

            case R.id.start_service2:
                break;

            case R.id.start_service3:
                break;
        }
    }


}
