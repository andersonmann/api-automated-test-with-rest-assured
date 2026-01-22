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

import static org.hamcrest.Matchers.*;

public class ContratoTest extends BaseTest {

    private final UsuarioService usuarioService = new UsuarioService();
    private final LoginService loginService = new LoginService();
    private String userId;

    @AfterEach
    public void limparDados() {
        if (userId != null) {
            usuarioService.excluirUsuario(userId);
        }
    }

    @Test
    @DisplayName("Validar schema JSON da resposta de listagem de usuários")
    public void testValidarSchemaListagemUsuarios() {
        Response response = usuarioService.listarUsuarios();

        response.then()
                .statusCode(200)
                // Validar estrutura principal
                .body("$", hasKey("usuarios"))
                .body("$", hasKey("quantidade"))
                // Validar tipos
                .body("usuarios", isA(java.util.List.class))
                .body("quantidade", isA(Integer.class))
                // Validar que a lista contém objetos com a estrutura correta
                .body("usuarios[0]", hasKey("nome"))
                .body("usuarios[0]", hasKey("email"))
                .body("usuarios[0]", hasKey("password"))
                .body("usuarios[0]", hasKey("administrador"))
                .body("usuarios[0]", hasKey("_id"))
                // Validar tipos dos campos do usuário
                .body("usuarios[0].nome", isA(String.class))
                .body("usuarios[0].email", isA(String.class))
                .body("usuarios[0].password", isA(String.class))
                .body("usuarios[0].administrador", isA(String.class))
                .body("usuarios[0]._id", isA(String.class));
    }

    @Test
    @DisplayName("Validar schema JSON da resposta de cadastro de usuário")
    public void testValidarSchemaCadastroUsuario() {
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.cadastrarUsuario(usuario);

        userId = response.jsonPath().getString("_id");

        response.then()
                .statusCode(201)
                // Validar estrutura
                .body("$", hasKey("message"))
                .body("$", hasKey("_id"))
                // Validar tipos
                .body("message", isA(String.class))
                .body("_id", isA(String.class))
                // Validar valores esperados
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", matchesPattern("^[a-zA-Z0-9]+$"))
                // Validar que _id não está vazio
                .body("_id", not(emptyString()));
    }

    @Test
    @DisplayName("Validar schema JSON da resposta de login")
    public void testValidarSchemaLogin() {
        // Criar usuário para login
        Usuario usuario = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Realizar login
        Login login = DataFactory.criarLoginValido(usuario);
        Response response = loginService.realizarLogin(login);

        response.then()
                .statusCode(200)
                // Validar estrutura
                .body("$", hasKey("message"))
                .body("$", hasKey("authorization"))
                // Validar tipos
                .body("message", isA(String.class))
                .body("authorization", isA(String.class))
                // Validar valores esperados
                .body("message", equalTo("Login realizado com sucesso"))
                .body("authorization", startsWith("Bearer "))
                // Validar formato do token
                .body("authorization", matchesPattern("^Bearer [A-Za-z0-9\\-_\\.]+$"));
    }

    @Test
    @DisplayName("Validar tipos de dados retornados na busca por ID")
    public void testValidarTiposDadosBuscaUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Buscar usuário
        Response response = usuarioService.buscarUsuarioPorId(userId);

        response.then()
                .statusCode(200)
                // Validar campos obrigatórios
                .body("$", hasKey("nome"))
                .body("$", hasKey("email"))
                .body("$", hasKey("password"))
                .body("$", hasKey("administrador"))
                .body("$", hasKey("_id"))
                // Validar tipos corretos
                .body("nome", isA(String.class))
                .body("email", isA(String.class))
                .body("password", isA(String.class))
                .body("administrador", isA(String.class))
                .body("_id", isA(String.class))
                // Validar que administrador é "true" ou "false" (string)
                .body("administrador", anyOf(equalTo("true"), equalTo("false")))
                // Validar formato de email
                .body("email", matchesPattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"));
    }

    @Test
    @DisplayName("Validar campos obrigatórios na resposta de listagem")
    public void testValidarCamposObrigatoriosListagem() {
        Response response = usuarioService.listarUsuarios();

        response.then()
                .statusCode(200)
                // Todos estes campos devem existir
                .body("$", hasKey("usuarios"))
                .body("$", hasKey("quantidade"))
                // A quantidade deve ser >= 0
                .body("quantidade", greaterThanOrEqualTo(0))
                // Se houver usuários, validar estrutura completa
                .body("usuarios", everyItem(hasKey("nome")))
                .body("usuarios", everyItem(hasKey("email")))
                .body("usuarios", everyItem(hasKey("password")))
                .body("usuarios", everyItem(hasKey("administrador")))
                .body("usuarios", everyItem(hasKey("_id")));
    }

    @Test
    @DisplayName("Validar estrutura de erro com campos inválidos")
    public void testValidarSchemaRespostaErro() {
        Usuario usuario = Usuario.builder()
                .nome("")
                .email("emailinvalido")
                .password("")
                .administrador("invalido")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);

        response.then()
                .statusCode(400)
                // Validar que a resposta de erro contém os campos esperados
                .body("$", hasKey("nome"))
                .body("$", hasKey("email"))
                .body("$", hasKey("password"))
                .body("$", hasKey("administrador"))
                // Validar que todos os erros são strings
                .body("nome", isA(String.class))
                .body("email", isA(String.class))
                .body("password", isA(String.class))
                .body("administrador", isA(String.class));
    }

    @Test
    @DisplayName("Validar estrutura completa da resposta de edição")
    public void testValidarSchemaEdicaoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Editar usuário
        Usuario usuarioEditado = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);

        response.then()
                .statusCode(200)
                // Validar estrutura
                .body("$", hasKey("message"))
                // Validar tipo
                .body("message", isA(String.class))
                // Validar valor
                .body("message", equalTo("Registro alterado com sucesso"))
                // Validar que não há outros campos
                .body("size()", equalTo(1));
    }

    @Test
    @DisplayName("Validar estrutura da resposta de exclusão")
    public void testValidarSchemaExclusaoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        userId = cadastroResponse.jsonPath().getString("_id");

        // Excluir usuário
        Response response = usuarioService.excluirUsuario(userId);

        userId = null; // Já foi excluído

        response.then()
                .statusCode(200)
                // Validar estrutura
                .body("$", hasKey("message"))
                // Validar tipo
                .body("message", isA(String.class))
                // Validar valor
                .body("message", equalTo("Registro excluído com sucesso"))
                // Validar que não há outros campos
                .body("size()", equalTo(1));
    }
}
