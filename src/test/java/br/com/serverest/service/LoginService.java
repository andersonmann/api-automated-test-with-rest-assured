package br.com.serverest.service;

import br.com.serverest.model.Login;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Object Pattern - Login
 * Encapsula todas as operações relacionadas ao endpoint /login
 */
public class LoginService extends BaseService {
    
    private static final String LOGIN_ENDPOINT = "/login";
    
    @Override
    protected String getBasePath() {
        return LOGIN_ENDPOINT;
    }
    
    /**
     * Realiza login com objeto Login
     */
    @Step("Realizar login com email: {login.email}")
    public Response realizarLogin(Login login) {
        return doPost(login);
    }
    
    /**
     * Realiza login com email e senha separados
     */
    @Step("Realizar login - Email: {email}")
    public Response realizarLogin(String email, String password) {
        Login login = Login.builder()
                .email(email)
                .password(password)
                .build();
        return realizarLogin(login);
    }
    
    /**
     * Realiza login e retorna o token
     */
    @Step("Realizar login e obter token")
    public String realizarLoginEObterToken(Login login) {
        Response response = realizarLogin(login);
        return extractToken(response);
    }
    
    /**
     * Realiza login e retorna o token (com email e senha)
     */
    @Step("Realizar login e obter token - Email: {email}")
    public String realizarLoginEObterToken(String email, String password) {
        Response response = realizarLogin(email, password);
        return extractToken(response);
    }
    
    /**
     * Realiza login com headers customizados
     */
    @Step("Realizar login com headers customizados")
    public Response realizarLoginComHeaders(Login login, Map<String, String> headers) {
        return doPostWithHeaders(login, headers);
    }
    
    /**
     * Realiza login com campos vazios
     */
    @Step("Realizar login com campos vazios")
    public Response realizarLoginComCamposVazios() {
        Login login = Login.builder()
                .email("")
                .password("")
                .build();
        return realizarLogin(login);
    }
    
    /**
     * Realiza login sem email
     */
    @Step("Realizar login sem email")
    public Response realizarLoginSemEmail(String password) {
        Login login = Login.builder()
                .password(password)
                .build();
        return realizarLogin(login);
    }
    
    /**
     * Realiza login sem senha
     */
    @Step("Realizar login sem senha")
    public Response realizarLoginSemSenha(String email) {
        Login login = Login.builder()
                .email(email)
                .build();
        return realizarLogin(login);
    }
    
    /**
     * Verifica se login foi bem-sucedido
     */
    @Step("Verificar se login foi bem-sucedido")
    public boolean loginFoiSucesso(Response response) {
        return response.getStatusCode() == 200 && 
               extractToken(response) != null;
    }
    
    /**
     * Extrai apenas o token Bearer (sem "Bearer ")
     */
    @Step("Extrair token Bearer limpo")
    public String extrairTokenLimpo(Response response) {
        String fullToken = extractToken(response);
        if (fullToken != null && fullToken.startsWith("Bearer ")) {
            return fullToken.substring(7);
        }
        return fullToken;
    }
}