package com.eebbk.bfc.demo.push.basicfunction.interfaceTest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.im.push.SDKVersion;

public class VersionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        initView();
    }

    private void initView(){
        TextView versionInfoTv= (TextView) findViewById(R.id.version_info_tv);

        String sb = "版本序号: " + SDKVersion.getSDKInt() +
                "\r\n \r\n库名称: " + SDKVersion.getLibraryName() +
                "\r\n \r\n版本名称: " + SDKVersion.getVersionName() +
                "\r\n \r\n构建版本: " + SDKVersion.getBuildName() +
                "\r\n \r\n构建时间: " + SDKVersion.getBuildTime() +
                "\r\n \r\nTAG标签: " + SDKVersion.getBuildTag() +
                "\r\n \r\nHEAD值: " + SDKVersion.getBuildHead();

        versionInfoTv.setText(sb);
    }
}
