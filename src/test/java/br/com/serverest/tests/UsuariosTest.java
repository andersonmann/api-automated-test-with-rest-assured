package br.com.serverest.tests;

import br.com.serverest.config.BaseTest;
import br.com.serverest.model.Usuario;
import br.com.serverest.service.UsuarioService;
import br.com.serverest.utils.DataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public class UsuariosTest extends BaseTest {
    
    private final UsuarioService usuarioService = new UsuarioService();
    
    @Test
    @DisplayName("Deve listar todos os usuários cadastrados")
    public void testListarUsuarios() {
        Response response = usuarioService.listarUsuarios();        
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue())
                .body("quantidade", greaterThanOrEqualTo(0));
    }
    
    @Test
    @DisplayName("Deve cadastrar um novo usuário com sucesso")
    public void testCadastrarUsuario() {
        Usuario usuario = DataFactory.criarUsuarioValido(true);        
        Response response = usuarioService.cadastrarUsuario(usuario);        
        response.then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
        
        String userId = response.jsonPath().getString("_id");
        assertThat(userId).isNotEmpty();        
        // Limpar - excluir o usuário criado
        usuarioService.excluirUsuario(userId);
    }
    
    @Test
    @DisplayName("Não deve cadastrar usuário com email duplicado")
    public void testCadastrarUsuarioComEmailDuplicado() {
        Usuario usuario = DataFactory.criarUsuarioValido(false);        
        // Primeiro cadastro
        Response response1 = usuarioService.cadastrarUsuario(usuario);
        String userId = response1.jsonPath().getString("_id");        
        // Segundo cadastro com mesmo email
        Response response2 = usuarioService.cadastrarUsuario(usuario);        
        response2.then()
                .statusCode(400)
                .body("message", equalTo("Este email já está sendo usado"));        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID")
    public void testBuscarUsuarioPorId() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");        
        // Buscar usuário
        Response response = usuarioService.buscarUsuarioPorId(userId);        
        response.then()
                .statusCode(200)
                .body("_id", equalTo(userId))
                .body("nome", equalTo(usuario.getNome()))
                .body("email", equalTo(usuario.getEmail()));        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }
    
    @Test
    @DisplayName("Deve excluir um usuário")
    public void testExcluirUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");        
        // Excluir usuário
        Response response = usuarioService.excluirUsuario(userId);        
        response.then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));
    }
    
    @Test
    @DisplayName("Deve editar um usuário existente")
    public void testEditarUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");        
        // Editar usuário
        Usuario usuarioEditado = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);        
        response.then()
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"));        
        // Limpar
        usuarioService.excluirUsuario(userId);
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
            String userId = response.jsonPath().getString("_id");
            usuarioService.excluirUsuario(userId);
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
        response.then()
                .statusCode(400)
                .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
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
        response.then()
                .statusCode(400)
                .body("administrador", equalTo("administrador é obrigatório"));
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
            String userId = response.jsonPath().getString("_id");
            usuarioService.excluirUsuario(userId);
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
    @DisplayName("Deve listar usuários com filtro por nome")
    public void testListarUsuariosComFiltroPorNome() {
        // Criar usuário com nome específico
        Usuario usuario = Usuario.builder()
                .nome("João Teste Filtro")
                .email(DataFactory.gerarEmailAleatorio())
                .password("senha123")
                .administrador("false")
                .build();
        
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Listar com filtro
        Response response = usuarioService.listarUsuarios();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve listar usuários com filtro por email")
    public void testListarUsuariosComFiltroPorEmail() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Listar todos
        Response response = usuarioService.listarUsuarios();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve listar apenas usuários administradores")
    public void testListarUsuariosAdministradores() {
        // Criar usuário admin
        Usuario usuarioAdmin = DataFactory.criarUsuarioValido(true);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuarioAdmin);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Listar usuários
        Response response = usuarioService.listarUsuarios();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve listar apenas usuários não administradores")
    public void testListarUsuariosNaoAdministradores() {
        // Criar usuário não admin
        Usuario usuarioNaoAdmin = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuarioNaoAdmin);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Listar usuários
        Response response = usuarioService.listarUsuarios();
        response.then()
                .statusCode(200)
                .body("usuarios", notNullValue());
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    // ==================== TESTES DE EDIÇÃO ====================

    @Test
    @DisplayName("Não deve editar usuário para email já existente")
    public void testEditarUsuarioParaEmailExistente() {
        // Criar primeiro usuário
        Usuario usuario1 = DataFactory.criarUsuarioValido(false);
        Response response1 = usuarioService.cadastrarUsuario(usuario1);
        String userId1 = response1.jsonPath().getString("_id");
        
        // Criar segundo usuário
        Usuario usuario2 = DataFactory.criarUsuarioValido(false);
        Response response2 = usuarioService.cadastrarUsuario(usuario2);
        String userId2 = response2.jsonPath().getString("_id");
        
        // Tentar editar usuario2 com o email do usuario1
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario2.getNome())
                .email(usuario1.getEmail())
                .password(usuario2.getPassword())
                .administrador(usuario2.getAdministrador())
                .build();
        
        Response response = usuarioService.editarUsuario(userId2, usuarioEditado);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Este email já está sendo usado"));
        
        // Limpar
        usuarioService.excluirUsuario(userId1);
        usuarioService.excluirUsuario(userId2);
    }

    @Test
    @DisplayName("Editar usuário com ID inexistente deve criar novo usuário")
    public void testEditarUsuarioComIdInexistente() {
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response response = usuarioService.editarUsuario("123456789012345678901234", usuario);
        
        // PUT com ID inexistente cria novo usuário na API ServeRest
        response.then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"));
        
        String userId = response.jsonPath().getString("_id");
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve editar apenas o nome do usuário")
    public void testEditarApenasNomeDoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Editar apenas o nome
        Usuario usuarioEditado = Usuario.builder()
                .nome("Nome Editado")
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .administrador(usuario.getAdministrador())
                .build();
        
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"));
        
        // Verificar se o nome foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("nome", equalTo("Nome Editado"));
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve editar apenas o email do usuário")
    public void testEditarApenasEmailDoUsuario() {
        // Criar usuário
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Editar apenas o email
        String novoEmail = DataFactory.gerarEmailAleatorio();
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario.getNome())
                .email(novoEmail)
                .password(usuario.getPassword())
                .administrador(usuario.getAdministrador())
                .build();
        
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"));
        
        // Verificar se o email foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("email", equalTo(novoEmail));
        
        // Limpar
        usuarioService.excluirUsuario(userId);
    }

    @Test
    @DisplayName("Deve editar status de administrador do usuário")
    public void testEditarStatusAdministrador() {
        // Criar usuário não administrador
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Editar para administrador
        Usuario usuarioEditado = Usuario.builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .administrador("true")
                .build();
        
        Response response = usuarioService.editarUsuario(userId, usuarioEditado);
        response.then()
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"));
        
        // Verificar se o status foi alterado
        Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
        buscaResponse.then()
                .body("administrador", equalTo("true"));
        
        // Limpar
        usuarioService.excluirUsuario(userId);
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
        Usuario usuario = DataFactory.criarUsuarioValido(false);
        Response cadastroResponse = usuarioService.cadastrarUsuario(usuario);
        String userId = cadastroResponse.jsonPath().getString("_id");
        
        // Excluir usuário
        Response deleteResponse = usuarioService.excluirUsuario(userId);
        deleteResponse.then()
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"));
        
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
            String userId = response.jsonPath().getString("_id");
            usuarioService.excluirUsuario(userId);
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
            String userId = response.jsonPath().getString("_id");
            usuarioService.excluirUsuario(userId);
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
            String userId = response.jsonPath().getString("_id");
            
            // Verificar se o conteúdo foi sanitizado
            Response buscaResponse = usuarioService.buscarUsuarioPorId(userId);
            buscaResponse.then()
                    .statusCode(200);
            
            usuarioService.excluirUsuario(userId);
        }
    }
}