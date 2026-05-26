# EZTrade con Docker

Este entorno levanta tres servicios:

- `db`: MySQL 8.4, accesible desde el host en `localhost:3306`.
- `backend`: Spring Boot, accesible desde el host en `http://localhost:8088`.
- `frontend`: Next.js en modo desarrollo, accesible desde el host en `http://localhost:3000`.

## Arranque

Desde la raiz del proyecto:

```bash
docker compose up --build
```

Para dejarlo en segundo plano:

```bash
docker compose up --build -d
```

Para pararlo:

```bash
docker compose down
```

Para borrar tambien los datos locales de MySQL:

```bash
docker compose down -v
```

## Configuracion

Docker Compose usa valores por defecto pensados para desarrollo local. Si necesitas cambiarlos, crea un archivo `.env` en la raiz del proyecto.

Variables principales:

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

Dentro de Docker, el backend conecta a MySQL usando el nombre de servicio `db`. Desde tu navegador, el frontend llama al backend por `http://localhost:8088`.
