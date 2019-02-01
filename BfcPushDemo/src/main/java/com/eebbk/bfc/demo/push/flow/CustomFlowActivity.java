package com.eebbk.bfc.demo.push.flow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;

public class CustomFlowActivity extends AppCompatActivity {

    private Switch mPushSw;
    private Button mClearBtn;

    private EditText mAliasEt;
    private EditText mTagsEt;
    private Button mAliasAndTagsBtn;

    private Button mDebugOpenBtn;
    private Button mDebugCloseBtn;
    private Button mDebugExpertBtn;

    private TextView mCountTv;
    private ListView mMessageLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_flow);
        initView();
        initViewData();
    }

    private void initView(){
        mPushSw= (Switch) findViewById(R.id.push_sw);
        mClearBtn= (Button) findViewById(R.id.clear_btn);

        mAliasEt= (EditText) findViewById(R.id.alias_et);
        mTagsEt= (EditText) findViewById(R.id.tags_et);
        mAliasAndTagsBtn= (Button) findViewById(R.id.alias_and_tags_btn);

        mDebugCloseBtn= (Button) findViewById(R.id.debug_close_btn);
        mDebugOpenBtn=(Button) findViewById(R.id.debug_open_btn);
        mDebugExpertBtn=(Button) findViewById(R.id.debug_expert_btn);

        mCountTv= (TextView) findViewById(R.id.push_count_tv);
        mMessageLv= (ListView) findViewById(R.id.push_message_lv);
    }

    private void initViewData(){
        initPushSw();
        initAliasAndTags();
        initDebug();
        initListView();
        initOther();
    }

    private void initPushSw(){

    }

    private void initAliasAndTags(){

    }

    private void initDebug(){

    }

    private void initListView(){

    }

    private void initOther(){

    }
}
