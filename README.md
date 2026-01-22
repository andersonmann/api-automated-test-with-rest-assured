# Test API REST Assured - ServeRest

Projeto de testes automatizados para a API [ServeRest](https://serverest.dev) utilizando RestAssured e Java, implementando design patterns e boas práticas de automação de testes.

## 📋 Pré-requisitos

- Java 11 ou superior
- Maven 3.6 ou superior

## 🚀 Tecnologias Utilizadas

- **RestAssured 5.4.0** - Framework para testes de API REST
- **JUnit 5.10.1** - Framework de testes
- **Allure 2.25.0** - Geração de relatórios elegantes
- **Jackson** - Serialização/Deserialização JSON
- **Lombok** - Redução de código boilerplate
- **AssertJ** - Assertions fluentes
- **JavaFaker** - Geração de dados de teste
- **JSON Schema Validator** - Validação de contratos de API

## 📁 Estrutura do Projeto

```
test-api-rest-assured/
├── src/
│   └── test/
│       ├── java/
│       │   └── br/com/serverest/
│       │       ├── config/
│       │       │   └── BaseTest.java          # Configuração base dos testes
│       │       ├── model/
│       │       │   ├── Usuario.java           # Modelo de usuário
│       │       │   ├── Login.java             # Modelo de login
│       │       ├── service/
│       │       │   ├── BaseService.java       # ⭐ Service Object abstrato
│       │       │   ├── UsuarioService.java    # Serviço de usuários (16 métodos)
│       │       │   ├── LoginService.java      # Serviço de login (10 métodos)
│       │       ├── utils/
│       │       │   └── DataFactory.java       # Factory para dados de teste
│       │       └── tests/
│       │           ├── UsuariosTest.java      # 44 testes de usuários
│       │           ├── LoginTest.java         # 11 testes de login
│       │           ├── ContratoTest.java      # 34 testes de contrato (JSON Schema)
│       │           ├── SecurityTest.java      # 12 testes de segurança
│       └── resources/
│           ├── config.properties              # Configurações da API
├── pom.xml
└── README.md
```

## ⚙️ Configuração

As configurações da API estão no arquivo `src/test/resources/config.properties`:

```properties
base.uri=https://serverest.dev
base.path=/
connection.timeout=10000
socket.timeout=10000
enable.request.logging=true
enable.response.logging=true
```

## 🔧 Instalação

1. Clone o repositório:
```bash
git clone git@github.com:andersonmann/api-automated-test-with-rest-assured.git
cd api-automated-test-with-rest-assured
```

2. Instale as dependências:
```bash
mvn clean install
```

## ▶️ Executando os Testes

### Executar todos os testes:
```bash
mvn clean test
```

### Executar uma classe de teste específica:
```bash
mvn test -Dtest=UsuariosTest
mvn test -Dtest=LoginTest
```

### Executar um teste específico:
```bash
mvn test -Dtest=UsuariosTest#testCadastrarUsuario
```

### Gerar relatório Allure:
```bash
mvn allure:serve
```

## Executando a Pipeline no GitHub Actions

### Como executar manualmente:

1. Acesse o repositório no GitHub
2. Clique na aba **Actions**
3. Selecione o workflow **Run API Tests** na barra lateral
4. Clique no botão **Run workflow** (no lado direito)
5. Selecione a branch desejada (ex: `main` ou `feature-aplicar-design-pattern`)
6. Escolha a suíte de testes no dropdown:
   - **all** - Executa todos os testes
7. Clique em **Run workflow** (botão verde)

### Visualizando o relatório Allure:

#### 📍 Opção 1: Acesso Direto via GitHub Pages (Recomendado)
Após a execução da pipeline, o relatório é automaticamente publicado e pode ser acessado diretamente em:

🔗 **https://andersonmann.github.io/api-automated-test-with-rest-assured/**

Não é necessário fazer download ou instalação. Basta acessar o link no navegador!

PS:Durante o desenvolvimento foi verificado que devido a restrições da API utilizada para os testes, alguns cenários podem falhar devido ao limite de requisições disponivíveis por minuto da API.

**Artefatos disponíveis:**
- `allure-report` - Relatório HTML completo (disponível por 30 dias)

### Status da Pipeline:

A pipeline executa automaticamente as seguintes etapas:
- ✅ Checkout do código
- ✅ Setup do Java 21 com Maven cache
- ✅ Execução dos testes (`mvn clean test`)
- ✅ Geração do relatório Allure
- ✅ Upload do relatório HTML como artefato

## 📊 Cobertura de Testes

| Classe de Teste | Testes | Descrição |
|----------------|--------|-----------|
| **UsuariosTest** | 44 | CRUD, validações, filtros, segurança |
| **LoginTest** | 11 | Autenticação, validações de campos |
| **ContratoTest** | 28 | Validação de JSON Schema |
| **SecurityTest** | 14 | Autenticação, autorização, SQL Injection, XSS |

## 📝 Endpoints Testados

### Usuários (`/usuarios`)
- ✅ Listar usuários (com filtros)
- ✅ Cadastrar usuário
- ✅ Buscar usuário por ID
- ✅ Buscar por email
- ✅ Buscar por nome
- ✅ Listar administradores
- ✅ Listar usuários comuns
- ✅ Editar usuário
- ✅ Excluir usuário
- ✅ Validações de campos (obrigatórios, vazios, formato)
- ✅ Testes de segurança (SQL Injection, XSS)

### Login (`/login`)
- ✅ Realizar login com sucesso
- ✅ Validar credenciais inválidas (email/senha)
- ✅ Validar campos obrigatórios
- ✅ Validar formato de email
- ✅ Validar campos vazios
- ✅ Extrair e validar token JWT

### Contratos (JSON Schema)
- ✅ Validação de schema de usuário
- ✅ Validação de schema de lista de usuários
- ✅ Validação de schema de login
- ✅ Validação de schema de produto
- ✅ Validação de campos obrigatórios
- ✅ Validação de tipos de dados

### Segurança
- ✅ Autenticação de endpoints protegidos
- ✅ Autorização (admin vs usuário comum)
- ✅ Proteção contra SQL Injection
- ✅ Proteção contra XSS
- ✅ Validação de tamanho de campos

## 🎯 Padrões de Design Implementados

### ⭐ Service Object Pattern (Page Object Model para APIs)
Implementação completa do padrão Service Object com classe base abstrata e serviços especializados.

**Arquitetura:**
```
BaseService (abstract)
    ├── UsuarioService (extends BaseService)
    ├── LoginService (extends BaseService)
    └── ProdutoService (extends BaseService)
```

### Builder Pattern
Os modelos utilizam Lombok `@Builder` para criação fluente de objetos.

### Data Factory Pattern
A classe `DataFactory` centraliza a criação de dados de teste utilizando JavaFaker.

### Test Fixtures (BeforeEach/AfterEach)
Gerenciamento automático de setup e cleanup de recursos de teste.

## 📖 Documentação e Recursos

### Documentação Externa
Este projeto foi desenvolvido seguindo as melhores práticas da documentação oficial:
- [RestAssured Documentation](https://rest-assured.io/)
- [RestAssured Usage Guide](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Allure Report](https://docs.qameta.io/allure/)
- [JSON Schema](https://json-schema.org/)

## 🎓 Conceitos e Boas Práticas Aplicadas

- ✅ **Service Object Pattern** - Encapsulamento de requisições HTTP
- ✅ **DRY (Don't Repeat Yourself)** - Métodos helper eliminam duplicação
- ✅ **Single Responsibility** - Cada service tem uma responsabilidade clara
- ✅ **Herança** - BaseService provê funcionalidades comuns
- ✅ **Composição** - Services podem ser combinados em testes complexos
- ✅ **Data-Driven Testing** - Testes parametrizados com JUnit
- ✅ **Contract Testing** - Validação com JSON Schema
- ✅ **Security Testing** - Testes de vulnerabilidades comuns
- ✅ **Test Fixtures** - Setup/teardown automático
- ✅ **Allure Reports** - Documentação visual dos testes


## 📄 Licença

Este projeto está sob a licença MIT.

## ✨ Autor
Anderson Mann (anderson.civil@hotmail.com)

Desenvolvido para fins de estudo e aprendizado de testes de API com RestAssured.

**Destaques do projeto:**
- 📐 Service Object Pattern implementado com arquitetura extensível
- 🔒 Testes de segurança (SQL Injection, XSS)
- 📋 Validação de contratos com JSON Schema
- 📈 Relatórios com Allure