package br.com.serverest.service;

import br.com.serverest.model.Usuario;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UsuarioService {
    
    private static final String USUARIOS_ENDPOINT = "/usuarios";
    
    public Response listarUsuarios() {
        return given()
                .when()
                .get(USUARIOS_ENDPOINT);
    }
    
    public Response listarUsuarios(String queryParam, String value) {
        return given()
                .queryParam(queryParam, value)
                .when()
                .get(USUARIOS_ENDPOINT);
    }
    
    public Response cadastrarUsuario(Usuario usuario) {
        return given()
                .contentType(ContentType.JSON)
                .body(usuario)
                .when()
                .post(USUARIOS_ENDPOINT);
    }
    
    public Response buscarUsuarioPorId(String id) {
        return given()
                .pathParam("_id", id)
                .when()
                .get(USUARIOS_ENDPOINT + "/{_id}");
    }
    
    public Response excluirUsuario(String id) {
        return given()
                .pathParam("_id", id)
                .when()
                .delete(USUARIOS_ENDPOINT + "/{_id}");
    }
    
    public Response editarUsuario(String id, Usuario usuario) {
        return given()
                .contentType(ContentType.JSON)
                .pathParam("_id", id)
                .body(usuario)
                .when()
                .put(USUARIOS_ENDPOINT + "/{_id}");
    }
}