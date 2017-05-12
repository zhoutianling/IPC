package com.example.ltbl.ipc;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.ltbl.ipc.bean.Book;
import com.example.ltbl.ipc.bean.User;
import com.example.ltbl.ipc.service.BookManagerService;
import com.example.ltbl.ipc.service.MessgengerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Messenger messenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Constance.uid = 2;
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, SecondActivity.class));
//                persistFile();
                readFile();
//                bindService(new Intent(MainActivity.this, BookManagerService.class), mCon2, BIND_AUTO_CREATE);
//                bindService(new Intent(MainActivity.this, MessgengerService.class), mCon, BIND_AUTO_CREATE);
                /*远程方法中执行耗时会发生ANR,正确执行耗时操作姿势，避免ANR的方式：？*/
//                try {
//                    bookManager.getList();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            List<Book> bookList = bookManager.getList();
//                            Log.i("zzz", "BookList.size=" + bookList.size());
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }
        });

    }

    private IBookManager bookManager;
    private ServiceConnection mCon2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("zzz", "service connected");
            bookManager = IBookManager.Stub.asInterface(iBinder);
            try {
                iBinder.linkToDeath(mDeadthRecipient, 0);//设置死亡代理
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                List<Book> list = bookManager.getList();
                for (Book book : list) {
                    Log.i("zzz", "bookName-->" + book.getName());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            try {
                bookManager.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("zzz", "service connected");
            bookManager = null;
        }
    };
    /**
     * 使用传统方式回调
     */
    private IOnNewBookArrivedListener listener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            Log.i("zzz", "NewBookArrived--->" + newBook.getName());
        }
    };
    /***
     * Binder:linkToDeath,unlinkToDeath
     */
    private IBinder.DeathRecipient mDeadthRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.i("zzz", "Service died,rebind later....");
            if (bookManager == null) {
                return;
            } else {
                bookManager.asBinder().unlinkToDeath(mDeadthRecipient, 0);
                bookManager = null;
                bindService(new Intent(MainActivity.this, BookManagerService.class), mCon2, BIND_AUTO_CREATE);
            }
        }
    };

    /***
     * Messenger通信
     */
    private ServiceConnection mCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);
            Message msg = Message.obtain(null, 0);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "你好，我是客户端！");
            msg.setData(bundle);
            msg.replyTo = mReplyMessenger;
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCon = null;
        }
    };
    private Messenger mReplyMessenger = new Messenger(new MessengerHandler());

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Log.i("zzz", "接收到服务端的回复：" + msg.getData().getString("reply"));
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /***
     * 读数据
     */
    private void readFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = null;
                String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "User" + File.separator + "user.txt";
                File file = new File(filePath);
                if (file.exists()) {
                    ObjectInputStream ois = null;
                    try {
                        ois = new ObjectInputStream(new FileInputStream(filePath));
                        user = (User) ois.readObject();
                        Log.i("zzz", "user name=" + user.userName);
                        Log.i("zzz", "user id=" + user.userId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /***
     * 持久化数据
     */
    private void persistFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = new User(10, "Lily", false);
                File files = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "User");
                if (!files.exists()) {
                    Log.i("zzz", "create file success");
                    files.mkdirs();
                }
                File file = new File(files + File.separator + "user.txt");
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                    oos.writeObject(user);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
//        unbindService(mCon);

        /*用传统方式注册的回调这里为什么没有解注册成功*/
        unbindService(mCon2);
//        if (bookManager != null && bookManager.asBinder().isBinderAlive()) {
        try {
            Log.i("zzz", "onDestroy");
            bookManager.unregisterListener(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        }

        super.onDestroy();
    }
}
