package eu.europeana.entitymanagement.utils;

import java.util.Date;
import dev.morphia.annotations.PrePersist;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;

/** Watches for Database operations on the Vocabulary collection */
public class VocabularyWatcher {

  /**
   * Invoked by Morphia when creating / updating a Vocabulary. Sets the created/modified time.
   * @param record Vocabulary
   */
  @PrePersist
  void prePersist(Vocabulary vocab) {
    Date now = new Date();
    if (vocab.getCreated() == null) {
      vocab.setCreated(now);
    }
    vocab.setModified(now);
  }
}
