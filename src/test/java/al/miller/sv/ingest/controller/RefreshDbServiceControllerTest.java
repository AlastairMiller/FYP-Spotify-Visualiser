package al.miller.sv.ingest.controller;

import al.miller.sv.ingest.service.RefreshDbService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RefreshDbServiceController.class)
@AutoConfigureMockMvc
public class RefreshDbServiceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    RefreshDbService mockService;

    @Before
    public void setUp() {
        when(mockService.getRefreshInProgress()).thenReturn(true);
    }

    @Test
    public void getRefreshDbServiceStatus() throws Exception {

    mockMvc.perform(get("/api/v1/refreshStatus"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

    verify(mockService, times(1)).getRefreshInProgress();
    verifyNoMoreInteractions(mockService);

    }
}
