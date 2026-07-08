# EZTrade Engineering Diagrams

This folder contains visual reverse-engineering documentation for the EZTrade project. The diagrams were derived from the existing source code, configuration, tests, and CI workflow; when there is not enough evidence, the documentation states it explicitly.

## Structure

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

## Recommended Entry Point

1. [Technical inventory](./00-inventory/REPOSITORY-REVERSE-ENGINEERING-INVENTORY.md)
2. [Diagram roadmap](./00-inventory/DIAGRAM-ROADMAP.md)
3. [Architecture](./01-architecture/ARCHITECTURE-DIAGRAMS.md)
4. [Backend](./02-backend/BACKEND-DIAGRAMS.md)
5. [Frontend](./03-frontend/FRONTEND-DIAGRAMS.md)
6. [Database](./04-database/DATABASE-DIAGRAMS.md)
7. [Sequences](./05-sequence/SEQUENCE-DIAGRAMS.md)
8. [DevOps and infrastructure](./06-devops-infrastructure/DEVOPS-INFRASTRUCTURE-DIAGRAMS.md)
9. [Quality and operations](./07-quality-operations/QUALITY-OPERATIONS-DIAGRAMS.md)

## Regenerating Diagrams

The diagrams are rendered with Docker using the `plantuml/plantuml:latest` image when a Docker daemon is available. If Docker is not running, the scripts can also use a local PlantUML jar through the `PLANTUML_JAR` environment variable, or a temporary `plantuml.jar` stored in the system temp directory.

From the project root:

```powershell
powershell -ExecutionPolicy Bypass -File docs\engineering-diagrams\scripts\render-diagrams.ps1
```

In Bash:

```bash
./docs/engineering-diagrams/scripts/render-diagrams.sh
```

The script convention is simple: each `plantuml/` folder is rendered into its sibling `rendered/` folder, generating one PNG and one SVG for each `.puml` file.

## PNG -> SVG Convention

Markdown documents use this form:

```markdown
[![Diagram name](./rendered/diagram-name.png)](./rendered/diagram-name.svg)
```

On GitHub, the PNG is embedded and clicking it opens the SVG for higher-quality inspection.

## Evidence Notes

- Backend architecture is based on `backend/src/main/java`, `backend/pom.xml`, `application.properties`, and the Spring Modulith `package-info.java` files.
- Frontend architecture is based on `frontend/app`, `frontend/features`, `frontend/components`, and `frontend/lib`.
- The database is inferred from JPA entities; there are no Flyway/Liquibase migrations.
- The DevOps section is based on `../.github/workflows/maven.yml`, `.env.example`, `docker-compose.yml`, Dockerfiles, and explicit searches for IaC/proxy artifacts.
- Production environments, reverse proxies, and physical FKs not present in the repository were not invented.
