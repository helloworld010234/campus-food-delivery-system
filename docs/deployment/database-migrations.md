# Database initialization & migration order

This document is the authoritative source for the database setup order
of the Sky Take Out backend. The current Java code references
multi-merchant columns (`merchant_id`, `campus_id`) and additional
tables (`campus`, `merchant`) that live in a *separate* migration
script, not in `core/database/init.sql`.

## Required order

1. **Create the database** (utf8mb4):

   ```sql
   CREATE DATABASE IF NOT EXISTS sky_take_out
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```

2. **Apply base schema + seed data**:

   ```bat
   mysql -u root -p sky_take_out < core\database\init.sql
   ```

   What `init.sql` provides:
   - core business tables (employee, category, dish, dish_flavor, setmeal, setmeal_dish, orders, order_detail, address_book, shopping_cart, user, etc.)
   - default admin (`admin` / `123456`) and a small set of categories / dishes used by acceptance.

3. **Apply multi-merchant migration** (almost always required for current code):

   ```bat
   mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
   ```

   What this migration adds:
   - new tables: `campus`, `merchant`, `merchant_user` (the exact list lives in the script)
   - new columns on existing tables: `merchant_id`, `campus_id`, etc.
   - foreign keys / indexes

   **Rerun safety:** the script uses `CREATE TABLE` (without `IF NOT EXISTS` for new tables) and `ALTER TABLE` statements that fail loudly if columns already exist. Treat it as a one-shot migration. Re-running on a partially migrated database is **not safe**; restore from backup if you need to reapply.

4. **Optional, image / dish replacements**:
   - `core\backend\scripts\migrate-legacy-dish-images.ps1` — copies legacy dish images into the campus image namespace.
   - `core\backend\scripts\replace_dishes_delete.sql` and `campus_replace_delete.sql` — reset / replace seed dishes for acceptance.

5. **Optional, mock user default address** (helps the order-flow E2E):

   ```sql
   INSERT INTO sky_take_out.address_book
   (user_id, consignee, sex, phone, province_name, city_name, district_name, detail, label, is_default)
   VALUES
   (1, '测试用户', '男', '13800138000', '北京市', '北京市', '朝阳区', '酒仙桥北路14号', '公司', 1);
   ```

## How to detect that the multi-merchant step is missing

When you run the backend and immediately see a startup failure or login
failure with one of the following symptoms, step 3 above was skipped or
only partially applied:

- `Unknown column 'merchant_id' in 'field list'` (any Mapper)
- `Unknown column 'campus_id' in 'where clause'`
- `Table 'sky_take_out.campus' doesn't exist`
- `Table 'sky_take_out.merchant' doesn't exist`
- HTTP 500 on `/admin/category/list` or `/user/shop/status` once login succeeds.

## Rollback / reset

There is no automated rollback for the multi-merchant migration. The
recommended path is:

1. Drop and recreate the database (this destroys local data):

   ```sql
   DROP DATABASE sky_take_out;
   CREATE DATABASE sky_take_out
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```

2. Re-run steps 2 and 3 from the top.

For staging / production environments, take a logical backup with
`mysqldump` *before* applying step 3, and restore from that dump if
something goes wrong.

## Coordination notes

- Database migration scripts themselves are owned by Agents 1 / 2 / 3
  (backend security, orders, ops). Agent 5 must **not** edit
  `core/database/init.sql` or the SQL files under
  `core/backend/scripts/` — only document execution order.
- If schema drift is detected by Agent 6 verification, file an issue
  and ask Agents 1-3 to amend the migration.
