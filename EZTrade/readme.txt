EZTrade - Project Testing and Execution
=======================================

The EZTrade project is currently executable only as a local Docker Compose environment.

Execution with Docker
---------------------

The main project folder includes this file:

EZTrade/docker-compose.yml

This file starts the complete local development environment, composed of:

- MySQL database.
- Backend built with Spring Boot.
- Frontend built with Next.js.

For all detailed installation, configuration, startup, and shutdown steps for the Docker environment, review the specific documentation included at:

EZTrade/DOCKER.md

That documentation explains how to run the project with Docker Compose, which services are started, which ports are used, and which configuration variables can be changed through a `.env` file.

Quick Local Execution Summary
-----------------------------

From the EZTrade project root, start the environment with:

docker compose up --build

Once started, the local services are normally available at:

- Frontend: http://localhost:3000
- Backend: http://localhost:8088
- MySQL database: localhost:3306

To stop the environment:

docker compose down

To also delete local MySQL data:

docker compose down -v

Note
----

To test, review, run, or modify the application, follow the Docker documentation located at EZTrade/DOCKER.md.
