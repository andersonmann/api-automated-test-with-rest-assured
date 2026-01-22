package br.com.serverest.service;

import br.com.serverest.model.Usuario;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Object Pattern - Usuários
 * Encapsula todas as operações relacionadas ao endpoint /usuarios
 * Facilita manutenção e torna os testes mais legíveis
 */
public class UsuarioService extends BaseService {
    
    private static final String USUARIOS_ENDPOINT = "/usuarios";
    
    @Override
    protected String getBasePath() {
        return USUARIOS_ENDPOINT;
    }
    
    /**
     * Lista todos os usuários
     */
    @Step("Listar todos os usuários")
    public Response listarUsuarios() {
        return doGet();
    }
    
    /**
     * Lista usuários com filtro
     */
    @Step("Listar usuários com filtro: {queryParam}={value}")
    public Response listarUsuarios(String queryParam, String value) {
        Map<String, String> params = new HashMap<>();
        params.put(queryParam, value);
        return doGet(params);
    }
    
    /**
     * Lista usuários com múltiplos filtros
     */
    @Step("Listar usuários com múltiplos filtros")
    public Response listarUsuarios(Map<String, ?> queryParams) {
        return doGet(queryParams);
    }
    
    /**
     * Cadastra um novo usuário
     */
    @Step("Cadastrar usuário: {usuario.nome}")
    public Response cadastrarUsuario(Usuario usuario) {
        return doPost(usuario);
    }
    
    /**
     * Busca usuário por ID
     */
    @Step("Buscar usuário por ID: {id}")
    public Response buscarUsuarioPorId(String id) {
        return doGetById(id);
    }
    
    /**
     * Exclui usuário por ID
     */
    @Step("Excluir usuário por ID: {id}")
    public Response excluirUsuario(String id) {
        return doDelete(id);
    }
    
    /**
     * Edita usuário existente
     */
    @Step("Editar usuário ID: {id} - Nome: {usuario.nome}")
    public Response editarUsuario(String id, Usuario usuario) {
        return doPut(id, usuario);
    }
    
    /**
     * Cadastra usuário e retorna o ID
     */
    @Step("Cadastrar usuário e retornar ID")
    public String cadastrarUsuarioERetornarId(Usuario usuario) {
        Response response = cadastrarUsuario(usuario);
        return extractId(response);
    }
    
    /**
     * Busca usuário por email
     */
    @Step("Buscar usuário por email: {email}")
    public Response buscarUsuarioPorEmail(String email) {
        return listarUsuarios("email", email);
    }
    
    /**
     * Busca usuário por nome
     */
    @Step("Buscar usuário por nome: {nome}")
    public Response buscarUsuarioPorNome(String nome) {
        return listarUsuarios("nome", nome);
    }
    
    /**
     * Lista apenas administradores
     */
    @Step("Listar apenas administradores")
    public Response listarAdministradores() {
        return listarUsuarios("administrador", "true");
    }
    
    /**
     * Lista apenas usuários comuns
     */
    @Step("Listar apenas usuários comuns")
    public Response listarUsuariosComuns() {
        return listarUsuarios("administrador", "false");
    }
    
    /**
     * Cadastra usuário com headers customizados
     */
    @Step("Cadastrar usuário com headers customizados")
    public Response cadastrarUsuarioComHeaders(Usuario usuario, Map<String, String> headers) {
        return doPostWithHeaders(usuario, headers);
    }
    
    /**
     * Verifica se usuário existe por email
     */
    @Step("Verificar se usuário existe por email: {email}")
    public boolean usuarioExistePorEmail(String email) {
        Response response = buscarUsuarioPorEmail(email);
        return response.jsonPath().getInt("quantidade") > 0;
    }
    
    /**
     * Conta total de usuários
     */
    @Step("Contar total de usuários")
    public int contarUsuarios() {
        Response response = listarUsuarios();
        return response.jsonPath().getInt("quantidade");
    }
}