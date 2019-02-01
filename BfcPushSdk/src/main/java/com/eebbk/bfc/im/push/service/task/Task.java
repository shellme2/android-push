package com.eebbk.bfc.im.push.service.task;

public abstract class Task implements Runnable, Comparable {

    protected boolean executed;

    protected void startTask() {
        executed = true;
    }

    public boolean execute() {
        return executed;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
