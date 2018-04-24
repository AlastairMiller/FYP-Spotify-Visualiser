package com.sv.svi.controller;

import com.sv.svi.service.RefreshDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
@Slf4j
public class RefreshDbServiceController {

    //Todo Remove field injection, bit naughty >:(
    @Autowired
    private RefreshDbService refreshDbService;

    @RequestMapping(value = "refreshStatus", method = RequestMethod.GET)
    public Boolean getRefreshDbServiceStatus(){
       return refreshDbService.getRefreshInProgress();
    }

}
