package edu.itc.se.lab01.task4;

import java.util.concurrent.Callable;
import java.io.IOException;
public class FakeDownload implements Callable<String>{
    private final String url;
    private final int id;

    public FakeDownload(String url, int id){
        this.url = url;
        this.id = id;
    }

    @Override 
    public String call() throws Exception{
        if(id == 10){
            Thread.sleep(5000);
        }else{
            Thread.sleep(200);
        }

        if(id % 50 == 0){
            throw new IOException("boom");
        }

        return url + " -> " + url.length();
    }
}
