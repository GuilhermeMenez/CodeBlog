# 📝 CodeBlog - Plataforma Social de Blog para Programadores

[![Java](https://img.shields.io/badge/Java-21-ED8936?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

## 📋 Visão Geral

**CodeBlog** é uma plataforma de blog social escalável desenvolvida em **Java 21 com Spring Boot 3.4.5**, projetada para permitir que programadores compartilhem conteúdo técnico, comentem posts e interajam com outros usuários através de um sistema de seguimento (follow).

A aplicação implementa técnicas avançadas de paginação, otimização de performance com Redis, segurança moderna com JWT, e fornece uma **API RESTful robusta** com documentação automática via OpenAPI 3.0/Swagger.

## 🎯 Funcionalidades Principais

### 👥 Gestão de Usuários
- ✅ Registro e autenticação com JWT
- ✅ Atualização de perfil com foto (integração Cloudinary)
- ✅ Sistema de seguimento/unfollowing
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
├── controller/          # REST Controllers (endpoints)
│   ├── AutheticationController
│   ├── UserController
│   ├── PostController
│   ├── CommentController
│   └── CloudnaryController
├── service/            # Lógica de negócio
│   ├── UserService
│   ├── PostService
│   ├── CommentService
│   ├── AuthorizationService
│   └── CloudinaryService
├── repository/         # Acesso a dados (JPA)
│   ├── UserRepository
│   ├── PostRepository
│   ├── CommentRepository
│   └── UserFollowRepository
├── model/              # Entidades JPA
│   ├── User
│   ├── Post
│   ├── Comment
│   └── UserFollow
├── dto/                # Data Transfer Objects
│   ├── UserDTO
│   ├── PostDTO
│   ├── CommentDTO
│   └── AuthDTO
├── config/             # Configurações
│   ├── security/       # Security Filter, JWT Validation
│   ├── handlers/       # Global Exception Handler
│   ├── RedisConfig
│   └── SwaggerConfig
├── enums/              # Enumerações
├── error/              # Tratamento de erros personalizados
└── CodeBlogApplication # Classe principal

src/test/java/blog/code/codeblog/
├── controller/         # Testes dos controllers
├── service/            # Testes dos services
├── repository/         # Testes dos repositories
├── integration/        # Testes de integração
└── config/             # Testes de configuração
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
| POST | `/api/auth/register` | Registrar novo usuário |
| POST | `/api/auth/login` | Fazer login e obter JWT token |
| POST | `/api/auth/refresh` | Renovar token JWT |

### 👤 Usuários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/users/{id}` | Obter perfil do usuário |
| PUT | `/api/users/{id}` | Atualizar perfil |
| DELETE | `/api/users/{id}` | Deletar conta |
| GET | `/api/users/{id}/followers` | Listar seguidores |
| GET | `/api/users/{id}/following` | Listar seguindo |

### 📝 Posts
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/posts` | Feed balanceado (paginado) |
| GET | `/api/posts/{id}` | Obter post específico |
| POST | `/api/posts` | Criar novo post |
| PUT | `/api/posts/{id}` | Atualizar post próprio |
| DELETE | `/api/posts/{id}` | Deletar post próprio |
| GET | `/api/posts/user/{userId}` | Posts de um usuário |

### 💬 Comentários
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/comments/post/{postId}` | Listar comentários de um post |
| POST | `/api/comments` | Adicionar comentário |
| DELETE | `/api/comments/{id}` | Deletar comentário próprio |

### 🔗 Follow
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users/{id}/follow` | Seguir usuário |
| DELETE | `/api/users/{id}/unfollow` | Deixar de seguir usuário |

### 📸 Mídia
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/cloudinary/upload` | Upload de imagem |
| DELETE | `/api/cloudinary/{publicId}` | Deletar imagem |

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
```xml
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

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

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
- [ ] Implementar uso de docker
- [ ] Configurar CI/CD para diferentes ambientes
- [ ] Deploy em algum serviço de hospedagem
- [ ] Usar algum serviço de monitoramento (ex: New Relic, Datadog)
- [ ] Usar algum servico de logging (ex: Kibana, OpenSearch)

### 🛡️ Melhorias de Segurança
- [ ] 2FA (Two-Factor Authentication)
- [ ] Implementar RBAC (Role-Based Access Control)

---

*Última atualização: Março 2025*