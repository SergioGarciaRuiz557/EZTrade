# Quality and Operations Diagrams

This section summarizes tests, architectural verification, static-quality scope, and inferable release strategy. It was built from `backend/src/test/java`, `backend/pom.xml`, `frontend/package.json`, `frontend/tsconfig.json`, and `../.github/workflows/maven.yml`.

## Testing Strategy Flow

[![Testing strategy flow](./rendered/testing-strategy-flow.png)](./rendered/testing-strategy-flow.svg)

**Purpose.** Show which types of tests the backend runs.

**How to read it.** The Maven flow triggers domain, service, controller, security, cache, and architectural integration tests.

**Value.** Provides a layered quality-assurance view, useful for justifying technical confidence.

**Limitation.** No frontend or e2e tests were detected.

## Static Quality and Modulith

[![Static quality and Modulith](./rendered/static-quality-and-modulith.png)](./rendered/static-quality-and-modulith.svg)

**Purpose.** Show structural quality controls and detected gaps.

**How to read it.** Spring Modulith verifies module dependencies; TypeScript and `next lint` exist in the frontend, but the workflow does not run them.

**Value.** Separates real architectural quality from checks that are declared but not integrated.

## Branching and Release Evidence

[![Branching and release evidence](./rendered/release-branching-evidence.png)](./rendered/release-branching-evidence.svg)

**Purpose.** Document the branching and publication strategy that can be inferred.

**How to read it.** Only `main` appears as the target branch for `push` and `pull_request`; there are no deployment jobs or tag-based publication.

**Value.** Avoids inventing GitFlow, staging, or production when the repository does not declare them.

## External Dependencies and Integrations

[![External dependencies and integrations](./rendered/external-dependencies-integrations.png)](./rendered/external-dependencies-integrations.svg)

**Purpose.** Summarize relevant backend, frontend, and CI external dependencies.

**How to read it.** Backend integrates Spring, JPA/MySQL, JWT, WebSocket, Caffeine, and Alpha Vantage. Frontend integrates Next/React, SWR, STOMP, Radix/lucide, Recharts, and Tailwind.

**Value.** Serves as a risk and maintenance map: each important dependency is connected with its responsibility.

## Conclusion

Backend quality is fairly mature, with layered tests and Modulith verification. Frontend/DevOps quality and operations have clear opportunities: run frontend lint/build in CI, add UI/e2e tests, and formalize releases or deployments when real environments exist.
