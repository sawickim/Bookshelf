package com.patneh.bookshelf;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class BookshelfApp extends NanoHTTPD {

    private RequestUrlMapper requestUrlMapper = new RequestUrlMapper();

    public BookshelfApp(int port) throws IOException {
        super(port);
        start(5000, false);
            System.out.println("System has been started.");
    }

    public static void main(String[] args) {
        try{
            new BookshelfApp(8080);
        }catch (IOException e){
            System.err.println("Server can't started because of error: \n"+e);
        }
    }

    @Override
    public Response serve(IHTTPSession session){
            return requestUrlMapper.delegateRequest(session);
    }
}
