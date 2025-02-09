Documentação

Objetivo:

Essa app tem como objetivo realizar o middleware de conexão com o redis, sendo assim uma das 
validações necessárias para a app continuar de é a comunicação com o Redis.

##Subida do Redis

Temos atualmente tres formas de subir o redis, localmente atraves do comando 

Docker local:
docker run --name redis -d -p 6379:6379 redis redis-server --save 60 1 --loglevel warning

Rancher Local:
acesse na raiz do projeto o .kube
e execute 
kubectl apply -f redis-configmap.yaml --namespace=hackaton-soat7-2025
kubectl apply -f redis-deployment.yaml --namespace=hackaton-soat7-2025
kubectl apply -f redis-ingress.yml --namespace=hackaton-soat7-2025
kubectl apply -f redis-service.yaml --namespace=hackaton-soat7-2025

AWS utilizando o ElastiCache:

https://github.com/Lehhh/fiap-hack-soat7-terraform-redis.git

Acesse na raiz do projeto env/prod e execute
terraform plan
terraform apply

#Subida da App

##Gerar a imagem localmente

docker build -t redis-communication:1.0.0 .

## Executar o comando para rodar no docker local

docker run --name redis-communication -p 8080:8080 -e REDIS_HOST={IP_MAQUINA_LOCAL} -e REDIS_PORT=30079 redis-communication:1.0.0

Como ver o ip da sua maquina local no mac

ifconfig | grep "inet " | grep -v 127.0.0.1 | grep 'inet' | awk '{print $2}'

no linux:
hostname -I

## Subir a App no kubernetes
Acessar na raiz a pasta .kube/redis-communicaiton

kubectl apply -f service-app.yaml --namespace=hackaton-soat7-2025
kubectl apply -f deployment-app.yaml --namespace=hackaton-soat7-2025














