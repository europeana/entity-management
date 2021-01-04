package eu.europeana.entitymanagement.web;

import javax.validation.constraints.Pattern;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example Rest Controller class with input validation
 */
@RestController
@Validated
public class EMController {

    private static final String MY_REGEX = "^[a-zA-Z0-9_]*$";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid parameter.";

    /**
     * Test endpoint
     * @param someRequest an alpha-numerical String
     * @return just something, doesn't matter what
     */
    @GetMapping(value = "/{someRequest}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleMyApiRequest(
        @PathVariable(value = "someRequest")
            @Pattern(regexp = MY_REGEX, message = INVALID_REQUEST_MESSAGE) String someRequest) {
        return "It works!";
    }
}
