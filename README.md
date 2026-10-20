# Receitas do Mundo

App Android em Kotlin com Jetpack Compose para descobrir receitas de vários países e guardar as favoritas na sua conta.

## API utilizada

**TheMealDB** — `https://www.themealdb.com/api/json/v1/1/` (pública, sem cadastro nem chave).

Usamos quatro rotas:

- `search.php?s=` para listar e buscar receitas pelo nome;
- `filter.php?c=` para filtrar por categoria;
- `lookup.php?i=` para abrir o detalhe com ingredientes e modo de preparo;
- `categories.php` para montar os filtros de categoria.

## Prints

(adicionar de 4 a 6 imagens: login, cadastro, lista, detalhe, favoritos, estado de erro)

## Divisão de tarefas

| Integrante | O que fez |
|------------|-----------|
| Lucas | Camada de dados (Room, repositórios, sessão e hash da senha), cadastro e login, tradução das receitas para português |
| Yslan | Telas em Compose (lista, detalhe, favoritos e perfil), navegação e testes do app |

## O que aprendemos

- Lucas: aprendi a separar o app em camadas (network, data, viewmodel, userinterface) e a guardar a senha com hash e a sessão em SharedPreferences.
- Yslan: aprendi a montar as telas em Jetpack Compose e a navegar entre elas com rotas type-safe.

## Organização do código

```
com.cotemig.receitas
 ├─ network        interface do Retrofit e a instância do Retrofit
 ├─ data           data classes, Room (Entity, Dao, Database) e Repositories
 ├─ viewmodel      ViewModels e seus estados
 └─ userinterface  telas Compose, navegação e MainActivity
```

- As telas só falam com os ViewModels; quem fala com o Retrofit e com o Room são os Repositories, em `data`.
- Os ViewModels não importam nada de `userinterface`.
- Cadastro e login ficam no `UsuarioRepository`. A senha é guardada como hash SHA-256 e a sessão é guardada em `SharedPreferences`.
- Os favoritos são salvos no Room com o `usuarioId`, então cada conta enxerga só os seus.

## O que o app faz

- Cadastro e login com validação (e-mail válido, senha com 6 ou mais caracteres, e-mail único).
- Continua logado ao fechar e abrir o app; o botão **Sair** volta para o login.
- Lista de receitas com busca por nome e filtro por categoria, com estados de carregando, sucesso e erro.
- Barra inferior com 3 abas (Receitas, Favoritos, Perfil) e tela de detalhe aberta por rota type-safe.
- Favoritos guardados no Room por usuário.

## Como rodar

Abrir a pasta no Android Studio, esperar o Gradle sincronizar e rodar no emulador (Android 8.0 ou superior).
