# Diagramas DevOps e infraestructura

Esta seccion documenta automatizacion, tiempo de ejecucion local, configuracion, observabilidad y limites de infraestructura. La evidencia procede de `../.github/workflows/maven.yml`, `.env.example`, `application.properties`, `backend/pom.xml` y la busqueda de artefactos Docker/IaC/proxy.

## Vista general del pipeline CI/CD

[![Vista general del pipeline CI/CD](./rendered/cicd-pipeline-overview.png)](./rendered/cicd-pipeline-overview.svg)

**Proposito.** Representar el flujo de trabajo real de GitHub Actions.

**Como leerlo.** El pipeline se activa en `push` y `pull_request` contra `main`, prepara JDK 23 Oracle con cache Maven y ejecuta `mvn -B clean verify` en `EZTrade/backend`.

**Valor.** Da trazabilidad de la automatizacion existente sin exagerar su alcance.

**Limitacion.** No hay compilacion/lint/pruebas de frontend, construccion Docker, publicacion de imagen ni despliegue.

## Flujo de compilacion, pruebas y empaquetado

[![Flujo de compilacion, pruebas y empaquetado](./rendered/build-test-package-flow.png)](./rendered/build-test-package-flow.svg)

**Proposito.** Descomponer el flujo Maven verificado por CI.

**Como leerlo.** `clean verify` compila, ejecuta pruebas y verifica arquitectura. El plugin de Spring Boot esta presente, pero no hay fase de publicacion o despliegue.

**Valor.** Explica el nivel real de garantia automatizada en backend.

## Topologia local en tiempo de ejecucion

[![Topologia local en tiempo de ejecucion](./rendered/runtime-topology-local.png)](./rendered/runtime-topology-local.svg)

**Proposito.** Mostrar la topologia local inferida desde configuracion.

**Como leerlo.** Next.js consume Spring Boot en `localhost:8088`; Spring Boot usa MySQL local, Alpha Vantage, cache Caffeine y access logs.

**Valor.** Es util para memoria y onboarding porque explica como se comunican los procesos en desarrollo.

**Limitacion.** No hay Compose ni manifiestos; la topologia no representa contenedores de aplicacion.

## Evidencia de contenedorizacion Docker

[![Evidencia de contenedorizacion Docker](./rendered/docker-containerization-evidence.png)](./rendered/docker-containerization-evidence.svg)

**Proposito.** Distinguir entre Docker como herramienta documental y Docker como tiempo de ejecucion de aplicacion.

**Como leerlo.** La parte izquierda marca artefactos no encontrados; la derecha muestra los scripts creados para renderizar PlantUML con Docker.

**Valor.** Es una pieza honesta de arquitectura DevOps: evita presentar una contenedorizacion que el repositorio no tiene.

## Gestion de configuracion y secretos

[![Gestion de configuracion y secretos](./rendered/configuration-secrets-management.png)](./rendered/configuration-secrets-management.svg)

**Proposito.** Representar variables de entorno, defaults y consumidores.

**Como leerlo.** `.env.example` documenta variables; `application.properties` define placeholders/defaults; frontend lee `NEXT_PUBLIC_*`.

**Valor.** Ayuda a discutir seguridad de configuracion y preparacion para entornos.

**Limitacion.** No hay vault, secretos de entorno CI documentados ni gestion declarativa de secretos productivos.

## Observabilidad y logging

[![Observabilidad y logging](./rendered/observability-logging.png)](./rendered/observability-logging.svg)

**Proposito.** Mostrar senales operativas presentes.

**Como leerlo.** Hay logs de acceso de Tomcat, registro de peticiones con payload/query, SLF4J y dependencias Actuator/Modulith observability.

**Valor.** Permite explicar que existe una base de observabilidad local, pero no una plataforma completa.

**Limitacion.** No se ha encontrado Prometheus, trazas distribuidas, envio centralizado de logs, paneles ni alertas.

## Conclusion

La dimension DevOps actual es real pero inicial: CI backend solido, configuracion local documentada y logs basicos; falta contenedorizacion de aplicacion, despliegue, frontend CI y observabilidad productiva. Esta lectura es valiosa porque convierte los huecos en trabajo tecnico pendiente y justificable.
