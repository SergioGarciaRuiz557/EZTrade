# Database Diagrams

The database is documented from JPA entities and Spring Data repositories. No Flyway/Liquibase migrations or versioned DDL were found, so the diagrams describe the schema inferred by JPA/Hibernate and `spring.jpa.hibernate.ddl-auto=update`.

## Entity-Relationship Summary

[![Entity-relationship summary](./rendered/database-er-summary.png)](./rendered/database-er-summary.svg)

**Purpose.** Summarize main tables, keys, and logical relationships.

**How to read it.** Dashed relationships are not declared FKs: they represent logical connections through `owner`, `recipient`, `symbol`, and `referenceId`.

**Value.** Explains domain persistence without inventing physical constraints.

**Limitation.** The repository does not declare JPA associations between entities or explicit foreign keys.

## JPA Entity-Table Mapping

[![JPA entity-table mapping](./rendered/jpa-entity-table-mapping.png)](./rendered/jpa-entity-table-mapping.svg)

**Purpose.** Relate domain, JPA entities, and tables.

**How to read it.** Mappers convert between domain models and infrastructure entities. Each JPA entity ends in a concrete table.

**Value.** Makes the domain-persistence separation visible, which is important for explaining hexagonal architecture and testability.

## Detailed Database Tables

[![Detailed database tables](./rendered/database-tables-detailed.png)](./rendered/database-tables-detailed.svg)

**Purpose.** Detail columns, inferred types, indexes, and unique constraints.

**How to read it.** Each table shows PK, relevant fields, `DECIMAL(19,8)` for monetary/quantity values, and constraints declared with `@Table`, `@Column`, `@Index`, and `@UniqueConstraint`.

**Value.** Serves as a technical reference for the report, audit, and future migrations.

**Limitation.** The real DDL may vary slightly depending on the MySQL dialect and the previous evolution of a database with `ddl-auto=update`.

## Conclusion

The relational model is oriented toward persisting aggregates and projections by module. The absence of migrations and explicit FKs is an important operational improvement point: it simplifies local development but reduces schema traceability and control in shared environments.
