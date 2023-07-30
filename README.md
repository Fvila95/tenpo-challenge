# Tenpo Challenge

Este proyecto es una aplicación Java basada en Spring Boot que proporciona el calculo de la sumatoria de dos numeros mas un porcentaje obtenido de un servicio externo (en este caso mockeado) y recuperar los resultados de esos cálculos de forma paginada. Utiliza Postgres como base de datos, Redis para caché y MockServer para simular un servicio externo que devuelve el porcentaje.

## Índice
* [Primeros pasos](#primeros-pasos)
* [Colección de Postman](#colección-de-postman)
* [Endpoints de la API](#endpoints-de-la-api)
* [Construido con](#construido-con)

## Primeros pasos
Para tener una copia local en funcionamiento, sigue estos sencillos pasos:

1. Instalar docker y docker-compose.

2. Clonar el repositorio
```
git clone https://github.com/your_username/tenpo-challenge.git
```
3. Ejecutar el proyecto con docker-compose utilizando estos dos comandos.
```
docker-compose build
docker-compose up
```
Nota: dentro del docker-compose.yaml esta especificada la imagen del servicio que esta disponible publicamente en [dockerhub](https://hub.docker.com/layers/fvila31/tenpo-challenge/v1/images/sha256-3e43690abae7159ec4a85c1a4bf453dbb3e863e5c97f3c99b340dec6d9cefd2d?context=repo).

## Colección de Postman
Este proyecto incluye una colección de Postman que contiene ejemplos de llamadas a los endpoints de la API. Podes encontrar la colección en la carpeta `postman-collection`. Para usarla, sigue estos pasos:

1. Abrir Postman
2. Haz clic en "Importar"
3. Elige "Importar desde archivo" y navega hasta el archivo de la colección en la carpeta `postman-collection`
4. Haz clic en "Importar"

Ahora deberías ver la colección de Postman en tu lista de colecciones y podrás usarla para probar los endpoints de la API.

## Endpoints de la API
Esta aplicación incluye los siguientes puntos finales:

1. POST /tenpo-challenge/api/v1/calculator/sum-with-percentage: Este endpoint acepta un objecto con dos numeros como entrada (Ejemplo: {"firstNumber": 5, "secondNumber": 5}) y devuelve la suma de ambos numeros mas el porcentaje aplicado de ellos. En caso de cualquier error, devuelve una respuesta HTTP 400 con un mensaje de error.

2. GET /tenpo-challenge/api/v1/calculator/history: Este endpoint devuelve una lista paginada de todos los porcentajes calculados previamente. Puedes controlar la paginación con los parámetros de consulta 'page' y 'size'.

## Construido con 
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Reactor Core](https://projectreactor.io/)
* [Docker](https://www.docker.com/)
* [Docker Compose](https://docs.docker.com/compose/)
* [Postgres](https://www.postgresql.org/)
* [Redis](https://redis.io/)
* [MockServer](https://www.mock-server.com/)
