# Diagramas de ingenieria de EZTrade

Esta carpeta contiene una documentacion visual de ingenieria inversa del proyecto EZTrade. Los diagramas se han derivado del codigo fuente, configuracion, pruebas y flujo de trabajo CI existentes; cuando no hay evidencia suficiente, la documentacion lo indica de forma explicita.

## Estructura

```text
docs/engineering-diagrams/
  00-inventory/
    REPOSITORY-REVERSE-ENGINEERING-INVENTORY.md
    DIAGRAM-ROADMAP.md
  01-architecture/
    plantuml/
    rendered/
    ARCHITECTURE-DIAGRAMS.md
  02-backend/
    plantuml/
    rendered/
    BACKEND-DIAGRAMS.md
  03-frontend/
    plantuml/
    rendered/
    FRONTEND-DIAGRAMS.md
  04-database/
    plantuml/
    rendered/
    DATABASE-DIAGRAMS.md
  05-sequence/
    plantuml/
    rendered/
    SEQUENCE-DIAGRAMS.md
  06-devops-infrastructure/
    plantuml/
    rendered/
    DEVOPS-INFRASTRUCTURE-DIAGRAMS.md
  07-quality-operations/
    plantuml/
    rendered/
    QUALITY-OPERATIONS-DIAGRAMS.md
  common/
    eztrade-style.puml
  scripts/
    render-diagrams.sh
    render-diagrams.ps1
```

## Punto de entrada recomendado

1. [Inventario tecnico](./00-inventory/REPOSITORY-REVERSE-ENGINEERING-INVENTORY.md)
2. [Roadmap de diagramas](./00-inventory/DIAGRAM-ROADMAP.md)
3. [Arquitectura](./01-architecture/ARCHITECTURE-DIAGRAMS.md)
4. [Backend](./02-backend/BACKEND-DIAGRAMS.md)
5. [Frontend](./03-frontend/FRONTEND-DIAGRAMS.md)
6. [Base de datos](./04-database/DATABASE-DIAGRAMS.md)
7. [Secuencias](./05-sequence/SEQUENCE-DIAGRAMS.md)
8. [DevOps e infraestructura](./06-devops-infrastructure/DEVOPS-INFRASTRUCTURE-DIAGRAMS.md)
9. [Calidad y operacion](./07-quality-operations/QUALITY-OPERATIONS-DIAGRAMS.md)

## Regenerar diagramas

Los diagramas se renderizan con Docker usando la imagen `plantuml/plantuml:latest`. No se requiere una instalacion local de PlantUML.

Desde la raiz del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File docs\engineering-diagrams\scripts\render-diagrams.ps1
```

En Bash:

```bash
./docs/engineering-diagrams/scripts/render-diagrams.sh
```

La convencion del script es simple: cada carpeta `plantuml/` se renderiza hacia su carpeta hermana `rendered/`, generando un PNG y un SVG por cada `.puml`.

## Convencion PNG -> SVG

Los documentos Markdown usan esta forma:

```markdown
[![Nombre del diagrama](./rendered/nombre-diagrama.png)](./rendered/nombre-diagrama.svg)
```

En GitHub se ve el PNG incrustado y, al hacer clic, se abre el SVG para inspeccion con mayor calidad.

## Notas de evidencia

- La arquitectura backend se basa en `backend/src/main/java`, `backend/pom.xml`, `application.properties` y los `package-info.java` de Spring Modulith.
- La arquitectura frontend se basa en `frontend/app`, `frontend/features`, `frontend/components` y `frontend/lib`.
- La base de datos se infiere desde entidades JPA; no hay migraciones Flyway/Liquibase.
- La parte DevOps se basa en `../.github/workflows/maven.yml`, `.env.example` y la busqueda explicita de artefactos Docker/IaC/proxy.
- No se han inventado Dockerfiles, entornos de produccion, reverse proxies ni FKs fisicas no presentes en el repositorio.
