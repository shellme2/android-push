package com.eebbk.bfc.im.push.communication;

import android.os.RemoteException;

import com.eebbk.bfc.im.push.IConnectionService;


public class AIDLTaskImpl<T> implements AIDLTask<T> {

    public T submit(IConnectionService iConnectionService) throws RemoteException {
        return null;
    }

    public void execute(IConnectionService iConnectionService) throws RemoteException {}

}
