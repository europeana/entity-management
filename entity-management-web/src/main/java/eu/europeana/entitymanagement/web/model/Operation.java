package eu.europeana.entitymanagement.web.model;

import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Date;

public class Operation implements Comparable<Operation> {

  private String action;
  //  private String zohoId;
  private String zohoEuropeanaId;

  private Record zohoRecord;
  private Date modified;
  EntityRecord entityRecord;

  public Operation(
      String zohoEuropeanaId, String action, Record zohoRecord, EntityRecord entityRecord) {
    this.zohoEuropeanaId = zohoEuropeanaId;
    this.action = action;
    this.zohoRecord = zohoRecord;
    if (zohoRecord != null) {
      modified = new Date(zohoRecord.getModifiedTime().toInstant().toEpochMilli());
    } else {
      modified = new Date();
    }
    this.entityRecord = entityRecord;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getZohoEuropeanaId() {
    return zohoEuropeanaId;
  }

  public void setZohoEuropeanaId(String zohoEuropeanaId) {
    this.zohoEuropeanaId = zohoEuropeanaId;
  }

  @Override
  public int hashCode() {
    if (getZohoEuropeanaId() == null) {
      return -1;
    }
    return getZohoEuropeanaId().hashCode();
  }

  @Override
  public String toString() {
    return String.format("organizationId: %s, action: %s", getZohoEuropeanaId(), getAction());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (this.getClass() != obj.getClass()) {
      return false;
    }

    Operation op2 = (Operation) obj;
    return getZohoEuropeanaId().equals(op2.getZohoEuropeanaId())
        && getAction().equals(op2.getAction());
  }

  @Override
  public int compareTo(Operation o) {
    // used to order operations in cronological order
    int ret = getModified().compareTo(o.getModified());
    
    //should be of same type
    if(ret == 0) {
      ret = getAction().compareTo(o.getAction());
    }
    
    // create operations don't have an operation id
    if (ret == 0 && getZohoEuropeanaId() != null) {
      ret = getZohoEuropeanaId().compareTo(o.getZohoEuropeanaId());
    }
    
    //permanent delete operations don't have a zoho record
    if(ret == 0 && getZohoRecord() != null) {
      if(o.getZohoRecord() == null) {
        return 1;
      } else {
        getZohoRecord().getId().compareTo(o.getZohoRecord().getId());
      } 
    }
      
    return ret;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public Record getZohoRecord() {
    return zohoRecord;
  }

  public EntityRecord getEntityRecord() {
    return entityRecord;
  }
}
