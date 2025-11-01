# Hướng Dẫn Migration UUID từ BINARY sang VARCHAR

## 🎯 Mục đích
Chuyển đổi tất cả các cột UUID từ kiểu BINARY/VARBINARY sang VARCHAR(36) để:
- Dễ đọc và debug hơn (hiển thị dạng `550e8400-e29b-41d4-a716-446655440000`)
- Tương thích với các entity đã định nghĩa `@Column(columnDefinition="VARCHAR(36)")`
- Giảm complexity khi làm việc với UUID

## ⚙️ Migration Strategy

Migration sử dụng chiến lược an toàn:
1. **Drop tất cả foreign keys** để tránh conflicts
2. **Convert từng cột**: Add temp column → Convert data using HEX() → Drop old → Rename
3. **Recreate foreign keys** với đúng data type mới

## 📋 Cách 1: Automatic Migration (Khuyến nghị)

Migration sẽ tự động chạy khi bạn start application:

```bash
# BACKUP DATABASE TRƯỚC!
mysqldump -u root -p food > backup_food.sql

# Chạy application
mvn spring-boot:run
```

Application sẽ tự động:
1. ✅ Kiểm tra kiểu dữ liệu của cột ID
2. ✅ Nếu là BINARY → Drop FKs → Convert columns → Recreate FKs
3. ✅ Nếu đã là VARCHAR → Skip migration

**Console output mẫu:**
```
Starting UUID columns migration from BINARY to VARCHAR(36)...
Binary ID columns detected. Starting conversion...
⚠️  WARNING: This migration will drop all foreign keys and recreate them!
Step 1: Dropping foreign key constraints...
Step 2: Converting UUID columns...
  Converting table: users
    ✓ Converted column: id
  Converting table: stores
    ✓ Converted column: id
...
Step 3: Recreating foreign key constraints...
✅ UUID migration completed successfully!
```

**Lưu ý**: 
- Migration tự động đã được implement trong `DatabaseMigration.java`
- Chạy tự động mỗi lần start app (nhưng chỉ migrate 1 lần)
- Nếu có lỗi, restore từ backup

## 📋 Cách 2: Manual Migration (Nếu muốn kiểm soát)

### Bước 1: Backup Database

```bash
# Windows CMD
mysqldump -u root -p food > backup_food_%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql

# Hoặc thủ công qua MySQL Workbench
# Server > Data Export > Export to Self-Contained File
```

### Bước 2: Chạy Migration Script

```bash
# Sử dụng MySQL CLI
mysql -u root -p food < migration_uuid_binary_to_varchar.sql

# Hoặc mở MySQL Workbench và execute file
# File > Open SQL Script > migration_uuid_binary_to_varchar.sql > Execute
```

### Bước 3: Kiểm tra kết quả

```sql
-- Kiểm tra kiểu dữ liệu của các cột
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food'
    AND COLUMN_NAME LIKE '%id%'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Kết quả mong đợi: Tất cả ID columns phải là VARCHAR(36)
```

## 🗂️ Danh sách bảng được migrate

1. **users** - id
2. **stores** - id
3. **categories** - id
4. **promotions** - id
5. **ingredients** - id, category_id
6. **bowls_template** - id, category_id
7. **orders** - id, store_id, user_id, promotion_id
8. **bowls** - id, order_id, template_id
9. **bowl_items** - id, bowl_id, ingredient_id
10. **template_steps** - id, template_id, ingredient_id
11. **inventory** - id, store_id, ingredient_id
12. **kitchen_jobs** - id, order_id, store_id
13. **payment_transactions** - id, order_id
14. **notifications** - id, user_id
15. **tokens** - id, user_id
16. **promotion_redemptions** - id, user_id, promotion_id, order_id
17. **ingredient_restrictions** - id, ingredient_id

## ⚠️ Lưu ý quan trọng

1. **BACKUP**: Luôn backup database trước khi migrate
2. **Foreign Keys**: Script tự động disable/enable foreign key checks
3. **Data Loss**: Migration KHÔNG làm mất dữ liệu, chỉ thay đổi kiểu dữ liệu
4. **Downtime**: Nên chạy khi ít người dùng (migration mất khoảng 1-2 phút)
5. **Rollback**: Nếu có lỗi, restore từ backup

## 🔧 Troubleshooting

### ❌ Lỗi: Incorrect string value '\xAE\xAD5\x8C\xD6A...'
**Nguyên nhân**: Không thể convert trực tiếp BINARY sang VARCHAR

**Giải pháp**: Migration mới đã fix bằng cách:
- Add temporary VARCHAR column
- Convert BINARY → VARCHAR using `HEX()` function
- Drop old BINARY column
- Rename temp column

### ❌ Lỗi: Referencing column and referenced column are incompatible
**Nguyên nhân**: Foreign key references cột có data type khác nhau

**Giải pháp**: Migration mới sẽ:
1. Drop tất cả FKs trước
2. Convert tất cả columns
3. Recreate FKs sau

### ❌ Lỗi: Migration chạy nhưng database vẫn BINARY
**Kiểm tra**:
```sql
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food' AND COLUMN_NAME LIKE '%id%'
ORDER BY TABLE_NAME;
```

**Giải pháp**: Chạy manual migration script

### ⚠️ Application crash khi migration
**Restore database**:
```bash
mysql -u root -p food < backup_food.sql
```

**Sau đó**: Báo lỗi cho dev team hoặc chạy manual migration

## ✅ Verification

Sau khi migration, verify bằng các query sau:

```sql
-- 1. Kiểm tra không có cột BINARY nào còn lại
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food' 
    AND DATA_TYPE IN ('binary', 'varbinary');
-- Kết quả phải empty

-- 2. Kiểm tra số lượng records không đổi
SELECT 'Before Migration' as status, <total_count> as count
UNION ALL
SELECT 'After Migration', COUNT(*) FROM users; -- lặp lại cho các bảng khác

-- 3. Test application
-- Start application và test các API
```

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Check console logs của application
2. Check MySQL error log
3. Restore từ backup và thử lại
4. Liên hệ team dev

## 🎉 Kết quả mong đợi

Sau migration thành công:
- Tất cả UUID hiển thị dạng: `550e8400-e29b-41d4-a716-446655440000`
- API hoạt động bình thường
- Performance không thay đổi đáng kể
- Dễ dàng debug và đọc SQL queries

