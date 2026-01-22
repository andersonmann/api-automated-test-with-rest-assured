package br.com.serverest.service;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Service Object Pattern - Classe Base
 * Centraliza operações comuns para todos os services
 * Facilita manutenção e reutilização de código
 */
public abstract class BaseService {
    
    protected abstract String getBasePath();
    
    /**
     * Cria uma RequestSpecification básica com configurações padrão
     */
    protected RequestSpecification getRequestSpec() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
    
    /**
     * GET - Listar todos os recursos
     */
    @Step("Listar recursos")
    protected Response doGet() {
        return getRequestSpec()
                .when()
                .get(getBasePath());
    }
    
    /**
     * GET - Listar com query parameters
     */
    @Step("Listar com filtros")
    protected Response doGet(Map<String, ?> queryParams) {
        return getRequestSpec()
                .queryParams(queryParams)
                .when()
                .get(getBasePath());
    }
    
    /**
     * GET - Buscar por ID
     */
    @Step("Buscar recurso por ID: {id}")
    protected Response doGetById(String id) {
        return getRequestSpec()
                .pathParam("_id", id)
                .when()
                .get(getBasePath() + "/{_id}");
    }
    
    /**
     * POST - Criar recurso
     */
    @Step("Criar recurso")
    protected Response doPost(Object body) {
        return getRequestSpec()
                .body(body)
                .when()
                .post(getBasePath());
    }
    
    /**
     * PUT - Atualizar recurso
     */
    @Step("Atualizar recurso: {id}")
    protected Response doPut(String id, Object body) {
        return getRequestSpec()
                .pathParam("_id", id)
                .body(body)
                .when()
                .put(getBasePath() + "/{_id}");
    }
    
    /**
     * DELETE - Excluir recurso
     */
    @Step("Excluir recurso: {id}")
    protected Response doDelete(String id) {
        return getRequestSpec()
                .pathParam("_id", id)
                .when()
                .delete(getBasePath() + "/{_id}");
    }
    
    /**
     * POST - Com headers customizados
     */
    @Step("Criar recurso com headers customizados")
    protected Response doPostWithHeaders(Object body, Map<String, String> headers) {
        RequestSpecification spec = getRequestSpec();
        headers.forEach(spec::header);
        return spec.body(body)
                .when()
                .post(getBasePath());
    }
    
    /**
     * GET - Com headers customizados
     */
    @Step("Buscar com headers customizados")
    protected Response doGetWithHeaders(Map<String, String> headers) {
        RequestSpecification spec = getRequestSpec();
        headers.forEach(spec::header);
        return spec.when()
                .get(getBasePath());
    }
    
    /**
     * Extrai ID da resposta
     */
    protected String extractId(Response response) {
        return response.jsonPath().getString("_id");
    }
    
    /**
     * Extrai mensagem da resposta
     */
    protected String extractMessage(Response response) {
        return response.jsonPath().getString("message");
    }
    
    /**
     * Extrai token de autorização
     */
    protected String extractToken(Response response) {
        return response.jsonPath().getString("authorization");
    }
}
