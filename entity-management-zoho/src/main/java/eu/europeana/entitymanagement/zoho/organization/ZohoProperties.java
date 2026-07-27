package eu.europeana.entitymanagement.zoho.organization;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for loading and handling Zoho properties from an external YAML file.
 * This class maps the configuration data into a structured Java object for further processing.
 *
 * The configuration data is primarily focused on Zoho modules and their associated fields.
 * Properties are loaded from the file `zoho-fields.yaml` located in the classpath.
 *
 * It contains a nested static class `ModuleConfig` that represents the structure
 * for module-specific configurations, including a list of fields corresponding to each module.
 */
@Configuration
@PropertySource(value = "classpath:zoho-fields.yaml", ignoreResourceNotFound = true)
public class ZohoProperties {

    private Map<String, ModuleConfig> modules = new HashMap<>();

    public List<String> getFieldsByModule(String moduleName) {
        return modules.get(moduleName).getFields();
    }

    public Map<String, ModuleConfig> getModules() {
        return modules;
    }

    public void setModules(Map<String, ModuleConfig> modules) {
        this.modules = modules;
    }


    public static class ModuleConfig {
        private List<String> fields = new ArrayList<>();

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }
    }
}