package eu.europeana.entitymanagement.utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class UriValidator {

  // adapted from
  // https://github.com/apache/jena/blob/jena-4.3.0/jena-iri/src/main/java/org/apache/jena/iri/impl/Parser.java#L61
  private static final Predicate<String> URI_PATTERN =
      Pattern.compile(
              // scheme
              "^(https?|urn|geo):"
                  + // user
                  "(?://((([^/?#@]*)@)?"
                  + // host
                  "(?:\\[[^/?#]*]|([^/?#:]*))?"
                  + // port
                  "(?::([^/?#]*))?))?"
                  + // path
                  "(?:[^#?]*)?"
                  + // query
                  "(?:\\?([^#]*))?"
                  + // frag
                  "(?:#(.*))?",
              Pattern.DOTALL)
          .asMatchPredicate();

  /**
   * Checks if the given string is a valid URI. This means the string is either a valid URN or a
   * valid HTTP URL
   *
   * @param uri URI to validate
   * @return true if string param is a valid URI, false otherwise
   */
  public static boolean isUri(String uri) {
    return URI_PATTERN.test(uri);
  }

  public static boolean isLocalhost(String uri) {
    return uri.startsWith("http://localhost");
  }
}
