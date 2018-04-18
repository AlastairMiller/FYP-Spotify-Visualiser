package com.sv.svi;

import com.svd.ClientHandler;
import com.svd.dao.MongoDao;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoHelper {

    @Getter
    private final MongoDao mongoDao;

    public MongoHelper(@Value("${mongo.hostname:localhost}") String hostname, @Value("${mongo.port:27017}") int port,
                       @Value("{$mongo.username:mongo}") String username, @Value("${mongo.dbname:spotify}") String dbname, @Value("${mongo.password:mongo}") String password) {

        mongoDao = new MongoDao(new ClientHandler(hostname, port, username, dbname, password.toCharArray()));
    }
}
