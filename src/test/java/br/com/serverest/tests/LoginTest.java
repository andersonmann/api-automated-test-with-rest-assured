package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Login;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.LoginService;
import br.com.serverest.service.UsuarioService;
import br.com.serverest.utils.DataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

public class LoginTest extends BaseTest {

    private final LoginService loginService = new LoginService();
    private final UsuarioService usuarioService = new UsuarioService();
    private String userId;
    private Usuario usuarioCriado;

    @BeforeEach
    public void criarUsuarioParaTeste() {
        usuarioCriado = DataFactory.criarUsuarioValido(true);
        Response response = usuarioService.cadastrarUsuario(usuarioCriado);
        userId = response.jsonPath().getString("_id");
    }

    @AfterEach
    public void limparDados() {
        if (userId != null) {
            usuarioService.excluirUsuario(userId);
        }
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    public void testLoginComSucesso() {
        Login login = DataFactory.criarLoginValido(usuarioCriado);
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", notNullValue())
                .body("authorization", startsWith("Bearer "));
    }

    @Test
    @DisplayName("Não deve realizar login com email inválido")
    public void testLoginComEmailInvalido() {
        Login login = Login.builder()
                .email("email.invalido@teste.com")
                .password(usuarioCriado.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(401)
                .body("message", equalTo("Email e/ou senha inválidos"));
    }

    @Test
    @DisplayName("Não deve realizar login com senha inválida")
    public void testLoginComSenhaInvalida() {
        Login login = Login.builder()
                .email(usuarioCriado.getEmail())
                .password("senhaerrada123")
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(401)
                .body("message", equalTo("Email e/ou senha inválidos"));
    }

    @Test
    @DisplayName("Não deve realizar login sem informar email")
    public void testLoginSemEmail() {
        Login login = Login.builder()
                .password(usuarioCriado.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email é obrigatório"));
    }

    @Test
    @DisplayName("Não deve realizar login sem informar senha")
    public void testLoginSemSenha() {
        Login login = Login.builder()
                .email(usuarioCriado.getEmail())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("password", equalTo("password é obrigatório"));
    }

    @Test
    @DisplayName("Não deve realizar login com email vazio")
    public void testLoginComEmailVazio() {
        Login login = Login.builder()
                .email("")
                .password(usuarioCriado.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email não pode ficar em branco"));
    }

    @Test
    @DisplayName("Não deve realizar login com senha vazia")
    public void testLoginComSenhaVazia() {
        Login login = Login.builder()
                .email(usuarioCriado.getEmail())
                .password("")
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @DisplayName("Não deve realizar login com ambos os campos vazios")
    public void testLoginComCamposVazios() {
        Login login = Login.builder()
                .email("")
                .password("")
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email não pode ficar em branco"))
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @DisplayName("Não deve realizar login com email em formato inválido")
    public void testLoginComEmailFormatoInvalido() {
        Login login = Login.builder()
                .email("emailinvalido")
                .password(usuarioCriado.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Não deve realizar login com espaços no email")
    public void testLoginComEspacosNoEmail() {
        Login login = Login.builder()
                .email("  " + usuarioCriado.getEmail() + "  ")
                .password(usuarioCriado.getPassword())
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Não deve realizar login com espaços na senha")
    public void testLoginComEspacosNaSenha() {
        Login login = Login.builder()
                .email(usuarioCriado.getEmail())
                .password("  " + usuarioCriado.getPassword() + "  ")
                .build();
        Response response = loginService.realizarLogin(login);
        response.then()
                .statusCode(401)
                .body("message", equalTo("Email e/ou senha inválidos"));
    }
}