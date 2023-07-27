# Tenpo Challenge

Este proyecto es una aplicación Java basada en Spring Boot que proporciona el calculo de la sumatoria de dos numeros mas un porcentaje obtenido de un servicio externo (en este caso mockeado) y recuperar los resultados de esos cálculos de forma paginada. Utiliza Postgres como base de datos, Redis para caché y MockServer para simular un servicio externo que devuelve el porcentaje.

## Índice
* [Primeros pasos](#primeros-pasos)
* [Endpoints de la API](#endpoints-de-la-api)
* [Docker Compose](#docker-compose)
* [Construido con](#construido-con)

## Primeros pasos
Para tener una copia local en funcionamiento, sigue estos sencillos pasos:

1. Clonar el repositorio
```
git clone https://github.com/Fvila95/tenpo-challenge.git
```
2. Construir el proyecto
```
mvn clean install
```
3. Ejecutar la aplicación.
```
mvn spring-boot:run
```

## Endpoints de la API
Esta aplicación incluye los siguientes puntos finales:

1. POST /tenpo-challenge/api/v1/calculation: Este endpoint acepta dos parámetros como entrada, 'firstNumber' y 'secondNumber', y devuelve el porcentaje del primer número con respecto al segundo. En caso de cualquier error, devuelve una respuesta HTTP 400 con un mensaje de error.

2. GET /tenpo-challenge/api/v1/calculation: Este endpoint devuelve una lista paginada de todos los porcentajes calculados previamente. Puedes controlar la paginación con los parámetros de consulta 'page' y 'size'.

## Docker Compose

Este proyecto utiliza Docker Compose para manejar sus servicios. Aquí está la configuración:

```
services:
  postgres:
    image: 'postgres:latest'
    environment:
      - 'POSTGRES_DB=mydatabase'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=myuser'
    ports:
      - '5432:5432'
  mockserver:
    image: mockserver/mockserver
    ports:
      - 1080:1080
    command: -logLevel INFO
  redis:
    image: 'redis:latest'
    ports:
      - '6379:6379'
```

Para ejecutar los servicios con Docker Compose, simplemente ejecuta:

```
docker-compose up
```

## Construido con 
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Reactor Core](https://projectreactor.io/)
* [Docker Compose](https://docs.docker.com/compose/)
* [Postgres](https://www.postgresql.org/)
* [Redis](https://redis.io/)
* [MockServer](https://www.mock-server.com/)
