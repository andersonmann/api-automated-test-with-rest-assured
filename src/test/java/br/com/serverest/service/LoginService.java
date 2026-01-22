package br.com.serverest.service;

import br.com.serverest.model.Login;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class LoginService {
    
    private static final String LOGIN_ENDPOINT = "/login";
    
    public Response realizarLogin(Login login) {
        return given()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post(LOGIN_ENDPOINT);
    }
    
    public Response realizarLogin(String email, String password) {
        Login login = Login.builder()
                .email(email)
                .password(password)
                .build();
        
        return realizarLogin(login);
    }
}