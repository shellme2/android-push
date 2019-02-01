package com.eebbk.bfc.im.push.communication;

import android.os.RemoteException;

import com.eebbk.bfc.im.push.IConnectionService;

public interface AIDLTask<T> {

    T submit(IConnectionService iConnectionService) throws RemoteException;

    void execute(IConnectionService iConnectionService) throws RemoteException;
}
