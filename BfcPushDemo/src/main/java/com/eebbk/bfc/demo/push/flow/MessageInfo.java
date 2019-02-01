package com.eebbk.bfc.demo.push.flow;

public class MessageInfo {
    private String msg;
    private String receiveTime;

    public MessageInfo(){}

    public MessageInfo(String msg,String receiveTime){
        this.msg=msg;
        this.receiveTime=receiveTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }
}
