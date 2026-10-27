# EZTrade with Docker

This environment starts three services:

- `db`: MySQL 8.4, available from the host at `localhost:3306`.
- `backend`: Spring Boot, available from the host at `http://localhost:8088`.
- `frontend`: Next.js in development mode, available from the host at `http://localhost:3000`.

## Startup

From the project root:

```bash
docker compose up --build
```

To leave it running in the background:

```bash
docker compose up --build -d
```

To stop it:

```bash
docker compose down
```

To also delete local MySQL data:

```bash
docker compose down -v
```

## Configuration

Docker Compose uses default values intended for local development. If you need to change them, create a `.env` file in the project root.

Main variables:

```env
DB_USERNAME=eztrade_user
DB_PASSWORD=change-me
MYSQL_ROOT_PASSWORD=root

ALPHA_VANTAGE_API_KEY=replace-with-alpha-vantage-key
JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=

BACKEND_PORT=8088
FRONTEND_PORT=3000
DB_PORT=3306
```

Inside Docker, the backend connects to MySQL using the `db` service name. From your browser, the frontend calls the backend through `http://localhost:8088`.
