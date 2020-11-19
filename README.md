# Europeana Entity Management API

Spring-Boot2 web application for curating and harvesting entity data from external sources.

## Prerequisites
 * Java 11
 * Maven<sup>*</sup> 
 * [Europeana parent pom](https://github.com/europeana/europeana-parent-pom)
 
 <sup>* A Maven installation is recommended, but you could use the accompanying `mvnw` (Linux, Mac OS) or `mvnw.cmd` (Windows) 
 files instead.
 
## Run

The application has a Tomcat web server that is embedded in Spring-Boot.

Either select the `EntityManagementApp` class in your IDE and 'run' it

or 

go to the application root where the pom.xml is located and excute  
`./mvnw spring-boot:run` (Linux, Mac OS) or `mvnw.cmd spring-boot:run` (Windows)


## License

Licensed under the EUPL 1.2. For full details, see [LICENSE.md](LICENSE.md).
