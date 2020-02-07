package com.patneh.bookshelf.storage;

import com.patneh.bookshelf.type.Book;

import java.util.List;

public interface BookStorage {
    Book getBook(long id);
    List<Book> getAllBooks();
    void addBook(Book book);

}
