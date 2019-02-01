package com.eebbk.bfc.demo.push.basicfunction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.db.DbUtils;
import com.eebbk.bfc.demo.push.util.TimeUtils;
import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.communication.PushReceiver;
import com.eebbk.bfc.im.push.util.LogUtils;

import static android.content.Context.NOTIFICATION_SERVICE;


public class ModuleMessageReceiver extends PushReceiver {

    public static final String TAG = "ModuleMessageReceiver";
    public static final String MESSAGE_RECEIVER_ACTION ="com.eebbk.bfc.im_message_receive_action";

    private static final int NOTIFY_ID=12131352;

    @Override
    protected void onMessage(Context context, SyncMessage syncMessage) {
        if (syncMessage != null) {
            LogUtils.i(TAG, syncMessage.toString());

            byte[] temp=syncMessage.getMsg();
            String msg=new String(temp);
            msg=syncMessage.getModule()+" :: "+msg;

            LogUtils.e(TAG, msg);

            notification(context,msg);

            sendMessage(context,msg);
        } else {
            LogUtils.e(TAG, "sync message is null.");
        }
    }

    private void sendMessage(Context context,String msg){
        String time=TimeUtils.getNowTime();

        LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(context);
        Intent intent=new Intent();

        intent.setAction(MESSAGE_RECEIVER_ACTION);
        localBroadcastManager.sendBroadcast(intent);

        DbUtils.saveMessage(new MessageInfo(msg,time));
    }

    private void notification(Context context,String msg){
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIFY_ID);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setContentTitle("模块推送消息")//设置通知栏标题
                    .setContentText(msg) //<span style="font-family: Arial;">/设置通知栏显示内容</span>
                    .setContentIntent(getDefaultIntent(context,msg)) //设置通知栏点击意图
                //  .setNumber(number) //设置通知集合的数量
                    .setTicker("测试通知来啦") //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                    .setOngoing(false)//true，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                    .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                    //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                    .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
        }else {
            mBuilder.setContentTitle("模块推送消息")//设置通知栏标题
                    .setContentText(msg) //<span style="font-family: Arial;">/设置通知栏显示内容</span>
                    .setContentIntent(getDefaultIntent(context,msg)) //设置通知栏点击意图
                    //  .setNumber(number) //设置通知集合的数量
                    .setTicker("测试通知来啦") //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                    .setOngoing(false)//true，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                    .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                    //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                    .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
        }

        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    public PendingIntent getDefaultIntent(Context context, String msg){
        Intent intent=new Intent(context,MessageShowActivity.class);
        intent.putExtra("message",msg);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
