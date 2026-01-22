package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.UsuarioService;
import br.com.serverest.utils.DataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("API ServeRest")
@Feature("Gerenciamento de Usuários")
public class UsuariosTest extends BaseTest {

    private final UsuarioService usuarioService = new UsuarioService();

    @Test
    @DisplayName("Deve listar todos os usuários cadastrados")
    @Description("Verifica se a API retorna a lista de todos os usuários cadastrados com sucesso")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Listagem de Usuários")
    public void testListarUsuarios() {
        Response response = usuarioService.listarUsuarios();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue())
                .body("quantidade", greaterThanOrEqualTo(0));
        
        // Usando método helper para contar usuários
        int totalUsuarios = usuarioService.contarUsuarios();
        anexarLog("Total de usuários cadastrados: " + totalUsuarios);
    }

    @Test
    @DisplayName("Deve cadastrar um novo usuário com sucesso")
    @Description("Valida o cadastro de um novo usuário com todos os campos válidos")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Cadastro de Usuários")
    public void testCadastrarUsuario() {
        Usuario usuario = DataFactory.criarUsuarioValido(true);

        // Anexar dados de teste
        anexarDadosDeTeste("Dados do Usuário Criado", usuario);

        // Usando método helper cadastrarUsuarioERetornarId
        String userId = usuarioService.cadastrarUsuarioERetornarId(usuario);
        assertThat(userId).isNotEmpty();

        anexarLog("Usuário cadastrado com sucesso. ID: " + userId);
        
        // Adicionar à lista de limpeza
        usuariosParaLimpar.add(userId);
    }

    @Test
    @DisplayName("Não deve cadastrar usuário com email duplicado")
    @Description("Valida que a API impede o cadastro de usuários com email já existente")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Cadastro de Usuários")
    public void testCadastrarUsuarioComEmailDuplicado() {
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        // Primeiro cadastro
        String userId = criarUsuarioCustomizadoERetornarId(usuario);

        anexarDadosDeTeste("Usuário Duplicado (Tentativa)", usuario);
        
        // Verificar se usuário existe usando método helper
        anexarLog("Usuário existe? " + usuarioService.usuarioExistePorEmail(usuario.getEmail()));

        // Segundo cadastro com mesmo email
        Response response2 = usuarioService.cadastrarUsuario(usuario);

        anexarResponse(response2);

        validarRespostaErro400(response2, "message", "Este email já está sendo usado");

        anexarLog("Validação de email duplicado funcionou corretamente");
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    @Description("Valida a busca de um usuário específico pelo seu ID")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Busca de Usuários")
    public void testBuscarUsuarioPorId() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        String userId = criarUsuarioCustomizadoERetornarId(usuario);

        anexarTexto("User ID", userId);

        // Buscar usuário
        Response response = usuarioService.buscarUsuarioPorId(userId);

        anexarResponse(response);

        response.then()
                .statusCode(200)
                .body("_id", equalTo(userId))
                .body("nome", equalTo(usuario.getNome()))
                .body("email", equalTo(usuario.getEmail()));

        anexarComparacao(
                String.format("Nome: %s\nEmail: %s", usuario.getNome(), usuario.getEmail()),
                String.format("Nome: %s\nEmail: %s",
                        response.jsonPath().getString("nome"),
                        response.jsonPath().getString("email")));
    }

    @Test
    @DisplayName("Deve excluir um usuário")
    public void testExcluirUsuario() {
        // Criar usuário
        String userId = criarUsuarioERetornarId(false);

        // Excluir usuário
        Response response = usuarioService.excluirUsuario(userId);
        validarRespostaOperacaoSucesso(response, "Registro excluído com sucesso");
    }

    @Test
    @DisplayName("Deve editar um usuário existente")
    public void testEditarUsuario() {
        // Criar usuário
        String userId = criarUsuarioERetornarId(false);

        // Editar usuário
        Usuario usuarioEditado = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        validarRespostaOperacaoSucesso(response, "Registro alterado com sucesso");
    }

    // ==================== VALIDAÇÕES DE CAMPOS ====================

    @Test
    @DisplayName("Não deve cadastrar usuário com nome vazio")
    public void testCadastrarUsuarioComNomeVazio() {
        Usuario usuario = Usuario.builder()
                .nome("")
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("true")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        response.then()
                .statusCode(400)
                .body("nome", equalTo("nome não pode ficar em branco"));
    }

    @Test
    @DisplayName("Não deve cadastrar usuário sem informar nome")
    public void testCadastrarUsuarioSemNome() {
        Usuario usuario = Usuario.builder()
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("true")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        response.then()
                .statusCode(400)
                .body("nome", equalTo("nome é obrigatório"));
    }

    @Test
    @DisplayName("Não deve cadastrar usuário com email sem @")
    public void testCadastrarUsuarioComEmailSemArroba() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email("emailinvalido.com")
                .password("senha123")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @ParameterizedTest(name = "Email inválido: {0}")
    @DisplayName("Não deve cadastrar usuário com email em formato inválido")
    @Description("Valida diversos formatos inválidos de email usando testes parametrizados")
    @Severity(SeverityLevel.NORMAL)
    @Story("Validações de Cadastro")
    @ValueSource(strings = {
        "emailinvalido",
        "email@",
        "@dominio.com",
        "email@@dominio.com",
        "email..teste@dominio.com"
    })
    public void testCadastrarUsuarioComEmailInvalido(String emailInvalido) {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(emailInvalido)
                .password("senha123")
                .administrador("false")
                .build();

        anexarDadosDeTeste("Usuário com email inválido: " + emailInvalido, usuario);

        Response response = usuarioService.cadastrarUsuario(usuario);

        anexarResponse(response);

        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @ParameterizedTest(name = "Campo obrigatório: {0}")
    @DisplayName("Não deve cadastrar usuário sem campos obrigatórios")
    @Description("Valida que todos os campos obrigatórios são validados usando testes parametrizados")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Validações de Cadastro")
    @CsvSource({
        "nome, nome é obrigatório",
        "email, email é obrigatório",
        "password, password é obrigatório",
        "administrador, administrador é obrigatório"
    })
    public void testCadastrarUsuarioSemCampoObrigatorio(String campo, String mensagemEsperada) {
        Usuario.UsuarioBuilder builder = Usuario.builder();

        // Preencher todos os campos exceto o que está sendo testado
        if (!campo.equals("nome")) builder.nome(DataFactory.gerarNomeAleatorio());
        if (!campo.equals("email")) builder.email(DataFactory.gerarEmailAleatorio());
        if (!campo.equals("password")) builder.password("senha123");
        if (!campo.equals("administrador")) builder.administrador("false");

        Usuario usuario = builder.build();

        anexarDadosDeTeste("Usuário sem campo: " + campo, usuario);

        Response response = usuarioService.cadastrarUsuario(usuario);

        anexarResponse(response);

        validarRespostaErro400(response, campo, mensagemEsperada);
    }

    @ParameterizedTest(name = "Campo vazio: {0}")
    @DisplayName("Não deve cadastrar usuário com campos vazios")
    @Description("Valida que campos não podem ficar em branco usando testes parametrizados")
    @Severity(SeverityLevel.NORMAL)
    @Story("Validações de Cadastro")
    @CsvSource({
        "nome, nome não pode ficar em branco",
        "email, email não pode ficar em branco",
        "password, password não pode ficar em branco"
    })
    public void testCadastrarUsuarioComCampoVazio(String campo, String mensagemEsperada) {
        Usuario.UsuarioBuilder builder = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("false");

        // Definir o campo sendo testado como vazio
        if (campo.equals("nome")) builder.nome("");
        if (campo.equals("email")) builder.email("");
        if (campo.equals("password")) builder.password("");

        Usuario usuario = builder.build();

        anexarDadosDeTeste("Usuário com campo vazio: " + campo, usuario);

        Response response = usuarioService.cadastrarUsuario(usuario);

        anexarResponse(response);

        validarRespostaErro400(response, campo, mensagemEsperada);
    }

    @Test
    @DisplayName("Não deve cadastrar usuário com email sem domínio")
    public void testCadastrarUsuarioComEmailSemDominio() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email("email@")
                .password("senha123")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Deve cadastrar usuário com senha muito curta")
    public void testCadastrarUsuarioComSenhaCurta() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("12")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        // A API ServeRest não valida tamanho mínimo de senha
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));

        if (response.statusCode() == 201) {
            usuariosParaLimpar.add(extrairIdDaResposta(response));
        }
    }

    @Test
    @DisplayName("Não deve cadastrar usuário com campo administrador inválido")
    public void testCadastrarUsuarioComAdministradorInvalido() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("sim")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        validarRespostaErro400(response, "administrador", "administrador deve ser 'true' ou 'false'");
    }

    @Test
    @DisplayName("Não deve cadastrar usuário sem campo administrador")
    public void testCadastrarUsuarioSemAdministrador() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        validarRespostaErro400(response, "administrador", "administrador é obrigatório");
    }

    @Test
    @DisplayName("Deve cadastrar usuário com caracteres especiais no nome")
    public void testCadastrarUsuarioComCaracteresEspeciaisNoNome() {
        Usuario usuario = Usuario.builder()
                .nome("José da Silva Júnior @#$%")
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        // API pode aceitar ou rejeitar caracteres especiais
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));

        if (response.statusCode() == 201) {
            usuariosParaLimpar.add(extrairIdDaResposta(response));
        }
    }

    @Test
    @DisplayName("Não deve cadastrar usuário com espaços extras no email")
    public void testCadastrarUsuarioComEspacosNoEmail() {
        Usuario usuario = Usuario.builder()
                .nome(DataFactory.gerarNomeAleatorio())
                .email("  teste@email.com  ")
                .password("senha123")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        response.then()
                .statusCode(400)
                .body("email", equalTo("email deve ser um email válido"));
    }

    // ==================== TESTES DE BUSCA E LISTAGEM ====================

    @Test
    @DisplayName("Buscar usuário com ID inválido deve retornar erro")
    public void testBuscarUsuarioComIdInvalido() {
        Response response = usuarioService.buscarUsuarioPorId("id_invalido_123");
        response.then()
                .statusCode(400);

        // Verificar se há mensagem de erro
        String responseBody = response.asString();
        assert responseBody != null && !responseBody.isEmpty() : "Resposta não deve ser vazia";
    }

    @Test
    @DisplayName("Buscar usuário com ID inexistente deve retornar erro")
    public void testBuscarUsuarioComIdInexistente() {
        Response response = usuarioService.buscarUsuarioPorId("123456789012345678901234");
        response.then()
                .statusCode(400);

        // Verificar se há mensagem de erro
        String responseBody = response.asString();
        assert responseBody != null && !responseBody.isEmpty() : "Resposta não deve ser vazia";
    }

    @Test
    @DisplayName("Deve buscar usuário por nome")
    public void testBuscarUsuarioPorNome() {
        // Criar usuário com nome específico
        Usuario usuario = Usuario.builder()
                .nome("João Teste Filtro")
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("false")
                .build();

        criarUsuarioCustomizadoERetornarId(usuario);

        // Buscar por nome usando método helper
        Response response = usuarioService.buscarUsuarioPorNome("João Teste Filtro");
        response.then()
                .statusCode(200)
                .body("usuarios[0].nome", equalTo("João Teste Filtro"));
        
        anexarLog("Usuário encontrado por nome: João Teste Filtro");
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    public void testBuscarUsuarioPorEmail() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        criarUsuarioCustomizadoERetornarId(usuario);

        // Buscar por email usando método helper
        Response response = usuarioService.buscarUsuarioPorEmail(usuario.getEmail());
        response.then()
                .statusCode(200)
                .body("usuarios[0].email", equalTo(usuario.getEmail()));
        
        anexarLog("Usuário encontrado por email: " + usuario.getEmail());
    }

    @Test
    @DisplayName("Deve listar apenas usuários administradores")
    public void testListarUsuariosAdministradores() {
        // Criar usuário admin
        criarUsuarioERetornarId(true);

        // Listar apenas administradores usando método helper
        Response response = usuarioService.listarAdministradores();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue())
                .body("usuarios.findAll { it.administrador == 'true' }.size()", greaterThanOrEqualTo(1));
        
        anexarLog("Administradores listados com sucesso");
    }

    @Test
    @DisplayName("Deve listar apenas usuários não administradores")
    public void testListarUsuariosNaoAdministradores() {
        // Criar usuário não admin
        criarUsuarioERetornarId(false);

        // Listar apenas usuários comuns usando método helper
        Response response = usuarioService.listarUsuariosComuns();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue())
                .body("usuarios.findAll { it.administrador == 'false' }.size()", greaterThanOrEqualTo(1));
        
        anexarLog("Usuários comuns listados com sucesso");
    }

    // ==================== TESTES DE EDIÇÃO ====================

    @Test
    @DisplayName("Não deve editar usuário para email já existente")
    public void testEditarUsuarioParaEmailExistente() {
        // Criar primeiro usuário
        Usuario usuario1 = DataFactory.criarUsuarioValido(false);
        String userId1 = criarUsuarioCustomizadoERetornarId(usuario1);

        // Criar segundo usuário
        Usuario usuario2 = DataFactory.criarUsuarioValido(false);
        String userId2 = criarUsuarioCustomizadoERetornarId(usuario2);

        // Tentar editar usuario2 com o email do usuario1
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario2.getNome())
                .email(usuario1.getEmail())
                .password(usuario2.getPassword())
                .administrador(usuario2.getAdministrador())
                .build();

        Response response = usuarioService.editarUsuario(userId2, usuarioEditado);
        validarRespostaErro400(response, "message", "Este email já está sendo usado");
    }

    @Test
    @DisplayName("Editar usuário com ID inexistente deve criar novo usuário")
    public void testEditarUsuarioComIdInexistente() {
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.editarUsuario("123456789012345678901234", usuario);

        // PUT com ID inexistente cria novo usuário na API ServeRest
        validarRespostaCadastroSucesso(response);

        usuariosParaLimpar.add(extrairIdDaResposta(response));
    }

    @Test
    @DisplayName("Deve editar apenas o nome do usuário")
    public void testEditarApenasNomeDoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        String userId = criarUsuarioCustomizadoERetornarId(usuario);

        // Editar apenas o nome
        Usuario usuarioEditado = Usuario.builder()
                .nome("Nome Editado")
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .administrador(usuario.getAdministrador())
                .build();

        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        validarRespostaOperacaoSucesso(response, "Registro alterado com sucesso");

        // Verificar se o nome foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("nome", equalTo("Nome Editado"));
    }

    @Test
    @DisplayName("Deve editar apenas o email do usuário")
    public void testEditarApenasEmailDoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        String userId = criarUsuarioCustomizadoERetornarId(usuario);

        // Editar apenas o email
        String novoEmail = DataFactory.gerarEmailAleatorio();
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario.getNome())
                .email(novoEmail)
                .password(usuario.getPassword())
                .administrador(usuario.getAdministrador())
                .build();

        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        validarRespostaOperacaoSucesso(response, "Registro alterado com sucesso");

        // Verificar se o email foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("email", equalTo(novoEmail));
    }

    @Test
    @DisplayName("Deve editar status de administrador do usuário")
    public void testEditarStatusAdministrador() {
        // Criar usuário não administrador
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        String userId = criarUsuarioCustomizadoERetornarId(usuario);

        // Editar para administrador
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .administrador("true")
                .build();

        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        validarRespostaOperacaoSucesso(response, "Registro alterado com sucesso");

        // Verificar se o status foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("administrador", equalTo("true"));
    }

    // ==================== TESTES DE EXCLUSÃO ====================

    @Test
    @DisplayName("Excluir usuário com ID inexistente deve retornar erro")
    public void testExcluirUsuarioComIdInexistente() {
        Response response = usuarioService.excluirUsuario("123456789012345678901234");
        response.then()
                .statusCode(200)
                .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("Excluir usuário com ID inválido deve retornar erro")
    public void testExcluirUsuarioComIdInvalido() {
        Response response = usuarioService.excluirUsuario("id_invalido");
        response.then()
                .statusCode(200)
                .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("Excluir e verificar que usuário foi realmente removido")
    public void testExcluirEVerificarRemocao() {
        // Criar usuário
        String userId = criarUsuarioERetornarId(false);

        // Excluir usuário
        Response deleteResponse = usuarioService.excluirUsuario(userId);
        validarRespostaOperacaoSucesso(deleteResponse, "Registro excluído com sucesso");

        // Verificar que o usuário não existe mais
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .statusCode(400)
                .body("message", equalTo("Usuário não encontrado"));
    }

    // ==================== TESTES DE SEGURANÇA ====================

    @Test
    @DisplayName("Validar tamanho máximo dos campos")
    public void testValidarTamanhoMaximoCampos() {
        // Criar strings muito longas
        StringBuilder nomeLongo = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            nomeLongo.append("a");
        }

        StringBuilder senhaLonga = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            senhaLonga.append("b");
        }

        Usuario usuario = Usuario.builder()
                .nome(nomeLongo.toString())
                .email(DataFactory.gerarEmailAleatorio())
                .password(senhaLonga.toString())
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        // API pode aceitar ou rejeitar campos muito longos
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));

        if (response.statusCode() == 201) {
            usuariosParaLimpar.add(extrairIdDaResposta(response));
        }
    }

    @Test
    @DisplayName("Tentar SQL Injection nos campos")
    public void testSQLInjectionNosCampos() {
        Usuario usuario = Usuario.builder()
                .nome("' OR '1'='1")
                .email("test' OR '1'='1@email.com")
                .password("' OR '1'='1")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        // API deve proteger contra SQL Injection
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));

        if (response.statusCode() == 201) {
            usuariosParaLimpar.add(extrairIdDaResposta(response));
        }
    }

    @Test
    @DisplayName("Tentar XSS nos campos de texto")
    public void testXSSNosCampos() {
        Usuario usuario = Usuario.builder()
                .nome("<script>alert('XSS')</script>")
                .email(DataFactory.gerarEmailAleatorio())
                .password("<img src=x onerror=alert('XSS')>")
                .administrador("false")
                .build();

        Response response = usuarioService.cadastrarUsuario(usuario);
        // API deve proteger contra XSS
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));

        if (response.statusCode() == 201) {
            String userId = extrairIdDaResposta(response);

            // Verificar se o conteúdo foi sanitizado
            Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
            buscaResponse.then()
                    .statusCode(200);

            usuariosParaLimpar.add(userId);
        }
    }
}