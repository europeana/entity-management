package eu.europeana.entitymanagement.web.model;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.zoho.crm.api.record.Record;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public class Operation implements Comparable<Operation> {

  private String action;
  //  private String zohoId;
  private String zohoEuropeanaId;

  private Record zohoRecord;
  private Date modified;
  EntityRecord entityRecord;

  public Operation(
      String zohoEuropeanaId, String action, Record zohoRecord, EntityRecord entityRecord) {
    
    if(! StringUtils.isBlank(zohoEuropeanaId)) {
      //avoid using empty strings which might have been manually entered in Zoho
      this.zohoEuropeanaId = zohoEuropeanaId;  
    }
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
    return getZohoRecord().getId().hashCode();
  }

  @Override
  public String toString() {
    return String.format("zohoId: %s, action: %s, zohoEuropeanaId: %", getZohoRecord().getId(), getAction(), getZohoEuropeanaId());
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
    //permanent delete must have zohoOrganizationID
    if (Operations.PERMANENT_DELETE.equals( this.getAction())) {
      return this.getZohoEuropeanaId().equals(op2.getZohoEuropeanaId());
    }
    
    return this.getZohoRecord().getId().equals(op2.getZohoRecord().getId())
        && this.getAction().equals(op2.getAction());
  }

  @Override
  public int compareTo(Operation o) {
    // used to order operations in cronological order
    int ret = getModified().compareTo(o.getModified());
    
    //should be of same type
    if(ret == 0) {
      ret = getAction().compareTo(o.getAction());
    }
    
    //permanent delete operations don't have a zoho record
    if(ret == 0 && getZohoRecord() != null) {
      if(o.getZohoRecord() == null) {
        return 1;
      } else {
        ret = getZohoRecord().getId().compareTo(o.getZohoRecord().getId());
      } 
    }
    
    //Permanent deletes must have zoho organization id 
    if (Operations.PERMANENT_DELETE.equals( this.getAction())) {
        ret = this.getZohoEuropeanaId().compareTo(o.getZohoEuropeanaId());
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
