package br.com.serverest.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;
    protected static Properties config;

    @BeforeAll
    public static void setup() {
        loadConfig();
        configureRestAssured();
        setupRequestSpecification();
        setupResponseSpecification();
    }

    private static void loadConfig() {
        config = new Properties();
        try (InputStream input = BaseTest.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Arquivo config.properties não encontrado");
            }
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar configurações", e);
        }
    }

    private static void configureRestAssured() {
        RestAssured.baseURI = config.getProperty("base.uri");
        RestAssured.basePath = config.getProperty("base.path");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Configurar ObjectMapper para usar Jackson
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2));
    }

    private static void setupRequestSpecification() {
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.setAccept(ContentType.JSON);
        
        if (Boolean.parseBoolean(config.getProperty("enable.request.logging"))) {
            reqBuilder.log(LogDetail.ALL);
        }
        
        requestSpec = reqBuilder.build();
    }

    private static void setupResponseSpecification() {
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder();
        resBuilder.expectContentType(ContentType.JSON);
        
        if (Boolean.parseBoolean(config.getProperty("enable.response.logging"))) {
            resBuilder.log(LogDetail.ALL);
        }
        
        responseSpec = resBuilder.build();
    }

    protected static String getConfig(String key) {
        return config.getProperty(key);
    }
}