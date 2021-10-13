package eu.europeana.entitymanagement.zoho.utils;

import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.UserSignature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZohoInMemoryTokenStore implements TokenStore {

  /** Contains tokens mapped to user email addresses */
  private final Map<String, Token> tokenStore = new ConcurrentHashMap<>();

  /**
   * @param user A UserSignature class instance.
   * @param token A Token (com.zoho.api.authenticator.OAuthToken) class instance.
   * @return A Token class instance representing the user token details.
   */
  @Override
  public Token getToken(UserSignature user, Token token) {
    return tokenStore.get(user.getEmail());
  }

  /**
   * @param user A UserSignature class instance.
   * @param token A Token (com.zoho.api.authenticator.OAuthToken) class instance.
   */
  @Override
  public void saveToken(UserSignature user, Token token) {
    tokenStore.put(user.getEmail(), token);
  }

  /** @param token A Token (com.zoho.api.authenticator.OAuthToken) class instance. */
  @Override
  public void deleteToken(Token token) {
    tokenStore.entrySet().removeIf(entry -> (token.equals(entry.getValue())));
  }

  @Override
  public List<Token> getTokens() {
    return new ArrayList<>(tokenStore.values());
  }

  @Override
  public void deleteTokens() {
    tokenStore.clear();
  }
}
