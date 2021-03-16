package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.definitions.model.impl.AddressImpl;

@Embedded
@JsonDeserialize(as = AddressImpl.class)
public interface Address {

    public void setVcardPostOfficeBox(String vcardPostOfficeBox);

    public String getVcardPostOfficeBox();

    public void setVcardCountryName(String vcardCountryName);

    public String getVcardCountryName();

    public void setVcardPostalCode(String vcardPostalCode);

    public String getVcardPostalCode();
 
    public void setVcardLocality(String vcardLocality);

    public String getVcardLocality();

    public void setVcardStreetAddress(String vcardStreetAddress);

    public String getVcardStreetAddress();

    public void setAbout(String about);

    public String getAbout();

    public String getVcardHasGeo();

    public void setVcardHasGeo(String hasGeo);

}
