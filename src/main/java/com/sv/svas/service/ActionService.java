package com.sv.svas.service;

import com.sv.svas.Router;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@AllArgsConstructor
@Slf4j
public class ActionService {
    Boolean ingestBusy;
    Router router;
    ThreadPoolExecutor executor;

    @Scheduled(fixedRate = 5000)
    public void doScheduled() {
        if (!ingestBusy) {
            try {
                router.sendIngestionRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
