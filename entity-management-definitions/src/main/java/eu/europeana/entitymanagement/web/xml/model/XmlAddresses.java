package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_VCARD;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ADDRESS;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_HAS_ADDRESS)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAddresses {

  public XmlAddresses() {
    // default no-arg constructor
  }

  public XmlAddresses(List<XmlAddressImpl> vcardAddressesList) {
    if (vcardAddressesList != null) {
      this.vcardAddressesList = new ArrayList<XmlAddressImpl>(vcardAddressesList);
    }
  }

  @XmlElement(namespace = NAMESPACE_VCARD, name = XML_ADDRESS)
  private List<XmlAddressImpl> vcardAddressesList;

  public List<XmlAddressImpl> getVcardAddressesList() {
    return vcardAddressesList == null ? null : new ArrayList<>(vcardAddressesList);
  }
}
