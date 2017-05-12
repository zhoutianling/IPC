// IBookManager.aidl
package com.example.ltbl.ipc;
import com.example.ltbl.ipc.bean.Book;
import com.example.ltbl.ipc.IOnNewBookArrivedListener;
// Declare any non-default types here with import statements

interface IBookManager {
void addBook(in Book book);
List<Book> getList();



void registerListener(IOnNewBookArrivedListener listener);
void unregisterListener(IOnNewBookArrivedListener listener);
}
