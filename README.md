# Desafio Backend - Requisitos

## 1. Validações

Você deve ajustar as entidades (model e sql) de acordo com as regras abaixo:

- `Product.name` é obrigatório, não pode ser vazio e deve ter no máximo 100 caracteres.
- `Product.description` é opcional e pode ter no máximo 255 caracteres.
- `Product.price` é obrigatório deve ser > 0.
- `Product.status` é obrigatório.
- `Product.category` é obrigatório.
- `Category.name` deve ter no máximo 100 caracteres.
- `Category.description` é opcional e pode ter no máximo 255 caracteres.

## 2. Otimização de Performance

- Analisar consultas para identificar possíveis gargalos.
- Utilizar índices e restrições de unicidade quando necessário.
- Implementar paginação nos endpoints para garantir a escala conforme o volume de dados crescer.
- Utilizar cache com `Redis` para o endpoint `/auth/context`, garantindo que a invalidação seja feita em caso de alteração dos dados.

## 3. Logging

- Registrar logs em arquivos utilizando um formato estruturado (ex.: JSON).
- Implementar níveis de log: DEBUG, INFO, WARNING, ERROR, CRITICAL.
- Utilizar logging assíncrono.
- Definir estratégias de retenção e compressão dos logs.

## 4. Refatoração

- Atualizar a entidade `Product`:
  - Alterar o atributo `code` para o tipo inteiro.
- Versionamento da API:
  - Manter o endpoint atual (v1) em `/api/products` com os códigos iniciados por `PROD-`.
  - Criar uma versão (v2) em `/api/v2/products` onde `code` é inteiro.

## 5. Integração com Swagger

- Documentar todos os endpoints com:
  - Descrições detalhadas.
  - Exemplos de JSON para requisições e respostas.
  - Listagem de códigos HTTP e mensagens de erro.

## 6. Autenticação e Gerenciamento de Usuários

- Criar a tabela `users` com as colunas:
  - `id` (chave primária com incremento automático)
  - `name` (obrigatório)
  - `email` (obrigatório, único e com formato válido)
  - `password` (obrigatório)
  - `role` (obrigatório e com valores permitidos: `admin` ou `user`)
- Inserir um usuário admin inicial:
  - Email: `contato@simplesdental.com`
  - Password: `KMbT%5wT*R!46i@@YHqx`
- Endpoints:
  - `POST /auth/login` - Realiza login.
  - `POST /auth/register` - Registra novos usuários (se permitido).
  - `GET /auth/context` - Retorna `id`, `email` e `role` do usuário autenticado.
  - `PUT /users/password` - Atualiza a senha do usuário autenticado.

## 7. Permissões e Controle de Acesso

- Usuários com `role` admin podem criar, alterar, consultar e excluir produtos, categorias e outros usuários.
- Usuários com `role` user podem:
  - Consultar produtos e categorias.
  - Atualizar apenas sua própria senha.
  - Não acessar ou alterar dados de outros usuários.

## 8. Testes

- Desenvolver testes unitários para os módulos de autenticação, autorização e operações CRUD.

---

## Rodando o projeto localmente

## `docker compose up`

# Perguntas

1. **Se tivesse a oportunidade de criar o projeto do zero ou refatorar o projeto atual, qual arquitetura você utilizaria e por quê?**

Utilizaria uma arquitetura baseada em microsserviços, pois facilitaria a escalabilidade, manutenção e permite que diferentes equipes trabalhem em serviços independentes. Cada serviço seria responsável por um domínio específico. Ex: autenticação, produtos e categorias.

2. **Qual é a melhor estratégia para garantir a escalabilidade do código mantendo o projeto organizado?**

Aplicar um design arquitetural de código (Ex: Clean Arch, arquitetura hexagonal), fazendo a separação por camadas: caso a escolha tenha sido Clean Arch, teríamos as camadas de regras de negócio (entities), regras de aplicação (use cases), adaptadores de interfaces (Controllers, Gateways) e de framework e drives. Isso iria garantir modularidade e facilitaria a evolução do código, permitindo também a troca de Banco de Dados ou até frameworks de forma que não impacte as regras de negócios e da aplicação, por exemplo.

3. **Quais estratégias poderiam ser utilizadas para implementar multitenancy no projeto?**

- Banco de dados compartilhado: utilizando uma coluna tenant_id em cada coluna. A implementação seria menos complexa mas o nível de isolamento poderia não ser o desejado.
- Banco de dados por tenant: Cada cliente teria seu próprio banco de dados, o que garantiria um isolamento total. Porém a implementação seria um pouco mais complexa.

Acredito que a escolha iria depender do nível de isolamento e complixadade desejados.

4. **Como garantir a resiliência e alta disponibilidade da API durante picos de tráfego e falhas de componentes?**

- Implementar balanceamento de carga e adotar escalabilidade horizontal (com auto-scaling) e vertical.
- Usar filas (ex.: RabbitMQ) para processar tarefas assíncronas.
- Configurar retries, fallbacks e circuit breakers com bibliotecas como Resilience4j.
- Replicar serviços em múltiplas zonas de disponibilidade.
- Utilizar cache distribuído para reduzir a latência e suportar picos de acesso.
- Monitorar a API com ferramentas de logging e observabilidade para identificar falhas rapidamente a atuar sobre elas.

5. **Quais práticas de segurança essenciais você implementaria para prevenir vulnerabilidades como injeção de SQL e XSS?**

- Validar e sanitizar entradas do usuário.
- Utilizar componentes de firewall (como AWS WAF).

6. **Qual a abordagem mais eficaz para estruturar o tratamento de exceções de negócio, garantindo um fluxo contínuo desde sua ocorrência até o retorno da API?**

- Utilizar um middleware para tratamento global de exceções, parecido com a classe GlobalExceptionHandler.java, mapeando as respostas HTTP apropriadas para cada erro.

7. **Considerando uma aplicação composta por múltiplos serviços, quais componentes você considera essenciais para assegurar sua robustez e eficiência?**

- API Gateway para roteamento e autenticação centralizada. Geralmente API Gateways também podem ser configurados como Load Balancers.
- Service Discovery para localização de serviços.
- Monitoriamento e Logging centralizados (DataDog, NewRelic, elastic stack).
- Diversos ambientes para testes: Development, QA, Prod...
- Estratégias de deploy como: Blue-Green, canary e utilização de feature toggles.

8. **Como você estruturaria uma pipeline de CI/CD para automação de testes e deploy, assegurando entregas contínuas e confiáveis?**

- Realizar testes unitários e de integração
- Analisar qualidade do código (SonarQube)
- Construir e versionar imagens do docker em repositórios (ex: ECR)
- Deploy em ambiente de Dev, QA e Prod (Prod poderia ser configurado um trigger manual).
- Ao deployar em Prod, utilizar estratégias de deploy mencionadas acima.
- Usar ferramentas como GitHub Actions, Jenkins ou GitLab CI.

Obs: Forneça apenas respostas textuais; não é necessário implementar as perguntas acima.
