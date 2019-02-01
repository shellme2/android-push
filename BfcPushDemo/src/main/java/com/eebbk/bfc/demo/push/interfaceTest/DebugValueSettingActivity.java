package com.eebbk.bfc.demo.push.interfaceTest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eebbk.bfc.demo.push.R;

public class DebugValueSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_value_setting);
        initView();
    }

    private void initView(){
        retrunData();
    }

    private void retrunData(){
        Intent intent=new Intent();
        intent.putExtra("mode","调试模式打开，设置完成");
        setResult(1,intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
