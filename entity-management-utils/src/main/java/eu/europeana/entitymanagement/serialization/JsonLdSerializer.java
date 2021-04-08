package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    ObjectMapper mapper;
    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public JsonLdSerializer() {
	mapper = new ObjectMapper();
	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
	mapper.setDateFormat(df);
    }
    
    
    public String serialize(EntityRecord record, String profile) throws EntityManagementRuntimeException {
	return serialize(record, EntityProfile.valueOf(profile));
    }

    public String serialize(EntityRecord record, EntityProfile profile) throws EntityManagementRuntimeException {
	String res = null;
	switch (profile) {
	case internal:
	    res = serializeInternal(record);
	    break;
	case external:
	    res = serializeExternal(record);
	    break;
	default:
	    throw new EntityManagementRuntimeException("Serialization not supported for profile:" + profile);
	}
	return res;
    }


    private String serializeExternal(EntityRecord record) throws EntityManagementRuntimeException {
	try {
	    return mapper.writeValueAsString(record.getEntity());
	} catch (JsonProcessingException e) {
	    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
	}
    }

    private String serializeInternal(EntityRecord record) throws EntityManagementRuntimeException {
		try {
			Entity recordEntity = record.getEntity();
			List<EntityProxy> recordProxies = record.getProxies();
			ObjectNode entityNode = mapper.valueToTree(recordEntity);

			ArrayNode proxyNode = mapper.createArrayNode();


			for(EntityProxy proxy: recordProxies){
				Entity proxyEntity = proxy.getEntity();
				ObjectNode proxyEntityNode = mapper.valueToTree(proxyEntity);

				// Entity @context shouldn't appear in proxy metadata
				proxyEntityNode.remove(WebEntityFields.CONTEXT);
				// Entity ID shouldn't overwrite proxyId
				proxyEntityNode.remove(WebEntityFields.ID);

				ObjectNode embeddedProxyNode = mapper.valueToTree(proxy);
				JsonNode mergedNode = JsonUtils.mergeProxyAndEntity(mapper, embeddedProxyNode, proxyEntityNode);
				proxyNode.add(mergedNode.deepCopy());
			}

			JsonNode combinedEntityAndProxies = JsonUtils
					.combineNestedNode(mapper, entityNode, proxyNode, "proxies");

			//TODO: clarify WebResource fields (not currently in spec)
		return mapper.writeValueAsString(combinedEntityAndProxies);
			
		} catch (JsonProcessingException e) {
		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
    }

}