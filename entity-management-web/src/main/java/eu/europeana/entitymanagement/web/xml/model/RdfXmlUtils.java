package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

//import org.apache.commons.lang3.StringEscapeUtils;

import eu.europeana.corelib.utils.EuropeanaUriUtils;

public class RdfXmlUtils {

    public static List<RdfResource> convertToRdfResource(String[] elements) {
	if (elements == null)
	    return null;
	List<RdfResource> res = new ArrayList<>();
	for (int index = 0; index < elements.length; index++) {
	    res.add(new RdfResource(elements[index]));
	}
	return res;
    }

    public static XmlMultilingualString createMultilingualString(String language, String entryValue) {
	return new XmlMultilingualString(StringEscapeUtils.escapeXml11(entryValue), language);
    }

    public static List<XmlMultilingualString> convertToXmlMultilingualString(Map<String, List<String>> values) {
	if (values == null)
	    return null;
	List<XmlMultilingualString> res = new ArrayList<>();
	for (String language : values.keySet()) {
	    List<String> entryValues = values.get(language);
	    for (String entryValue : entryValues) {
		res.add(createMultilingualString(language, entryValue));
	    }
	}
	return res;
    }

    public static List<Object> convertToXmlMultilingualStringOrRdfResource(Map<String, List<String>> values) {
	if (values == null)
	    return null;
	List<Object> res = new ArrayList<>();
	for (String language : values.keySet()) {
	    List<String> entryValues = values.get(language);
	    for (String entryValue : entryValues) {
		if (EuropeanaUriUtils.isUri(entryValue))
		    res.add(new RdfResource(entryValue));
		else
		    res.add(createMultilingualString(language, entryValue));
	    }
	}
	return res;
    }

    public static List<XmlMultilingualString> convertMapToXmlMultilingualString(Map<String, String> values) {
	if (values == null) {
	    return null;
	}
	List<XmlMultilingualString> res = new ArrayList<>();
	for (String language : values.keySet()) {
	    res.add(createMultilingualString(language, values.get(language)));
	}
	return res;
    }

    public static Map<String, String> toLanguageMap(List<XmlMultilingualString> multilingualStrings) {
	if (multilingualStrings == null) {
	    return null;
	}

	Map<String, String> res = new HashMap<String, String>(multilingualStrings.size());
	for (XmlMultilingualString xmlMultilingualString : multilingualStrings) {
	    res.putIfAbsent(xmlMultilingualString.getLanguage(), xmlMultilingualString.getValue());
	}
	return res;
    }

    public static Map<String, List<String>> toLanguageMapList(List<XmlMultilingualString> multilingualStrings) {
	if (multilingualStrings == null) {
	    return null;
	}

	Map<String, List<String>> res = new HashMap<String, List<String>>(multilingualStrings.size());
	for (XmlMultilingualString xmlMultilingualString : multilingualStrings) {
	    if (res.containsKey(xmlMultilingualString.getLanguage())) {
		res.get(xmlMultilingualString.getLanguage()).add(xmlMultilingualString.getValue());
	    } else {
		List<String> values = new ArrayList<String>();
		values.add(xmlMultilingualString.getValue());
		res.put(xmlMultilingualString.getLanguage(), values);
	    }
	}
	return res;
    }

    public static String[] toStringArray(List<RdfResource> resources) {
	if (resources == null) {
	    return null;
	}

	String[] res = new String[resources.size()];
	int i = 0;
	for (RdfResource rdfResource : resources) {
	    res[i++] = rdfResource.getValue();
	}

	return res;
    }
}
