package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import eu.europeana.entitymanagement.definitions.model.Organization;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      ABOUT,
      DEPICTION,
      IS_SHOWN_BY,
      PREF_LABEL,
      XML_ACRONYM,
      ALT_LABEL,
      HIDDEN_LABEL,
      XML_DESCRIPTION,
      XML_LOGO,
      XML_EUROPEANA_ROLE,
      XML_COUNTRY,
      XML_LANGUAGE,
      XML_HOMEPAGE,
      XML_PHONE,
      XML_HAS_ADDRESS,
      XML_AGGREGATED_VIA,
      XML_AGGREGATES_FROM,
      XML_IDENTIFIER,
      XML_SAME_AS,
      IS_AGGREGATED_BY
    })
/**
 * Class for XML serialization and deserialization of Organizations
 */
public class XmlOrganizationImpl extends XmlBaseOrganizationImpl {
  
  /**
   * Constructor from organization
   * @param organization pojo
   */
  public XmlOrganizationImpl(Organization organization) {
    super(organization);
  }
  
  /**
   * Default constructor
   */
  public XmlOrganizationImpl() {
    //default connstructor
  } 
}
