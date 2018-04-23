package com.sv.svas.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class Scheduler {
    URL sisBaseURL;
    ThreadPoolExecutor executor;


    public void main(){



    }

    public boolean startIngestion() throws IOException {
            URL refreshRequest = new URL(sisBaseURL.toString() + "/api/v1/refresh" );
            HttpURLConnection connection = (HttpURLConnection) refreshRequest.openConnection();
            connection.setRequestMethod("GET");
            int response = connection.getResponseCode();
            connection.disconnect();
            return response == 200;
    }
}
