package eu.europeana.entitymanagement.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit test for testing the RecommendController class
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EMControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMyControllerValidInput() throws Exception {
        // with accept header
        mockMvc.perform(get("/{someRequest}", "123test")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.OK.value()));

        // without accept header
        mockMvc.perform(get("/{someRequest}", "123test"))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void testMyControllerInvalidInput() throws Exception {
        mockMvc.perform(get("/{someRequest}", "123-test")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

}
