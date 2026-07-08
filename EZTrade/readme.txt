EZTrade - Project Testing and Execution
=======================================

The EZTrade project can currently be tested in two ways:

1. Project Deployed on Amazon Web Services
------------------------------------------

The application is deployed and available for testing at the following URL:

https://main.d2uwr76rqmp27x.amplifyapp.com/

This link provides direct access to the deployed frontend without installing or starting the local environment.

2. Local Execution with Docker
------------------------------

In addition to the AWS deployment, the project can also be started locally with Docker.

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

If you only want to test the application as a user, the deployed AWS link is recommended. If you want to review, run, or modify the source code, follow the Docker documentation located at EZTrade/DOCKER.md.
