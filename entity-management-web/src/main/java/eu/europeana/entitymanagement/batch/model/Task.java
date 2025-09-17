package eu.europeana.entitymanagement.batch.model;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;

public enum Task {

    DEREFERENCE        (BEAN_ENTITY_DEREFERENCE_PROCESSOR),
    CONSOLIDATION      (BEAN_ENTITY_CONSOLIDATION_PROCESSOR),
    METRICS            (BEAN_ENTITY_METRICS_PROCESSOR),
    VALIDATION         (BEAN_ENTITY_VERIFICATION_LOGGER),
    DB_UPDATE          (BEAN_ENTITY_RECORD_DBINSERTION_WRITER),
    SOLR_INSERTION     (BEAN_ENTITY_SOLR_INSERTION_WRITER),
    RECORD_DEPRECATION (ENTITY_RECORD_DB_DEPRECATION_WRITER),
    RECORD_REMOVAL     (ENTITY_RECORD_DB_REMOVAL_WRITER),
    SOLR_REMOVAL       (ENTITY_SOLR_REMOVAL_WRITER);

    final String beanName;

    Task(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
