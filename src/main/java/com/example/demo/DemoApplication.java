package com.example.demo;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@SpringBootApplication
@RestController
public class DemoApplication extends SpringBootServletInitializer {
	protected static final String BPMN_XSD = "你好/Main.xsd";
	protected static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(DemoApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private void testXercesNewSchemaMethod() throws UnsupportedEncodingException, URISyntaxException {
		LOGGER.info("Start testXercesNewSchemaMethod");
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL resource =  DemoApplication.class.getClassLoader().getResource(BPMN_XSD);
		String decodedURL = URLDecoder.decode(resource.toExternalForm(), StandardCharsets.UTF_8);
		LOGGER.info("Decoded UTF-8 URL = " +  decodedURL);
		LOGGER.info( "URL using toExternalForm= " + resource.toExternalForm());
		LOGGER.info( "URI.toASCIIString() = " + resource.toURI().toASCIIString());
		LOGGER.info(dashedLine());
		
		testResourceUrl(factory, resource);
		testUsingExplicitUtf8DecodedStringURL(factory, decodedURL);
		testUsingURItoASCIIString(factory, resource);
	}
	
	private void testResourceUrl(SchemaFactory factory,URL resource){
		try {
			LOGGER.info("Start Test 1 - factory.newSchema(resource)");
			LOGGER.info("It uses toExternalForm that causes errors on Tomcat but works with Spring");
			factory.newSchema(resource);
			LOGGER.info("End Test 1 - execution factory.newSchema(resource)");
		} catch (SAXException e) {
			e.printStackTrace();
		}
		LOGGER.info(dashedLine());
	}

	private void testUsingExplicitUtf8DecodedStringURL(SchemaFactory factory, String decodedURL){
		try {
			LOGGER.info("Start Test 2 - factory.newSchema(new StreamSource(decodedURL))");
			// To replicate the issue on Spring we have to decode the URL
			// Tomcat use a custom classLoader hat doesn't encode the URL so in Tomcat this is the same than TEST 1
			factory.newSchema(new StreamSource(decodedURL));
			LOGGER.info("End Test -2 - execution factory.newSchema(resource)");
		} catch (SAXException e) {
			e.printStackTrace();
		}
		LOGGER.info(dashedLine());
	}
	
	private void testUsingURItoASCIIString(SchemaFactory factory, URL resource){
		try {
			//Use toASCIIString will make it work on both Spring and Tomcat
			LOGGER.info("Start Test 3 - factory.newSchema(new StreamSource(resource.toURI().toASCIIString()))");
			factory.newSchema(new StreamSource(resource.toURI().toASCIIString()));
			LOGGER.info("End Test 3 - factory.newSchema(new StreamSource(resource.toURI().toASCIIString()))");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	

	@Component
	public class StartupHousekeeper {

		@EventListener(ContextRefreshedEvent.class)
		public void contextRefreshedEvent() {
			try {
				testXercesNewSchemaMethod();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String dashedLine() {
		int size = 100;
		StringBuilder sb = new StringBuilder();
		sb.append("-".repeat(size));
		sb.append(System.lineSeparator());
		return sb.toString();
	}
}
