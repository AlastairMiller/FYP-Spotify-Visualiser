package al.miller.sv.ingest.controller;

import al.miller.sv.ingest.service.RefreshDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
@Slf4j
public class RefreshDbServiceController {

    private RefreshDbService refreshDbService;

    @Autowired
    public RefreshDbServiceController(RefreshDbService refreshDbService) {
        this.refreshDbService = refreshDbService;
    }

    @RequestMapping(value = "refreshStatus", method = RequestMethod.GET)
    public Boolean getRefreshDbServiceStatus(){
       return refreshDbService.getRefreshInProgress();
    }

}
