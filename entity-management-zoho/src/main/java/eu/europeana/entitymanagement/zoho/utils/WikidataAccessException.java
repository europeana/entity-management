package eu.europeana.entitymanagement.zoho.utils;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class WikidataAccessException extends EuropeanaApiException {

    private static final long serialVersionUID = 7724261367420984595L;


    public WikidataAccessException(String msg, Throwable t) {
        super(msg,  t);
    }

    public WikidataAccessException(String msg) {
        super(msg);
    }
}
