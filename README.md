# Test API REST Assured - ServeRest

Testes automatizados para a API [ServeRest](https://serverest.dev) usando RestAssured e Java. O projeto usa alguns design patterns que ajudam a manter o código organizado e fácil de manter.

## Pré-requisitos

- Java 21
- Maven 3.6 ou superior

## Tecnologias

- **RestAssured 5.4.0** - para testes de API REST
- **JUnit 5.10.1** - framework de testes
- **Allure 2.25.0** - geração de relatórios (fica bem visual)
- **Jackson** - serialização/deserialização JSON
- **Lombok** - reduz bastante código boilerplate
- **AssertJ** - assertions mais legíveis
- **JavaFaker** - gera dados de teste aleatórios
- **JSON Schema Validator** - validação de contratos

## Estrutura do Projeto

```
test-api-rest-assured/
├── src/test/java/br/com/serverest/
│   ├── config/
│   │   └── BaseTest.java          # configuração base
│   ├── model/
│   │   ├── Usuario.java           
│   │   └── Login.java             
│   ├── service/
│   │   ├── BaseService.java       # classe abstrata com funcionalidades comuns
│   │   ├── UsuarioService.java    # 16 métodos
│   │   └── LoginService.java      # 10 métodos
│   ├── utils/
│   │   └── DataFactory.java       # gerador de dados de teste
│   └── tests/
│       ├── UsuariosTest.java      # 44 testes
│       ├── LoginTest.java         # 11 testes
│       ├── ContratoTest.java      # 28 testes de schema
│       └── SecurityTest.java      # 14 testes
└── src/test/resources/
    └── config.properties
```

## Configuração

O arquivo `config.properties` tem as configurações básicas:

```properties
base.uri=https://serverest.dev
base.path=/
connection.timeout=10000
socket.timeout=10000
enable.request.logging=true
enable.response.logging=true
```

## Instalação

Clone o repositório:
```bash
git clone git@github.com:andersonmann/api-automated-test-with-rest-assured.git
cd api-automated-test-with-rest-assured
mvn clean install
```

## Rodando os Testes

Todos os testes:
```bash
mvn clean test
```

Uma classe específica:
```bash
mvn test -Dtest=UsuariosTest
```

Um teste específico:
```bash
mvn test -Dtest=UsuariosTest#testCadastrarUsuario
```

Relatório Allure:
```bash
mvn allure:serve
```

## GitHub Actions

Para rodar manualmente:
1. Vá na aba Actions do repositório
2. Selecione o workflow "Run API Tests"
3. Clique em "Run workflow"
4. Escolha a branch e clique no botão verde

### Relatório Allure

O relatório fica publicado automaticamente no GitHub Pages:

**https://andersonmann.github.io/api-automated-test-with-rest-assured/**

> Observação: A API do ServeRest tem limite de requisições por minuto, então alguns testes podem falhar ocasionalmente por rate limit. É algo que notei durante os testes.

O relatório também fica disponível como artefato do workflow por 30 dias.

### Pipeline

A pipeline basicamente faz:
- Checkout do código
- Setup do Java 21 com cache do Maven
- Roda `mvn clean test`
- Gera o relatório Allure
- Faz upload do relatório

## Cobertura de Testes

| Classe | Testes | Descrição |
|--------|--------|-----------|
| **UsuariosTest** | 44 | CRUD, validações, filtros, segurança |
| **LoginTest** | 11 | Autenticação, validações de campos |
| **ContratoTest** | 28 | Validação de JSON Schema |
| **SecurityTest** | 14 | Autenticação, autorização, SQL Injection, XSS |

## Endpoints Testados

### Usuários (`/usuarios`)
- Listar usuários com filtros
- Cadastrar, buscar, editar e excluir
- Busca por email e nome
- Filtrar administradores e usuários comuns
- Validações de campos obrigatórios e formatos
- Testes de segurança (SQL Injection, XSS)

### Login (`/login`)
- Login com credenciais válidas e inválidas
- Validações de campos (obrigatórios, formato de email, vazios)
- Extração e validação de token JWT

### Contratos (JSON Schema)
Validação de schemas para:
- Usuário (individual e lista)
- Login
- Campos obrigatórios e tipos de dados

### Segurança
- Autenticação em endpoints protegidos
- Autorização (diferença entre admin e usuário comum)
- Proteção contra SQL Injection e XSS
- Validação de tamanho de campos

## Design Patterns

### Service Object Pattern
O projeto usa Service Objects (parecido com Page Object para APIs). Tem uma classe base abstrata `BaseService` que os outros services estendem:

```
BaseService (abstract)
    ├── UsuarioService
    ├── LoginService
```

Isso ajuda bastante a não repetir código e deixa os testes mais limpos.

### Builder Pattern
Os models usam `@Builder` do Lombok, então fica fácil criar objetos nos testes.

### Data Factory
`DataFactory` centraliza a criação de dados de teste com JavaFaker. Facilita quando precisa de vários usuários com dados diferentes.

### Test Fixtures
Usando `@BeforeEach` e `@AfterEach` para setup e cleanup.

## Referências

Documentação útil:
- [RestAssured](https://rest-assured.io/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Allure Report](https://docs.qameta.io/allure/)
- [JSON Schema](https://json-schema.org/)

## Conceitos Aplicados

**Service Object Pattern** - Encapsula as requisições HTTP, deixa os testes mais limpos

**DRY** - Métodos helper evitam repetição de código

**Single Responsibility** - Cada service cuida da sua parte

**Herança** - BaseService tem funcionalidades que todos os services usam

**Data-Driven Testing** - Testes parametrizados com JUnit

**Contract Testing** - Validação com JSON Schema

**Security Testing** - Testa vulnerabilidades comuns (SQL Injection, XSS)


## Licença

MIT

## Autor

Anderson Mann (anderson.civil@hotmail.com)

Projeto desenvolvido para estudar testes de API com RestAssured.