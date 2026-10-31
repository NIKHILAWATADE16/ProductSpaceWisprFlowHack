# Phase 1 schema notes

The initial relational schema is generated from the JPA entities under `backend/src/main/java/com/financecoach/entity`.

Tables:

- `app_users`
- `financial_profiles`
- `transactions`
- `financial_goals`
- `budgets`
- `subscriptions`

Relationships use a user-owned aggregate model. Foreign keys prevent records from existing without a user, and unique constraints protect one profile per user and one budget per category/month/year. Amount columns use PostgreSQL `numeric(19,2)` through JPA precision/scale mappings.
