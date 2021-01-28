package eu.europeana.entitymanagement.definitions.model;

import java.util.Date;
import java.util.List;

import dev.morphia.annotations.Embedded;

@Embedded
public interface Aggregation {

    void setAggregates(List<String> aggregates);

    List<String> getAggregates();

    void setScore(int score);

    int getScore();

    void setRecordCount(int recordCount);

    int getRecordCount();

    void setPageRank(double pageRank);

    double getPageRank();

    void setModified(Date modified);

    Date getModified();

    void setCreated(Date created);

    Date getCreated();

    void setSource(String source);

    String getSource();

    void setRights(String rights);

    String getRights();

    String getType();

    void setId(String id);

    String getId();

}
