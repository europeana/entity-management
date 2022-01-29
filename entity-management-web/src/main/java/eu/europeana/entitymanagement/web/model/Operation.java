package eu.europeana.entitymanagement.web.model;

import java.util.Date;
import java.util.Optional;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;


public class Operation implements Comparable<Operation>{

  
  private String action;
//  private String zohoId;
  private String organizationId;
  
  private Record zohoRecord;
  private Date modified;
  Optional<EntityRecord> entityRecord;

  public Operation(String organizationId, String action, Record zohoRecord, Optional<EntityRecord> entityRecord) {
    this.organizationId = organizationId;
    this.action = action;
    this.zohoRecord = zohoRecord;
    if(zohoRecord != null) {
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
  
  
  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  @Override
  public int hashCode() {
    if(getOrganizationId() == null) {
      return -1;
    }
    return getOrganizationId().hashCode();
  }
  
  @Override
  public String toString() {
    return String.format("organizationId: {}, action:{} ", getOrganizationId(), getAction());
  }
 
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Operation)
      return false;

    Operation op2 = (Operation) obj;
    return getOrganizationId().equals(op2.getOrganizationId()) && getAction().equals(op2.getAction());
  }

  @Override
  public int compareTo(Operation o) {
    //used to order operations in cronological order
    int ret = getModified().compareTo(o.getModified());
    if (ret == 0)
      ret = getOrganizationId().compareTo(o.getOrganizationId());
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

  public Optional<EntityRecord> getEntityRecord() {
    return entityRecord;
  }

}
