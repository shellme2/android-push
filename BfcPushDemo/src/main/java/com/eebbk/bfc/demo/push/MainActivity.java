package com.eebbk.bfc.demo.push;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.basicfunction.BasicFunctionActivity;
import com.eebbk.bfc.demo.push.db.DbManager;
import com.eebbk.bfc.demo.push.performance.PerformanceActivity;
import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.util.LogUtils;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission = new CheckPermission(this);

        initView();

    }

    private static final int REQUEST_CODE = 0;//请求码

    private CheckPermission checkPermission;//检测权限器

    //配置需要取的权限
    public static final String[] PERMISSION = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            android.Manifest.permission.READ_EXTERNAL_STORAGE,  //读取权限

            android.Manifest.permission.READ_PHONE_STATE //读电话状态
    };
    @Override
    protected void onResume() {
        super.onResume();
        //缺少权限时，进入权限设置页面
        if (checkPermission.permissionSet(PERMISSION)) {
            startPermissionActivity();
            LogUtils.i("liuyewu", "start permission -->");
        }
    }

    //进入权限设置页面
    private void startPermissionActivity() {
        PermissionActivity.startActivityForResult(this, REQUEST_CODE, PERMISSION);
    }

    //返回结果回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //拒绝时，没有获取到主要权限，无法运行，关闭页面
        if (requestCode == REQUEST_CODE && resultCode == PermissionActivity.PERMISSION_DENIEG) {
            finish();
        }

        LogUtils.i("liuyewu", "onActivityResult start permission -->");

        DbManager.initializeInstance(this);

        //默认初始化设置,实际应用时最好在这里初始化
        EebbkPush.init(getApplicationContext(), new OnInitSateListener() {
            @Override
            public void onSuccess() {
                LogUtils.i("", "sync push init success!!!");
            }

            @Override
            public void onFail(String errorMsg, String errorCode) {
                LogUtils.i("", "sync push init fail msg-->" + errorMsg);
            }
        }, new OnPushStatusListener() {
            @Override
            public void onPushStatus(int status, Object... values) {

            }
        });

    }

    private void initView(){
        findViewById(R.id.basic_function_btn).setOnClickListener(this);
        findViewById(R.id.performance_btn).setOnClickListener(this);
        findViewById(R.id.safe_btn).setOnClickListener(this);
        findViewById(R.id.limit_btn).setOnClickListener(this);
        findViewById(R.id.other_btn).setOnClickListener(this);

        TextView serial= (TextView) findViewById(R.id.serial_tv);
        LogUtils.d("zjf","序列号："+Build.SERIAL);
        serial.setText("序列号："+Build.SERIAL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.basic_function_btn:
                enterBasicFunctionTest();
                break;

            case R.id.performance_btn:
                enterPerformanceTest();
                break;

            case R.id.safe_btn:
                enterPerformanceTest();
                break;

            case R.id.limit_btn:
//                enterPerformanceTest();
                Da.record(getApplicationContext(), new DaInfo()
                        .setFunctionName("func测试")
                        .setModuleDetail("modu测试")
                        .setTrigValue("haha数据123")
                        .setExtendSdkVersion()
                        .setExtendRemoteIp("213.4547.44")
                        .setExtendRemotePort("5575"));
                break;

            case R.id.other_btn:
                enterPerformanceTest();
                break;
        }
    }

    private void enterBasicFunctionTest(){
        Intent intent=new Intent(this, BasicFunctionActivity.class);
        startActivity(intent);
    }


    private void enterPerformanceTest(){
        Intent intent=new Intent(this, PerformanceActivity.class);
        startActivity(intent);
    }
}
