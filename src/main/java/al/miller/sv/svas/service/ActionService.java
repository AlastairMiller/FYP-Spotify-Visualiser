package al.miller.sv.svas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@PropertySource("classpath:application.properties")
public class ActionService {
    private String sviBaseURL;
    private int sviPort;

    @Autowired
    public ActionService(@Value("${svi.baseURL}") String sviBaseURL, @Value("${svi.port}") int sviPort) {
        this.sviBaseURL = sviBaseURL;
        this.sviPort = sviPort;
    }

    @Scheduled(fixedRate = 50000)
    public void doScheduled() {
        checkIngestStatus();
    }

    private void checkIngestStatus() {
        HttpURLConnection connection = null;
        try {

            URL refreshRequest = new URL(sviBaseURL + ":" + sviPort + "/api/v1/refreshStatus");
            connection = (HttpURLConnection) refreshRequest.openConnection();
            connection.setRequestMethod("GET");
            connection.getInputStream();
            connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response.toString().equals("true")) {
                log.info("Refresh in progress");
            } else if (response.toString().equals("false")) {
                log.error(response.toString());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            assert connection != null;
            connection.disconnect();
        }

    }
}
