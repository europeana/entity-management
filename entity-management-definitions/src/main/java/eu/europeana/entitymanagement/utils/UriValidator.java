package eu.europeana.entitymanagement.utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class UriValidator {

  private static final Predicate<String> URN_MATCH_PREDICATE =
      Pattern.compile(
              "^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$",
              Pattern.CASE_INSENSITIVE)
          .asMatchPredicate();

  // only http / https schemes supported, non-latin characters should be percent-encoded
  private static final Predicate<String> URL_MATCH_PREDICATE =
      Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
          .asMatchPredicate();

  /**
   * Checks if the given string is a valid HTTP URL. URL should be absolute and contain a scheme.
   * Only URLs starting with http and https are supported.
   *
   * @param url URL to validate
   * @return true if string param is a valid URL, false otherwise
   */
  public static boolean isValidUrl(String url) {
    return URL_MATCH_PREDICATE.test(url);
  }

  /**
   * Checks if the given string is a valid URN. A URN is a URI with the "urn:" scheme.
   *
   * @param urn URN to validate
   * @return true if string param is a valid URN, false otherwise
   */
  public static boolean isValidUrn(String urn) {
    return URN_MATCH_PREDICATE.test(urn);
  }

  /**
   * Checks if the given string is a valid URI. This means the string is either a valid URN or a
   * valid HTTP URL
   *
   * @param uri URI to validate
   * @return true if string param is a valid URI, false otherwise
   */
  public static boolean isValidUri(String uri) {
    return isValidUrl(uri) || isValidUrn(uri);
  }
}
