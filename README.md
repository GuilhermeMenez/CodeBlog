# 📝 CodeBlog - Plataforma Social de Blog para Programadores

[![Java](https://img.shields.io/badge/Java-21-ED8936?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)]

## 📋 Visão Geral

**CodeBlog** é uma plataforma de blog social escalável desenvolvida em **Java 21 com Spring Boot 3.4.5**, projetada para permitir que programadores compartilhem conteúdo técnico, comentem posts e sigam uns aos outros.

A aplicação implementa técnicas de paginação, otimização de performance com Redis, segurança com JWT, e fornece uma **API RESTful** com documentação automática via OpenAPI 3.0/Swagger.

## 🎯 Funcionalidades Principais

### 👥 Gestão de Usuários
- ✅ Registro e autenticação com JWT ou Auth0 via OTP
- ✅ Atualização de foto de perfil (integração Cloudinary)
- ✅ Sistema de seguimento
- ✅ Visualização de seguidores e de quem o usuário segue
- ✅ Exclusão de conta

### 📝 Posts
- ✅ Criação de posts com conteúdo textual e imagens
- ✅ Edição e exclusão de posts próprios
- ✅ Feed personalizado com balanceamento entre posts de quem você segue e que não segue
- ✅ Visualização de posts por usuário

### 💬 Comentários
- ✅ Adicionar comentários em posts
- ✅ Visualização de comentários paginados
- ✅ Exclusão de comentários próprios

---

## 🛠️ Stack Tecnológico

### **Backend Framework**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.4.5 | Framework principal |
| Spring Data JPA | - | ORM e persistência |
| Spring Web MVC | - | APIs RESTful |
| Lombok | 1.18.32 | Redução de boilerplate |

### **Segurança & Autenticação**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| Spring Security | 6.4.10 | Autenticação e autorização |
| JWT (JJWT) | 0.12.6 | Tokens estateless |
| Auth0 JWT | 4.5.0 | Validação JWT alternativa |
| Nimbus JOSE+JWT | 10.3 | Suporte JOSE |
| OAuth2 Resource Server | - | Validação OAuth2 |

### **Banco de Dados**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| PostgreSQL | 42.7.3 | Banco principal (produção) |
| H2 | 2.3.232 | Banco em memória (testes) |
| Hibernate | - | ORM mapeamento |

### **Cache & Performance**
| Tecnologia | - | Propósito |
|-----------|--------|---------|
| Spring Data Redis | - | Cache distribuído |
| Redis | - | Cache em memória |

### **Integração & APIs**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| Cloudinary | 2.0.0 | Upload e hospedagem de imagens |
| SpringDoc OpenAPI | 2.8.14 | Documentação automática |
| Swagger UI | - | Interface de documentação |

### **Validação & Logging**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| Hibernate Validator | 8.0.0 | Validação de dados |
| Jakarta Validation | 3.0.2 | Especificação de validação |
| Logback | 1.5.25 | Logging estruturado |
| SLF4J | - | Abstração de logging |

### **Testing**
| Tecnologia | Versão | Propósito |
|-----------|--------|---------|
| JUnit 5 | - | Framework de testes |
| Mockito | - | Mocking de dependências |
| AssertJ | 3.27.7 | Assertions fluentes |
| Spring Boot Test | - | Utilitários de teste |
| Testcontainers | 1.21.4 | Redis efêmero para os testes de integração |

#### Executando os testes

```bash
./mvnw test
```

Os testes de integração precisam de um Redis. Eles o resolvem sozinhos, nesta ordem:

1. **Redis externo**, se você indicar um — use isto em máquinas sem Docker:
   ```bash
   ./mvnw test -Dredis.test.host=localhost -Dredis.test.port=6379
   # ou, via variáveis de ambiente: REDIS_TEST_HOST / REDIS_TEST_PORT
   ```
   Um `redis-server` instalado localmente (`apt install redis-server`, `brew install redis`) já serve.
   Os testes apagam apenas as chaves que eles mesmos criam (`integration:test:*` e os prefixos dos
   caches), nunca um `FLUSHDB` — mas ainda assim prefira uma instância descartável.

2. **Testcontainers**, se houver Docker: sobe um `redis:7-alpine` efêmero automaticamente. Nada a fazer.

3. **Nenhum dos dois**: as classes que dependem de Redis são puladas com uma mensagem explicando
   como habilitá-las, e o restante da suíte (a maioria dos testes) roda normalmente.

---

## 🏗️ Arquitetura

### Padrão em Camadas
```
Controller (REST Endpoints) 
    ↓ (requisições HTTP)
Service (Lógica de Negócio)
    ↓ (regras de negócio)
Repository (Acesso a Dados)
    ↓ (queries JPA/SQL)
Banco de Dados (PostgreSQL/H2)
```

### Estrutura de Diretórios
```
src/main/java/blog/code/codeblog/
├── command/             # Objetos auxiliares para comandos de aplicação
├── config/              # Configurações da aplicação
│   ├── security/        # Segurança, filtros e autenticação
│   └── handlers/        # Tratamento global de exceções
├── controller/          # REST Controllers (endpoints)
│   ├── AutheticationController
│   ├── UserController
│   ├── PostController
│   ├── CommentController
│   └── CloudinaryController
├── dto/                 # Data Transfer Objects
│   ├── authentication/
│   ├── cloudinary/
│   ├── comment/
│   ├── follow/
│   ├── post/
│   └── user/
├── facade/              # Camada de orquestração entre controller e service
├── mapper/              # Conversores entre entidades e DTOs
├── model/               # Entidades JPA
│   ├── User
│   ├── Post
│   ├── Comment
│   └── UserFollow
├── repository/         # Acesso a dados (JPA)
│   ├── UserRepository
│   ├── PostRepository
│   ├── CommentRepository
│   └── UserFollowRepository
├── service/            # Lógica de negócio
│   ├── integration/
│   ├── interfaces/
│   ├── provider/
│   ├── AuthenticationService
│   ├── AuthorizationService
│   ├── CloudinaryService
│   ├── CommentService
│   ├── FeedService
│   ├── PostService
│   ├── TokenService
│   └── UserService
├── enums/              # Enumerações
├── error/              # Tratamento de erros personalizados
├── execptions/         # Exceções específicas do projeto
└── CodeBlogApplication # Classe principal

src/test/java/blog/code/codeblog/
├── config/
│   ├── handlers/       # Testes do handler global
│   └── security/       # Testes de segurança
├── controller/         # Testes dos controllers
├── integration/        # Testes de integração
├── mapper/             # Testes dos mappers
├── repository/         # Testes dos repositories
└── service/            # Testes dos services
```

---

## 🚀 Iniciando o Projeto

### Pré-requisitos
- **Java 21** ou superior
- **Maven 3.6+**
- **PostgreSQL 14+** (para desenvolvimento)
- **Redis** (para cache)
- **Cloudinary Account** (para upload de imagens)

### Variáveis de Ambiente

Configurar as variáveis de ambiente conforme abaixo (exemplo para desenvolvimento local):

```env
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/codeblog
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_TIMEOUT=60000ms

# JWT
JWT_SECRET=sua_chave_secreta_muito_segura_aqui_minimo_256_bits
JWT_EXPIRATION=86400000

# Cloudinary
CLOUDINARY_CLOUD_NAME=seu_cloud_name
CLOUDINARY_API_KEY=sua_api_key
CLOUDINARY_API_SECRET=sua_api_secret

# Perfil
SPRING_PROFILES_ACTIVE=local
```

### Instalação e Execução

#### 1️⃣ Clonar o repositório
```bash
git clone https://github.com/seu-usuario/CodeBlog.git
cd CodeBlog
```

#### 2️⃣ Instalar dependências
```bash
mvn clean install
```

#### 3️⃣ Executar a aplicação
```bash
mvn spring-boot:run
```

```

A aplicação estará disponível em: `http://localhost:8080`

#### 4️⃣ Acessar a documentação da API
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
```

---

## 📚 Endpoints Principais

### 🔐 Autenticação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/register` | Registrar novo usuário |
| POST | `/auth/login` | Fazer login e obter JWT token |
| POST | `/auth/logout` | Encerrar sessão atual |
| POST | `/auth/otp/send` | Enviar OTP para autenticação passwordless |
| POST | `/auth/otp/verify` | Validar OTP recebido |

### 👤 Usuários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/user/me` | Obter perfil do usuário logado |
| GET | `/user/{id}` | Obter perfil de um usuário pelo ID |
| PUT | `/user/edit` | Atualizar perfil do usuário logado |
| DELETE | `/user` | Deletar conta do usuário logado |
| GET | `/user/{id}/followers` | Listar seguidores |
| GET | `/user/{id}/following` | Listar seguindo |
| POST | `/user/follow` | Seguir usuário |
| POST | `/user/unfollow` | Deixar de seguir usuário |

### 📝 Posts
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/post/posts` | Listar todos os posts |
| GET | `/post/{id}` | Obter post específico |
| GET | `/post/users/{userId}/feed` | Feed balanceado (paginado) |
| GET | `/post/userPosts/{id}` | Posts de um usuário |
| POST | `/post/newpost` | Criar novo post |
| PUT | `/post/edit` | Atualizar post próprio |
| DELETE | `/post/{id}` | Deletar post próprio |
| GET | `/post/{id}/comments` | Listar comentários de um post |
| POST | `/post/upload` | Upload de imagem para um post |

### 💬 Comentários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/comment/create` | Adicionar comentário |
| PUT | `/comment/update/{id}` | Atualizar comentário |
| DELETE | `/comment/delete/{id}` | Deletar comentário |

### 📸 Mídia
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/image/upload` | Upload de imagem |
| DELETE | `/image/delete` | Deletar imagem |

> Observação: os endpoints acima refletem os `@RequestMapping`/`@GetMapping`/`@PostMapping` reais dos controllers atuais. Em alguns pontos, o projeto usa nomes com typos históricos, como `AutheticationController` e `execptions/`.

---

## 🔒 Segurança

### Implementado
✅ **Autenticação JWT**: Tokens estateless validados em cada requisição  
✅ **Autorização**: Baseada em roles e validação de proprietário  
✅ **Validação de Entrada**: Hibernate Validator + Jakarta Validation  
✅ **Proteção contra CVEs**: Override de dependências vulneráveis  
✅ **Senhas Criptografadas**: BCrypt via Spring Security  
✅ **CORS**: Configurado para ambientes de desenvolvimento e produção  
✅ **Global Exception Handler**: Mensagens de erro seguras e consistentes

### Versões Seguras Override
```text
Spring Framework: 6.2.13
Spring Security: 6.4.10
Tomcat: 10.1.47 (corrige CVE-2025-31651)
Logback: 1.5.25 (corrige CVE-2026-1225)
AssertJ: 3.27.7 (corrige CVE-2026-24400)
```

---

## 📊 Padrões de Resposta da API

### ✅ Sucesso (200 OK)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "João Silva",
  "email": "joao@example.com",
  "photo": "https://cloudinary.com/...",
  "followersCount": 45,
  "followingCount": 28,
  "createdAt": "2024-03-17T10:30:00Z"
}
```

### 📄 Paginação (200 OK)
```json
{
  "content": [
    { "id": "...", "title": "Como usar Spring Security", "content": "..." },
    { "id": "...", "title": "JWT Best Practices", "content": "..." }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

### ❌ Erro (4xx/5xx)
```json
{
  "timestamp": "2024-03-17T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/users/550e8400-e29b-41d4-a716-446655440000"
}
```

### ⚠️ Validação (422 Unprocessable Entity)
```json
{
  "timestamp": "2024-03-17T10:30:00Z",
  "status": 422,
  "error": "Validation Error",
  "message": "Validation failed for object='userDTO'",
  "errors": {
    "email": "must be a valid email address",
    "name": "size must be between 3 and 100"
  }
}
```

---

## 🔧 Técnicas Implementadas

### 1️⃣ **Feed Balanceado com Shuffle Determinístico**
- Algoritmo de seed baseado em timestamp para aleatoriedade consistente
- Cache inteligente com Redis para performance
- Limite máximo de posts configurável por página
- Filtro temporal (últimos 7 dias) para posts sem seguidores

### 2️⃣ **Paginação Otimizada**
- **Offset-based Pagination**: Implementado com Spring Data `Pageable`
- **Cursor-based Pagination**: Documentado como proposta futura
- Cache em Redis reduz queries ao banco
- TTL (Time To Live) configurável

### 3️⃣ **Segurança Avançada**
- Custom SecurityFilter para validação JWT por requisição
- CustomAuthenticationEntryPoint para tratamento de erros de autenticação
- Override de dependências vulneráveis
- Validação em múltiplas camadas

### 4️⃣ **Arquitetura Robusta**
- Padrão de camadas bem definido (Controller → Service → Repository)
- DTOs para separação entre camadas
- Global Exception Handler centralizado
- Logging estruturado com SLF4J/Logback

### 5️⃣ **Persistência Flexível**
- JPA/Hibernate com suporte a múltiplos bancos
- PostgreSQL para produção
- H2 em memória para testes
- Migrations automáticas com Hibernate DDL

### 6️⃣ **Testing**
- Testes unitários com Mockito
- Testes de integração com Spring Boot Test Context
- Testes de repository com H2
- Cobertura de controllers, services e repositories

---


## 📝 Licença

Este projeto está sob a licença MIT.

---

## 👨‍💻 Autor

Desenvolvido por **Guilherme Menezes**

- 📧 Email: guilhermemenezestav@gmail.com
- 💼 LinkedIn: [Guilherme Menezes](https://www.linkedin.com/in/guilherme-menezes-tavares/)
- 🐙 GitHub: [GuilhermeMenez](https://github.com/GuilhermeMenez)

---

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório ou entre em contato através do email.

---

## 🎓 Aprendizados e Melhorias Futuras

### ✨ Próximas Features
- [ ] Sistema de notificações
- [ ] Busca avançada com ElasticSearch
- [ ] Implementação do uso de IA para auxiliar na criação dos posts

### 🔍 Melhorias infraestruturais
- [ ] Usar algum serviço de monitoramento (ex: New Relic, Datadog)
- [ ] Usar algum servico de logging (ex: Kibana, OpenSearch)

### 🛡️ Melhorias de Segurança
- [ ] 2FA (Two-Factor Authentication)
- [ ] Implementar RBAC (Role-Based Access Control)

---

*Última atualização: Março 2025*