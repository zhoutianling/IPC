package com.example.ltbl.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BookListService extends Service {
    public BookListService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
