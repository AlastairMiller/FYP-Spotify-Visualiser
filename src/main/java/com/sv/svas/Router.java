package com.sv.svas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class Router {
    URL sisBaseURL;

    public void sendIngestionRequest() throws IOException {
        URL refreshRequest = new URL(sisBaseURL.toString() + "/api/v1/refresh");
        HttpURLConnection connection = (HttpURLConnection) refreshRequest.openConnection();
        connection.setRequestMethod("GET");
        int response = connection.getResponseCode();
        connection.disconnect();
        if (response == 200) {
            log.info("Refresh successfully started");
        } else if (response == 208) {
            log.info("Refresh in progress already");
        } else {
            log.error("Error sending request to ingest service. Error code: {}", response);
        }
    }
}
