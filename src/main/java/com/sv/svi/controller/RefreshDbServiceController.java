package com.sv.svi.controller;

import com.sv.svi.service.RefreshDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
@Slf4j
public class RefreshDbServiceController {

    @Autowired
    private RefreshDbService refreshDbService;

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public ResponseEntity<Object> setRefreshDbService(){
        if(!refreshDbService.getRefreshInProgress()) {
            log.info("Refresh request received from scheduler service");
            refreshDbService.refresh();
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
    }

}
