package eu.europeana.entitymanagement.common.vocabulary;

public abstract class EMI18nConstants
    implements eu.europeana.api.commons.definitions.config.i18n.I18nConstants {

  private EMI18nConstants() {
    // private constructor to hide implicit one
  }

  // shared error messages

  // 400
  static final String BAD_HEADER_REQUEST = "error.extension_and_accept";

  // 401
  // replaced by generic error message
  static final String NO_APPLICATION_FOR_APIKEY = "error.entity_no_application_for_apikey";

  // 404
  // replaced by generic constant
  static final String CANT_FIND_BY_SAME_AS_URI = "error.entity_same_as_not_found";
  static final String UNSUPPORTED_ENTITY_TYPE = "error.entity_unsupported_type";
  static final String UNSUPPORTED_ALGORITHM_TYPE = "error.algorithm_unsupported_type";
  static final String MESSAGE_NOT_ACCESSIBLE = "error.entity_not_accessible";

  // 406
  static final String INVLAID_HEADER_REQUEST = "error.invalid_header_value";

  // 500
  static final String SERVER_ERROR_CANT_RETRIEVE_URI = "error.entity_server_cannot_retrieve_uri";
  static final String SERVER_ERROR_CANT_RESOLVE_SAME_AS_URI =
      "error.entity_server_cannot_resolve_uri";

  static final String CONCEPT_SCHEME_CANT_PARSE_BODY = "error.concept_scheme_cant_parse_body";

  static final String UNSUPPORTED_TOKEN_TYPE = "error.entity_unsupported_token_type";
  static final String INVALID_HEADER_FORMAT = "error.entity_invalid_header_format";
  static final String BASE64_DECODING_FAIL = "error.entity_base64_encoding_fail";
  static final String INVALID_TOKEN = "error.entity_invalid_token";
  static final String USER_NOT_AUTHORIZED = "error.entity_user_not_authorized";
  static final String TEST_USER_FORBIDDEN = "error.entity_test_user_forbidden";
  static final String ENTITY_VALIDATION_MANDATORY_PROPERTY =
      "error.entity_validation_mandatory_property";
  static final String ENTITY_VALIDATION_PROPERTY_VALUE = "error.entity_validation_property_value";

  static final String INVALID_HEADER_VALUE = "error.entity_invalid_header_value";
  static final String ENTITY_NOT_FOUND = "error.entity_not_found";
}
