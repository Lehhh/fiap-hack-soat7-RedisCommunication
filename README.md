# Projeto Redis Communication

## Descrição
Este projeto é uma aplicação Spring Boot que se comunica com um servidor Redis. Ele utiliza Kubernetes para orquestração de contêineres.

## Pré-requisitos
- Java 11+
- Maven
- Docker
- Kubernetes

## Configuração

### Variáveis de Ambiente
As seguintes variáveis de ambiente precisam ser configuradas para a aplicação funcionar corretamente:

- `REDIS_HOST`: O host do servidor Redis. No ambiente Kubernetes, isso é configurado como `redis-service`.
- `REDIS_PORT`: A porta do servidor Redis. No ambiente Kubernetes, isso é configurado como `6379`.

### Arquivos de Configuração
- `src/main/resources/application.yml`: Configurações do Spring Boot.
- `.kube/redis/redis-configmap.yaml`: Configurações do Redis.
- `.kube/redis/redis-ingress.yml`: Configurações de Ingress do Redis.
- `.kube/redis-communication/deployment-app.yaml`: Configurações de Deployment da aplicação.

## Como Executar

### Localmente
1. Compile o projeto:
    ```sh
    mvn clean install
    ```
2. Execute a aplicação:
    ```sh
    java -jar target/redis-communication-1.0.0.jar
    ```

### No Kubernetes
1. Crie os ConfigMaps e Deployments:
    ```sh
    kubectl apply -f .kube/redis/redis-configmap.yaml
    kubectl apply -f .kube/redis-communication/deployment-app.yaml
    kubectl apply -f .kube/redis/redis-ingress.yml
    ```

## Licença
Este projeto está licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.