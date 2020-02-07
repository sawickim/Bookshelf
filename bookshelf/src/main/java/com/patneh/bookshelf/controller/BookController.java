package com.patneh.bookshelf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patneh.bookshelf.storage.BookStorage;
import com.patneh.bookshelf.storage.impl.PostgresBookStorageImpl;
import com.patneh.bookshelf.storage.impl.StaticListBookStorageImpl;
import com.patneh.bookshelf.type.Book;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class BookController {

    private final static String BOOK_ID_PARAM_NAME = "bookId";

    private BookStorage bookStorage = new PostgresBookStorageImpl();

    public Response serveGetBookRequest(IHTTPSession session){
        Map<String, List<String>> requestParameter = session.getParameters();
        if(requestParameter.containsKey(BOOK_ID_PARAM_NAME)){
            List<String> bookIdParams = requestParameter.get(BOOK_ID_PARAM_NAME);
            String bookIdParam = bookIdParams.get(0);
            long bookId = 0;

            try{
                bookId=Long.parseLong(bookIdParam);
            }catch (NumberFormatException nfe){
                System.err.println("Error during request param: \n"+nfe);
                return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Request param 'bookId' has to be a number");
            }

            Book book = bookStorage.getBook(bookId);
            if(book!=null){
                try{
                    ObjectMapper objectMapper = new ObjectMapper();
                    String response = objectMapper.writeValueAsString(book);
                    return newFixedLengthResponse(OK, "application/json", response);
                }catch (JsonProcessingException e){
                    System.err.println("Error during proces request: \n"+e);
                    return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read book");
                }
            }
            return newFixedLengthResponse(NOT_FOUND, "application/json", "");
        }
        return newFixedLengthResponse(BAD_REQUEST, "text/plain", "Uncorrected request params.");
    }

    public NanoHTTPD.Response serveGetBooksRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        String response = "";

        try{
            response = objectMapper.writeValueAsString(bookStorage.getAllBooks());
        }catch (JsonProcessingException e){
            System.err.println("Error during process request: \n"+e);
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error can't read all books");
        }
        return newFixedLengthResponse(OK, "application/json", response);
    }

    public NanoHTTPD.Response serveAddBookRequest(IHTTPSession session){
        ObjectMapper objectMapper = new ObjectMapper();
        long randomBookId = System.currentTimeMillis();

        String lengthHeader = session.getHeaders().get("content-length");
        int contentLength = Integer.parseInt(lengthHeader);
        byte[] buffer = new byte[contentLength];

        try{
            session.getInputStream().read(buffer, 0, contentLength);
            String requestBody = new String(buffer).trim();
            Book requestBook = objectMapper.readValue(requestBody, Book.class);
            requestBook.setId(randomBookId);

            bookStorage.addBook(requestBook);
        }catch (Exception e){
            System.err.println("Error during proces request: \n"+e);
            return newFixedLengthResponse(INTERNAL_ERROR, "text/plain", "Internal error book hasn't been added");
        }
        return newFixedLengthResponse(OK, "text/plain", "Book has been successfully added, id="+randomBookId);
    }

}
