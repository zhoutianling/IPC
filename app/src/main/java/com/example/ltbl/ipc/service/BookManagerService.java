package com.example.ltbl.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;

import com.example.ltbl.ipc.IBookManager;
import com.example.ltbl.ipc.IOnNewBookArrivedListener;
import com.example.ltbl.ipc.bean.Book;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RunnableFuture;

public class BookManagerService extends Service {

    @Override
    public void onCreate() {
        mBookList.add(new Book(1, "android群英传"));
        mBookList.add(new Book(2, "android开发艺术探索"));
        new Thread(new ProductBookThread()).start();
        super.onCreate();
    }

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
//    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList2 = new RemoteCallbackList<>();

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);

        }

        @Override
        public List<Book> getList() throws RemoteException {
//            SystemClock.sleep(5000);//模拟一个耗时的操作
            return mBookList;
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mListenerList.contains(listener)) {
//                mListenerList.add(listener);
//            }
            mListenerList2.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (mListenerList.contains(listener)) {
//                mListenerList.remove(listener);
//            }
            mListenerList2.unregister(listener);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class ProductBookThread implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                SystemClock.sleep(3000);
                Book book = new Book(i, "【Android讲义:" + i + "】");
                mBookList.add(book);
                //第一种方式
//                for (IOnNewBookArrivedListener listener : mListenerList) {
//                    try {
//                        listener.onNewBookArrived(book);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }

//第二种方式
                final int N = mListenerList2.beginBroadcast();
                for (int j = 0; j < N; j++) {
                    IOnNewBookArrivedListener listener = mListenerList2.getBroadcastItem(j);
                    if (listener != null) {
                        try {
                            listener.onNewBookArrived(book);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mListenerList2.finishBroadcast();
            }
        }
    }
}
