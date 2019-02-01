package com.eebbk.bfc.demo.push.flow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eebbk.bfc.demo.push.R;
import com.eebbk.bfc.demo.push.db.DbUtils;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private List<MessageInfo> mData;
    private Context mContext;

    public MessageAdapter(Context context){
        this.mData = DbUtils.getAllMessage();
        this.mContext=context;
    }

    @Override
    public void notifyDataSetChanged() {
        this.mData = DbUtils.getAllMessage();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public MessageInfo getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold hold;
        if(convertView==null){
            convertView= LayoutInflater.from(mContext).inflate(R.layout.message_listview_item_layout,parent,false);
            hold=new ViewHold();
            hold.msg= (TextView) convertView.findViewById(R.id.item_message_tv);
            hold.time= (TextView) convertView.findViewById(R.id.item_receive_time_tv);
            convertView.setTag(hold);
        }else {
            hold= (ViewHold) convertView.getTag();
        }

        MessageInfo info=getItem(position);
        hold.msg.setText(info.getMsg());
        hold.time.setText(info.getReceiveTime());

        return convertView;
    }

    public static class ViewHold{
        public TextView msg;
        public TextView time;
    }
}
