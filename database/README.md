# Database setup

The development database is provisioned by the root `docker-compose.yml` file.

```powershell
docker compose up -d postgres
docker compose ps
```

Defaults:

- Database: `financecoach`
- User: `financecoach`
- Password: `change-me`
- Port: `5432`

Override these values with environment variables before starting Compose. The Spring Boot datasource can be overridden with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
