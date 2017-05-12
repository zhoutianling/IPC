// IOnNewBookArrivedListener.aidl
package com.example.ltbl.ipc;
import com.example.ltbl.ipc.bean.Book;


interface IOnNewBookArrivedListener {
 void onNewBookArrived(in Book newBook);
}
