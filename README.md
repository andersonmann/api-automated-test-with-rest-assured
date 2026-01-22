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
mvn test -Dtest=ServiceObjectPatternTest
```

### Executar um teste específico:
```bash
mvn test -Dtest=UsuariosTest#testCadastrarUsuario
```

### Gerar relatório Allure:
```bash
mvn clean test
allure serve target/allure-results
```

## 📊 Cobertura de Testes

| Classe de Teste | Testes | Descrição |
|----------------|--------|-----------|
| **UsuariosTest** | 44 | CRUD, validações, filtros, segurança |
| **LoginTest** | 11 | Autenticação, validações de campos |
| **ContratoTest** | 34 | Validação de JSON Schema |
| **SecurityTest** | 12 | Autenticação, autorização, SQL Injection, XSS |
| **ServiceObjectPatternTest** | 7 | Demonstração do padrão implementado |
| **TOTAL** | **108** | **100% de sucesso** |

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

### Produtos (`/produtos`)
- ✅ Listar produtos
- ✅ Cadastrar produto (requer autenticação)
- ✅ Buscar produto por ID
- ✅ Editar produto (requer autenticação)
- ✅ Excluir produto (requer autenticação)

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

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/NovaFeature`)
5. Abra um Pull Request

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