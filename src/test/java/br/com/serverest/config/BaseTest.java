package br.com.serverest.config;

import br.com.serverest.model.Usuario;
import br.com.serverest.service.UsuarioService;
import br.com.serverest.utils.DataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BaseTest {

    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;
    protected static Properties config;
    protected final UsuarioService usuarioService = new UsuarioService();
    protected List<String> usuariosParaLimpar = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        loadConfig();
        configureRestAssured();
        setupRequestSpecification();
        setupResponseSpecification();
    }

    private static void loadConfig() {
        config = new Properties();
        try (InputStream input = BaseTest.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Arquivo config.properties não encontrado");
            }
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar configurações", e);
        }
    }

    private static void configureRestAssured() {
        RestAssured.baseURI = config.getProperty("base.uri");
        RestAssured.basePath = config.getProperty("base.path");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Configurar ObjectMapper para usar Jackson
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2));
        
        // Adicionar filtro do Allure para capturar requests/responses
        RestAssured.filters(new AllureRestAssured());
    }

    private static void setupRequestSpecification() {
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.setAccept(ContentType.JSON);
        reqBuilder.addFilter(new AllureRestAssured());
        
        if (Boolean.parseBoolean(config.getProperty("enable.request.logging"))) {
            reqBuilder.log(LogDetail.ALL);
        }
        
        requestSpec = reqBuilder.build();
    }

    private static void setupResponseSpecification() {
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder();
        resBuilder.expectContentType(ContentType.JSON);
        
        if (Boolean.parseBoolean(config.getProperty("enable.response.logging"))) {
            resBuilder.log(LogDetail.ALL);
        }
        
        responseSpec = resBuilder.build();
    }

    protected static String getConfig(String key) {
        return config.getProperty(key);
    }

    // ==================== TEMPLATE METHODS ====================
    
    /**
     * Cria um usuário e retorna o ID, adicionando à lista de limpeza automática
     */
    protected String criarUsuarioERetornarId(boolean isAdmin) {
        Usuario usuario = DataFactory.criarUsuarioValido(isAdmin);
        Response response = usuarioService.cadastrarUsuario(usuario);
        String userId = response.jsonPath().getString("_id");
        usuariosParaLimpar.add(userId);
        return userId;
    }

    /**
     * Cria um usuário e retorna o objeto Usuario criado
     */
    protected Usuario criarUsuarioERetornarObjeto(boolean isAdmin) {
        Usuario usuario = DataFactory.criarUsuarioValido(isAdmin);
        Response response = usuarioService.cadastrarUsuario(usuario);
        String userId = response.jsonPath().getString("_id");
        usuariosParaLimpar.add(userId);
        return usuario;
    }

    /**
     * Cria um usuário customizado e retorna o ID
     */
    protected String criarUsuarioCustomizadoERetornarId(Usuario usuario) {
        Response response = usuarioService.cadastrarUsuario(usuario);
        String userId = response.jsonPath().getString("_id");
        if (userId != null) {
            usuariosParaLimpar.add(userId);
        }
        return userId;
    }

    /**
     * Valida resposta de sucesso padrão (201 - Cadastro)
     */
    protected void validarRespostaCadastroSucesso(Response response) {
        response.then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue());
    }

    /**
     * Valida resposta de sucesso padrão (200 - Operação bem-sucedida)
     */
    protected void validarRespostaOperacaoSucesso(Response response, String mensagemEsperada) {
        response.then()
                .statusCode(200)
                .body("message", equalTo(mensagemEsperada));
    }

    /**
     * Valida resposta de erro 400 com mensagem específica
     */
    protected void validarRespostaErro400(Response response, String campo, String mensagemEsperada) {
        response.then()
                .statusCode(400)
                .body(campo, equalTo(mensagemEsperada));
    }

    /**
     * Valida resposta de erro 401 (não autorizado)
     */
    protected void validarRespostaErro401(Response response, String mensagemEsperada) {
        response.then()
                .statusCode(401)
                .body("message", equalTo(mensagemEsperada));
    }

    /**
     * Extrai ID da resposta
     */
    protected String extrairIdDaResposta(Response response) {
        return response.jsonPath().getString("_id");
    }

    // ==================== ALLURE ATTACHMENTS ====================

    /**
     * Anexa o request body como JSON no relatório Allure
     */
    protected void anexarRequestBody(Object requestBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            Allure.addAttachment("Request Body", "application/json", json, "json");
        } catch (Exception e) {
            Allure.addAttachment("Request Body Error", "text/plain", e.getMessage());
        }
    }

    /**
     * Anexa o response completo no relatório Allure
     */
    protected void anexarResponse(Response response) {
        anexarResponseAsText(response.getBody().asString());
        Allure.addAttachment("Response Status", "text/plain", 
                String.valueOf(response.getStatusCode()));
        Allure.addAttachment("Response Headers", "text/plain", 
                response.getHeaders().toString());
    }

    /**
     * Anexa response body formatado
     */
    @Attachment(value = "Response Body", type = "application/json")
    protected String anexarResponseAsText(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(responseBody, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return responseBody;
        }
    }

    /**
     * Anexa dados de teste utilizados
     */
    protected void anexarDadosDeTeste(String nome, Object dados) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dados);
            Allure.addAttachment(nome, "application/json", json, "json");
        } catch (Exception e) {
            Allure.addAttachment(nome, "text/plain", dados.toString());
        }
    }

    /**
     * Anexa texto simples
     */
    @Attachment(value = "{attachmentName}", type = "text/plain")
    protected String anexarTexto(String attachmentName, String conteudo) {
        return conteudo;
    }

    /**
     * Anexa informações do ambiente de teste
     */
    protected void anexarInformacoesAmbiente() {
        StringBuilder info = new StringBuilder();
        info.append("Base URI: ").append(RestAssured.baseURI).append("\n");
        info.append("Base Path: ").append(RestAssured.basePath).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append(" ")
            .append(System.getProperty("os.version")).append("\n");
        info.append("User: ").append(System.getProperty("user.name")).append("\n");
        
        Allure.addAttachment("Environment Info", "text/plain", info.toString());
    }

    /**
     * Anexa log customizado
     */
    protected void anexarLog(String mensagem) {
        Allure.addAttachment("Test Log", "text/plain", 
                String.format("[%s] %s", java.time.LocalDateTime.now(), mensagem));
    }

    /**
     * Anexa comparação de dados (esperado vs atual)
     */
    protected void anexarComparacao(String esperado, String atual) {
        StringBuilder comparacao = new StringBuilder();
        comparacao.append("=== ESPERADO ===\n");
        comparacao.append(esperado).append("\n\n");
        comparacao.append("=== ATUAL ===\n");
        comparacao.append(atual).append("\n");
        
        Allure.addAttachment("Comparação", "text/plain", comparacao.toString());
    }

    /**
     * Anexa dados em formato CSV
     */
    @Attachment(value = "{fileName}", type = "text/csv")
    protected String anexarCSV(String fileName, String csvContent) {
        return csvContent;
    }

    /**
     * Anexa informações de erro com stack trace
     */
    protected void anexarErro(Throwable erro) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("Erro: ").append(erro.getMessage()).append("\n\n");
        errorInfo.append("Stack Trace:\n");
        for (StackTraceElement element : erro.getStackTrace()) {
            errorInfo.append(element.toString()).append("\n");
        }
        
        Allure.addAttachment("Error Details", "text/plain", errorInfo.toString());
    }

    /**
     * Limpa todos os usuários criados durante o teste
     */
    @AfterEach
    public void limparUsuariosCriados() {
        for (String userId : usuariosParaLimpar) {
            try {
                usuarioService.excluirUsuario(userId);
            } catch (Exception e) {
                // Ignora erros de limpeza
            }
        }
        usuariosParaLimpar.clear();
    }
}