package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entitymanagement.common.config.I18nConstants;

public class ParamValidationException extends EuropeanaApiException {

	public ParamValidationException(String msg) {
		super(msg);
	}

	@Override
	public boolean doLog() {
		return false;
	}

	@Override
	public boolean doLogStacktrace() {
		return false;
	}

	@Override
	public HttpStatus getResponseStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
