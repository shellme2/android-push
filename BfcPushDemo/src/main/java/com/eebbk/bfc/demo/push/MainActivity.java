package com.eebbk.bfc.demo.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.eebbk.bfc.demo.push.flow.WholeFlowActivity;
import com.eebbk.bfc.demo.push.function.FunctionActivity;
import com.eebbk.bfc.demo.push.interfaceTest.InterFaceActivity;
import com.eebbk.bfc.demo.push.performance.PerformanceActivity;


public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView(){
        findViewById(R.id.function_btn).setOnClickListener(this);
        findViewById(R.id.performance_btn).setOnClickListener(this);
        findViewById(R.id.interface_btn).setOnClickListener(this);
        findViewById(R.id.whole_flow_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.whole_flow_btn:
                enterFlowTest();
                break;

            case R.id.interface_btn:
                enterInterFaceTest();
                break;

            case R.id.function_btn:
                enterFunctionTest();
                break;

            case R.id.performance_btn:
                enterPerformanceTest();
                break;
        }
    }

    private void enterFlowTest(){
        Intent intent=new Intent(this, WholeFlowActivity.class);
        startActivity(intent);
    }

    private void enterInterFaceTest(){
        Intent intent=new Intent(this, InterFaceActivity.class);
        startActivity(intent);
    }

    private void enterFunctionTest(){
        Intent intent=new Intent(this, FunctionActivity.class);
        startActivity(intent);
    }

    private void enterPerformanceTest(){
        Intent intent=new Intent(this, PerformanceActivity.class);
        startActivity(intent);
    }
}
