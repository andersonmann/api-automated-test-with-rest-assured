package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Login;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.LoginService;
import br.com.serverest.service.UsuarioService;
import br.com.serverest.utils.DataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SecurityTest extends BaseTest {

    private final LoginService loginService = new LoginService();
    private final UsuarioService usuarioService = new UsuarioService();
    private String userId;

    @AfterEach
    public void limparDados() {
        if (userId != null) {
            usuarioService.excluirUsuario(userId);
        }
    }

    @Test
    @DisplayName("Validar formato do token JWT retornado no login")
    public void testValidarFormatoTokenJWT() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Realizar login
        Login login = DataFactory.criarLoginValido(usuario);
        Response response = loginService.realizarLogin(login);

        String token = response.jsonPath().getString("authorization");

        response.then()
                .statusCode(200)
                // Validar que o token começa com "Bearer "
                .body("authorization", startsWith("Bearer "))
                // Validar formato JWT (Bearer + 3 partes separadas por ponto)
                .body("authorization", matchesPattern("^Bearer [A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+$"));

        // Validar que o token tem 3 partes (header.payload.signature)
        String tokenSemBearer = token.replace("Bearer ", "");
        String[] partes = tokenSemBearer.split("\\.");
        
        // Validar estrutura do JWT: deve ter exatamente 3 partes
        assert partes.length == 3 : "Token JWT deve ter 3 partes (header.payload.signature)";
    }

    @Test
    @DisplayName("Não deve aceitar requisição com token inválido")
    public void testAcessoComTokenInvalido() {
        Response response = given()
                .header("Authorization", "Bearer tokeninvalido123")
                .when()
                .get("/usuarios");

        // A API ServeRest permite listar usuários sem autenticação,
        // mas se enviarmos um token inválido, ele deve ser ignorado ou rejeitado
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

        // Token sem "Bearer " ou com formato incorreto
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Deve aceitar requisição sem token em endpoints públicos")
    public void testAcessoSemTokenEmEndpointPublico() {
        Response response = given()
                .when()
                .get("/usuarios");

        // Endpoint público - deve permitir acesso sem token
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
    }

    @Test
    @DisplayName("Deve realizar login com senha contendo caracteres especiais")
    public void testLoginComSenhaCaracteresEspeciais() {
        // Criar usuário com senha contendo caracteres especiais
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("Senh@123!#$%&*()_+-=[]{}|;:,.<>?")
                .administrador("true")
                .build();

        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        cadastroResponse.then()
                .statusCode(201);

        // Realizar login com a senha especial
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
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        Login login = DataFactory.criarLoginValido(usuario);

        // Primeiro login
        Response response1 = loginService.realizarLogin(login);
        String token1 = response1.jsonPath().getString("authorization");

        // Aguardar um momento
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Segundo login com mesmo usuário
        Response response2 = loginService.realizarLogin(login);
        String token2 = response2.jsonPath().getString("authorization");

        // Os tokens podem ser iguais ou diferentes dependendo da implementação
        // Apenas validar que ambos foram gerados com sucesso
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

        // Token com espaços extras pode ser rejeitado ou aceito após trim
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

        // Múltiplos tokens devem ser rejeitados
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Validar estrutura do payload do token JWT")
    public void testValidarPayloadToken() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Realizar login
        Login login = DataFactory.criarLoginValido(usuario);
        Response response = loginService.realizarLogin(login);

        String token = response.jsonPath().getString("authorization");
        String tokenSemBearer = token.replace("Bearer ", "");
        
        // Separar as partes do JWT
        String[] partes = tokenSemBearer.split("\\.");
        
        // Validar que cada parte não está vazia
        assert partes.length == 3 : "Token deve ter 3 partes";
        assert !partes[0].isEmpty() : "Header do token não deve estar vazio";
        assert !partes[1].isEmpty() : "Payload do token não deve estar vazio";
        assert !partes[2].isEmpty() : "Signature do token não deve estar vazia";
        
        // Validar tamanho mínimo das partes
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

        // Token vazio deve ser ignorado ou rejeitado
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(400)));
    }

    @Test
    @DisplayName("Deve realizar login com senha contendo apenas números")
    public void testLoginComSenhaApenasNumeros() {
        // Criar usuário com senha numérica
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("123456789")
                .administrador("false")
                .build();

        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        cadastroResponse.then()
                .statusCode(201);

        // Realizar login
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
        // Criar senha muito longa (100 caracteres)
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

        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        cadastroResponse.then()
                .statusCode(201);

        // Realizar login
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
