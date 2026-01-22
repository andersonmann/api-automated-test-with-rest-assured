# Test API REST Assured - ServeRest

Projeto de testes automatizados para a API [ServeRest](https://serverest.dev) utilizando RestAssured e Java.

## 📋 Pré-requisitos

- Java 11 ou superior
- Maven 3.6 ou superior

## 🚀 Tecnologias Utilizadas

- **RestAssured 5.4.0** - Framework para testes de API REST
- **JUnit 5** - Framework de testes
- **Jackson** - Serialização/Deserialização JSON
- **Lombok** - Redução de código boilerplate
- **AssertJ** - Assertions fluentes
- **JavaFaker** - Geração de dados de teste

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
│       │       │   └── Produto.java           # Modelo de produto
│       │       ├── service/
│       │       │   ├── UsuarioService.java    # Serviço de usuários
│       │       │   ├── LoginService.java      # Serviço de login
│       │       │   └── ProdutoService.java    # Serviço de produtos
│       │       ├── utils/
│       │       │   └── DataFactory.java       # Factory para dados de teste
│       │       └── tests/
│       │           ├── UsuariosTest.java      # Testes de usuários
│       │           ├── LoginTest.java         # Testes de login
│       │           └── ProdutosTest.java      # Testes de produtos
│       └── resources/
│           └── config.properties              # Configurações da API
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
git clone <seu-repositorio>
cd test-api-rest-assured
```

2. Instale as dependências:
```bash
mvn clean install
```

## ▶️ Executando os Testes

### Executar todos os testes:
```bash
mvn test
```

### Executar uma classe de teste específica:
```bash
mvn test -Dtest=UsuariosTest
```

### Executar um teste específico:
```bash
mvn test -Dtest=UsuariosTest#testCadastrarUsuario
```

## 📝 Endpoints Testados

### Usuários (`/usuarios`)
- ✅ Listar usuários
- ✅ Cadastrar usuário
- ✅ Buscar usuário por ID
- ✅ Editar usuário
- ✅ Excluir usuário

### Login (`/login`)
- ✅ Realizar login com sucesso
- ✅ Validar credenciais inválidas
- ✅ Validar campos obrigatórios

### Produtos (`/produtos`)
- ✅ Listar produtos
- ✅ Cadastrar produto (requer autenticação)
- ✅ Buscar produto por ID
- ✅ Editar produto (requer autenticação)
- ✅ Excluir produto (requer autenticação)

## 🎯 Padrões Utilizados

### Page Object Pattern
Os serviços (`UsuarioService`, `LoginService`, `ProdutoService`) encapsulam as requisições HTTP, facilitando a manutenção e reutilização.

### Builder Pattern
Os modelos utilizam Lombok `@Builder` para criação fluente de objetos.

### Data Factory Pattern
A classe `DataFactory` centraliza a criação de dados de teste utilizando JavaFaker.

## 📊 Exemplo de Teste

```java
@Test
@DisplayName("Deve cadastrar um novo usuário com sucesso")
public void testCadastrarUsuario() {
    Usuario usuario = DataFactory.criarUsuarioValido(false);
    
    Response response = usuarioService.cadastrarUsuario(usuario);
    
    response.then()
            .statusCode(201)
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .body("_id", notNullValue());
}
```

## 📖 Documentação RestAssured

Este projeto foi desenvolvido seguindo as melhores práticas da documentação oficial do RestAssured:
- [RestAssured Documentation](https://rest-assured.io/)
- [RestAssured Usage Guide](https://github.com/rest-assured/rest-assured/wiki/Usage)

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/NovaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.

## ✨ Autor

Desenvolvido para fins de estudo e aprendizado de testes de API com RestAssured.
