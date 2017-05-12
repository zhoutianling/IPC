package com.example.ltbl.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessgengerService extends Service {
    public MessgengerService() {
    }

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String msgStr = msg.getData().getString("msg");
                    Log.i("zzz", "接收到客户端的消息--->" + msgStr);
                    Messenger messenger = msg.replyTo;
                    Message replyMessage = Message.obtain(null, 2);
                    Bundle bundle = new Bundle();
                    bundle.putString("reply", "你的消息已收到，稍后回复你！");
                    replyMessage.setData(bundle);
                    try {
                        if (messenger != null)
                            messenger.send(replyMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    Messenger messenger = new Messenger(new MessengerHandler());

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
}
