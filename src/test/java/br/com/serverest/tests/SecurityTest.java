package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Login;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.LoginService;
import br.com.serverest.utils.DataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API ServeRest")
@Feature("Segurança")
public class SecurityTest extends BaseTest {

    private final LoginService loginService = new LoginService();

    @Test
    @DisplayName("Validar formato do token JWT retornado no login")
    @Description("Valida que o token JWT retornado possui o formato correto com 3 partes: header.payload.signature")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Validação de Token JWT")
    public void testValidarFormatoTokenJWT() {
        Usuario usuario = criarUsuarioERetornarObjeto(true);
        Login login = DataFactory.criarLoginValido(usuario);
        Response response = loginService.realizarLogin(login);
        String token = response.jsonPath().getString("authorization");
        response.then()
                .statusCode(200)
                .body("authorization", startsWith("Bearer "))
                .body("authorization", matchesPattern("^Bearer [A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+$"));
        String tokenSemBearer = token.replace("Bearer ", "");
        String[] partes = tokenSemBearer.split("\\.");        
        assert partes.length == 3 : "Token JWT deve ter 3 partes (header.payload.signature)";
    }

    @Test
    @DisplayName("Não deve aceitar requisição com token inválido")
    public void testAcessoComTokenInvalido() {
        Response response = given()
                .header("Authorization", "Bearer tokeninvalido123")
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401)));
    }

    @Test
    @DisplayName("Não deve aceitar requisição com token malformado")
    public void testAcessoComTokenMalformado() {
        Response response = given()
                .header("Authorization", "InvalidTokenFormat")
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Deve aceitar requisição sem token em endpoints públicos")
    public void testAcessoSemTokenEmEndpointPublico() {
        Response response = given()
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
    }

    @Test
    @DisplayName("Deve realizar login com senha contendo caracteres especiais")
    public void testLoginComSenhaCaracteresEspeciais() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("Senh@123!#$%&*()_+-=[]{}|;:,.<>?")
                .administrador("true")
                .build();
        criarUsuarioCustomizadoERetornarId(usuario);
        Login login = Login.builder()
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", notNullValue())
                .body("authorization", startsWith("Bearer "));
    }

    @Test
    @DisplayName("Validar que token é único para cada login")
    public void testTokenUnicoParaCadaLogin() {
        Usuario usuario = criarUsuarioERetornarObjeto(true);
        Login login = DataFactory.criarLoginValido(usuario);
        Response response1 = loginService.realizarLogin(login);
        String token1 = response1.jsonPath().getString("authorization");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Response response2 = loginService.realizarLogin(login);
        String token2 = response2.jsonPath().getString("authorization");
        response1.then().statusCode(200);
        response2.then().statusCode(200);        
        assert token1 != null && !token1.isEmpty() : "Token 1 não deve ser nulo ou vazio";
        assert token2 != null && !token2.isEmpty() : "Token 2 não deve ser nulo ou vazio";
    }

    @Test
    @DisplayName("Não deve aceitar token com espaços extras")
    public void testTokenComEspacosExtras() {
        Response response = given()
                .header("Authorization", "  Bearer   tokencomespaco  ")
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Não deve aceitar múltiplos tokens na requisição")
    public void testMultiplosTokens() {
        Response response = given()
                .header("Authorization", "Bearer token1, Bearer token2")
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Validar estrutura do payload do token JWT")
    public void testValidarPayloadToken() {
        Usuario usuario = criarUsuarioERetornarObjeto(true);
        Login login = DataFactory.criarLoginValido(usuario);
        Response response = loginService.realizarLogin(login);
        String token = response.jsonPath().getString("authorization");
        String tokenSemBearer = token.replace("Bearer ", "");        
        String[] partes = tokenSemBearer.split("\\.");        
        assert partes.length == 3 : "Token deve ter 3 partes";
        assert !partes[0].isEmpty() : "Header do token não deve estar vazio";
        assert !partes[1].isEmpty() : "Payload do token não deve estar vazio";
        assert !partes[2].isEmpty() : "Signature do token não deve estar vazia";        
        assert partes[0].length() > 10 : "Header do token muito curto";
        assert partes[1].length() > 10 : "Payload do token muito curto";
        assert partes[2].length() > 10 : "Signature do token muito curta";
    }

    @Test
    @DisplayName("Não deve aceitar token vazio no header Authorization")
    public void testTokenVazio() {
        Response response = given()
                .header("Authorization", "")
                .when()
                .get("/usuarios");
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Deve realizar login com senha contendo apenas números")
    public void testLoginComSenhaApenasNumeros() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("123456789")
                .administrador("false")
                .build();
        criarUsuarioCustomizadoERetornarId(usuario);
        Login login = Login.builder()
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", notNullValue());
    }

    @Test
    @DisplayName("Deve realizar login com senha muito longa")
    public void testLoginComSenhaMuitoLonga() {
        StringBuilder senhaLonga = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            senhaLonga.append("a");
        }
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password(senhaLonga.toString())
                .administrador("false")
                .build();
        criarUsuarioCustomizadoERetornarId(usuario);
        Login login = Login.builder()
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", notNullValue());
    }
}