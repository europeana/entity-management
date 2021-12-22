package eu.europeana.entitymanagement.common.config;

public class GeneralUtils {

	
  //classFullName can be e.g. "class java.lang.String"
  public static String getSimpleClassName(String classFullName) {
    String classTypeFullName = classFullName.substring(classFullName.lastIndexOf(" ") + 1);
    return classTypeFullName.substring(classTypeFullName.lastIndexOf(".") + 1);
  }
}
