package com.eebbk.bfc.demo.push_1;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.communication.PushReceiver;


public class MessageReceiver extends PushReceiver {

    public static final String TAG = MessageReceiver.class.getName();

    @Override
    protected void onMessage(Context context, SyncMessage syncMessage) {
        if (syncMessage != null) {
            Log.i(TAG, syncMessage.toString());
            sendMessage(context,syncMessage);
        } else {
            Log.e(TAG, "sync message is null.");
        }
    }

    private void sendMessage(Context context,SyncMessage syncMessage){

        byte[] temp=syncMessage.getMsg();
        String msg=new String(temp);

        Log.e(TAG, msg);

        Intent intent=new Intent();
        intent.putExtra("message",msg);
        intent.setAction(MainActivity.MESSAGE_RECIEVER_ACTION);
        context.sendBroadcast(intent);
    }
}
