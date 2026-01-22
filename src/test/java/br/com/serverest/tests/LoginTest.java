package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Login;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.LoginService;
import br.com.serverest.utils.DataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.hamcrest.Matchers.*;

@Epic("API ServeRest")
@Feature("Autenticação")
public class LoginTest extends BaseTest {

    private final LoginService loginService = new LoginService();
    private Usuario usuarioCriado;

    @BeforeEach
    public void criarUsuarioParaTeste() {
        usuarioCriado = criarUsuarioERetornarObjeto(true);
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    @Description("Valida que um usuário com credenciais válidas consegue realizar login e recebe um token JWT")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Login - Casos de Sucesso")
    public void testLoginComSucesso() {
        Login login = DataFactory.criarLoginValido(usuarioCriado);        
        anexarDadosDeTeste("Credenciais de Login", login);        
        Response response = loginService.realizarLogin(login);        
        anexarResponse(response);        
        String token = loginService.extrairTokenLimpo(response);
        anexarTexto("Token JWT", token);        
        response.then()
                .statusCode(200)
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", notNullValue())
                .body("authorization", startsWith("Bearer "));        
        anexarLog("Login realizado com sucesso: " + loginService.loginFoiSucesso(response));
    }

    @Test
    @DisplayName("Não deve realizar login com email inválido")
    @Description("Valida que a API rejeita login com email não cadastrado")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Login - Casos de Erro")
    public void testLoginComEmailInvalido() {
        Response response = loginService.realizarLogin("email.invalido@teste.com", usuarioCriado.getPassword());        
        anexarDadosDeTeste("Email Inválido", "email.invalido@teste.com");
        anexarResponse(response);        
        validarRespostaErro401(response, "Email e/ou senha inválidos");
    }

    @Test
    @DisplayName("Não deve realizar login com senha inválida")
    @Description("Valida que a API rejeita login com senha incorreta")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Login - Casos de Erro")
    public void testLoginComSenhaInvalida() {
        Response response = loginService.realizarLogin(usuarioCriado.getEmail(), "senhaerrada123");        
        anexarDadosDeTeste("Senha Inválida", "senhaerrada123");
        anexarResponse(response);        
        validarRespostaErro401(response, "Email e/ou senha inválidos");
    }

    @ParameterizedTest(name = "Validação de email vazio/inválido: {0}")
    @DisplayName("Não deve realizar login com email em formato inválido")
    @Description("Valida que a API valida o formato do email usando testes parametrizados")
    @Severity(SeverityLevel.NORMAL)
    @Story("Login - Validações de Campo")
    @CsvSource({
        "emailinvalido, email deve ser um email válido",
        "'  email@teste.com  ', email deve ser um email válido"
    })
    public void testLoginComEmailInvalido(String email, String mensagemEsperada) {
        Login login = Login.builder()
                .email(email)
                .password(usuarioCriado.getPassword())
                .build();
        
        anexarDadosDeTeste("Login com Email Inválido: " + email, login);        
        Response response = loginService.realizarLogin(login);    
        anexarResponse(response);        
        validarRespostaErro400(response, "email", mensagemEsperada);
    }

    @ParameterizedTest(name = "Campo obrigatório: {0}")
    @DisplayName("Não deve realizar login sem campos obrigatórios")
    @Description("Valida que a API requer os campos obrigatórios usando testes parametrizados")
    @Severity(SeverityLevel.NORMAL)
    @Story("Login - Validações de Campo")
    @CsvSource({
        "email, email é obrigatório",
        "password, password é obrigatório"
    })
    public void testLoginCamposObrigatorios(String campo, String mensagem) {
        Response response;        
        if (campo.equals("email")) {
            response = loginService.realizarLoginSemEmail(usuarioCriado.getPassword());
        } else {
            response = loginService.realizarLoginSemSenha(usuarioCriado.getEmail());
        }        
        anexarDadosDeTeste("Login sem campo: " + campo, "Campo " + campo + " omitido");
        anexarResponse(response);        
        validarRespostaErro400(response, campo, mensagem);
    }

    @ParameterizedTest(name = "Campo vazio: {0}")
    @DisplayName("Não deve realizar login com campos vazios")
    @Description("Valida que a API não aceita campos em branco usando testes parametrizados")
    @Severity(SeverityLevel.NORMAL)
    @Story("Login - Validações de Campo")
    @CsvSource({
        "email, email não pode ficar em branco",
        "password, password não pode ficar em branco"
    })
    public void testLoginCamposVazios(String campo, String mensagem) {
        Login login;        
        if (campo.equals("email")) {
            login = Login.builder()
                    .email("")
                    .password(usuarioCriado.getPassword())
                    .build();
        } else {
            login = Login.builder()
                    .email(usuarioCriado.getEmail())
                    .password("")
                    .build();
        }        
        anexarDadosDeTeste("Login com campo vazio: " + campo, login);        
        Response response = loginService.realizarLogin(login);        
        anexarResponse(response);        
        validarRespostaErro400(response, campo, mensagem);
    }

    @Test
    @DisplayName("Não deve realizar login com ambos os campos vazios")
    @Description("Valida que a API retorna erro para ambos os campos quando estão vazios")
    @Severity(SeverityLevel.NORMAL)
    @Story("Login - Validações de Campo")
    public void testLoginComCamposVazios() {
        Response response = loginService.realizarLoginComCamposVazios();        
        anexarDadosDeTeste("Login com Campos Vazios", "Email e senha vazios");
        anexarResponse(response);        
        response.then()
                .statusCode(400)
                .body("email", equalTo("email não pode ficar em branco"))
                .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @DisplayName("Não deve realizar login com espaços na senha")
    @Description("Valida que a API não aceita senha com espaços em branco")
    @Severity(SeverityLevel.MINOR)
    @Story("Login - Validações de Campo")
    public void testLoginComEspacosNaSenha() {
        Login login = Login.builder()
                .email(usuarioCriado.getEmail())
                .password("  " + usuarioCriado.getPassword() + "  ")
                .build();        
        anexarDadosDeTeste("Login com Espaços na Senha", login);        
        Response response = loginService.realizarLogin(login);        
        anexarResponse(response);        
        validarRespostaErro401(response, "Email e/ou senha inválidos");
    }
}