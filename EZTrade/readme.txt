EZTrade - Prueba y ejecucion del proyecto
==========================================

El proyecto EZTrade puede probarse actualmente de dos formas:

1. Proyecto desplegado en Amazon Web Services
---------------------------------------------

La aplicacion se encuentra desplegada y disponible para pruebas en el siguiente enlace:

https://main.d2uwr76rqmp27x.amplifyapp.com/

Este enlace permite acceder directamente a la version desplegada del frontend sin necesidad de instalar ni levantar el entorno localmente.

2. Ejecucion local mediante Docker
----------------------------------

Ademas del despliegue en AWS, el proyecto tambien puede levantarse en local mediante Docker.

Dentro de la carpeta principal del proyecto se incluye el archivo:

EZTrade/docker-compose.yml

Este archivo permite levantar el entorno completo de desarrollo local, compuesto por:

- Base de datos MySQL.
- Backend desarrollado con Spring Boot.
- Frontend desarrollado con Next.js.

Para consultar todos los pasos detallados de instalacion, configuracion, arranque y parada del entorno Docker, se debe revisar la documentacion especifica incluida en:

EZTrade/DOCKER.md

En dicha documentacion se explica como ejecutar el proyecto con Docker Compose, que servicios se levantan, que puertos se utilizan y que variables de configuracion pueden modificarse mediante un archivo .env.

Resumen rapido de ejecucion local
---------------------------------

Desde la raiz del proyecto EZTrade, se puede levantar el entorno con:

docker compose up --build

Una vez iniciado, los servicios locales quedan accesibles normalmente en:

- Frontend: http://localhost:3000
- Backend: http://localhost:8088
- Base de datos MySQL: localhost:3306

Para detener el entorno:

docker compose down

Para eliminar tambien los datos locales de MySQL:

docker compose down -v

Nota
----

Si solo se quiere probar la aplicacion como usuario, se recomienda utilizar directamente el enlace desplegado en AWS. Si se quiere revisar, ejecutar o modificar el codigo fuente, se recomienda seguir la documentacion Docker ubicada en EZTrade/DOCKER.md.