package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

//import org.apache.commons.lang3.StringEscapeUtils;

import eu.europeana.corelib.utils.EuropeanaUriUtils;

public class RdfXmlUtils {

    public static List<LabelledResource> convertToRdfResource(String[] elements) {
	if (elements == null)
	    return null;
	List<LabelledResource> res = new ArrayList<>();
	for (int index = 0; index < elements.length; index++) {
	    res.add(new LabelledResource(elements[index]));
	}
	return res;
    }

	public static List<LabelledResource> convertToXmlMultilingualString(Map<String, List<String>> values) {
	if (values == null)
	    return null;
	List<LabelledResource> res = new ArrayList<>();
	for (String language : values.keySet()) {
	    List<String> entryValues = values.get(language);
	    for (String entryValue : entryValues) {
				res.add(new LabelledResource(StringEscapeUtils.escapeXml11(entryValue), language));
	    }
	}
	return res;
    }

    public static List<LabelledResource> convertToXmlMultilingualStringOrRdfResource(Map<String, List<String>> values) {
	if (values == null)
	    return null;
	List<LabelledResource> res = new ArrayList<>();
	for (String language : values.keySet()) {
	    List<String> entryValues = values.get(language);
	    for (String entryValue : entryValues) {
		if (EuropeanaUriUtils.isUri(entryValue))
		    res.add(new LabelledResource(entryValue));
		else
			res.add(new LabelledResource(StringEscapeUtils.escapeXml11(entryValue), language));
	    }
	}
	return res;
    }

    public static List<LabelledResource> convertMapToXmlMultilingualString(Map<String, String> values) {
	if (values == null) {
	    return null;
	}
	List<LabelledResource> res = new ArrayList<>();
	for (String language : values.keySet()) {
		res.add(
				new LabelledResource(StringEscapeUtils.escapeXml11(values.get(language)), language));
	}
	return res;
    }

    public static Map<String, String> toLanguageMap(List<LabelledResource> multilingualStrings) {
	if (multilingualStrings == null) {
	    return null;
	}

	Map<String, String> res = new HashMap<String, String>(multilingualStrings.size());
	for (LabelledResource xmlMultilingualString : multilingualStrings) {
	    res.putIfAbsent(xmlMultilingualString.getLang(), xmlMultilingualString.getValue());
	}
	return res;
    }

    public static Map<String, List<String>> toLanguageMapList(List<LabelledResource> multilingualStrings) {
	if (multilingualStrings == null) {
	    return null;
	}

	Map<String, List<String>> res = new HashMap<String, List<String>>(multilingualStrings.size());
	for (LabelledResource xmlMultilingualString : multilingualStrings) {
	    if (res.containsKey(xmlMultilingualString.getLang())) {
		res.get(xmlMultilingualString.getLang()).add(xmlMultilingualString.getValue());
	    } else {
		List<String> values = new ArrayList<String>();
		values.add(xmlMultilingualString.getValue());
		res.put(xmlMultilingualString.getLang(), values);
	    }
	}
	return res;
    }

    public static String[] toStringArray(List<LabelledResource> resources) {
	if (resources == null) {
	    return null;
	}

	String[] res = new String[resources.size()];
	int i = 0;
	for (LabelledResource labelledResource : resources) {
	    res[i++] = labelledResource.getValue();
	}

	return res;
    }
}
