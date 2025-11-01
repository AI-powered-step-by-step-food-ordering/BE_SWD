# 🚀 Quick Start - UUID Migration

## ⚡ TL;DR - Dữ liệu có bị mất không?

### ✅ **KHÔNG BỊ MẤT** nếu bạn làm đúng:

```bash
# 1. BACKUP (BẮT BUỘC!)
mysqldump -u root -p food > backup.sql

# 2. Chạy migration
mysql -u root -p food < migration_uuid_binary_to_varchar.sql

# 3. Nếu có lỗi → Restore
mysql -u root -p food < backup.sql
```

## 🔐 Cơ chế bảo vệ dữ liệu:

Script sử dụng phương pháp **COPY → DELETE** (không phải DELETE → CREATE):

```
✅ Bước 1: Tạo cột mới (id_temp)
✅ Bước 2: COPY data từ cột cũ sang cột mới
✅ Bước 3: Xóa cột cũ (SAU KHI đã copy xong)
✅ Bước 4: Đổi tên cột mới thành tên cũ
```

**= DỮ LIỆU CÓ 2 BẢN SAO trong quá trình migration!**

## 📊 Theo dõi tiến trình:

Script tự động tạo bảng `migration_log` để track:

```sql
-- Xem log migration
SELECT * FROM migration_log ORDER BY id;

-- Đếm success/error
SELECT status, COUNT(*) FROM migration_log GROUP BY status;
```

## ⏱️ Thời gian dự kiến:

| Số records | Thời gian |
|------------|-----------|
| < 1,000    | ~30 giây  |
| < 10,000   | ~1-2 phút |
| < 100,000  | ~3-5 phút |
| > 100,000  | ~5-10 phút|

## ⚠️ Checklist trước khi chạy:

- [ ] ✅ Đã backup database
- [ ] ✅ Đủ disk space (ít nhất 2x size database)
- [ ] ✅ Không có users đang online
- [ ] ✅ Application đã shutdown
- [ ] ✅ Đã test trên môi trường DEV/TEST

## 🛡️ Rollback Plan:

Nếu migration thất bại:

```bash
# Drop database hiện tại (nếu cần)
mysql -u root -p -e "DROP DATABASE food; CREATE DATABASE food;"

# Restore từ backup
mysql -u root -p food < backup.sql

# Verify
mysql -u root -p food -e "SELECT COUNT(*) FROM users;"
```

## 💡 Tips:

### Môi trường DEV/TEST:
✅ Chạy trực tiếp, không lo lắng

### Môi trường PRODUCTION:
⚠️ **Schedule maintenance window**
1. Thông báo users
2. Shutdown application
3. Backup
4. Run migration
5. Verify thoroughly
6. Start application

## 📞 Nếu có vấn đề:

1. **STOP ngay lập tức**
2. **KHÔNG chạy thêm bất kỳ command nào**
3. **Check migration_log table**:
   ```sql
   SELECT * FROM migration_log WHERE status = 'ERROR';
   ```
4. **Restore từ backup**
5. **Liên hệ team để debug**

## ✅ Verification sau migration:

```sql
-- 1. Check data types
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food' AND DATA_TYPE = 'binary';
-- Phải trả về EMPTY!

-- 2. Check record counts (so với trước migration)
SELECT 'users' as tbl, COUNT(*) as cnt FROM users
UNION ALL SELECT 'orders', COUNT(*) FROM orders;
-- Numbers phải GIỐNG NHAU!

-- 3. Check foreign keys
SHOW CREATE TABLE orders;
-- Phải thấy tất cả foreign keys!

-- 4. Test query
SELECT * FROM users LIMIT 5;
SELECT * FROM orders LIMIT 5;
-- UUID phải hiển thị dạng: 550e8400-e29b-41d4-a716-446655440000
```

## 🎯 Kết luận:

| Câu hỏi | Trả lời |
|---------|---------|
| Dữ liệu có bị mất không? | ❌ KHÔNG (nếu có backup) |
| Migration có an toàn không? | ✅ CÓ (với backup) |
| Có thể rollback không? | ✅ CÓ (restore backup) |
| Nên chạy khi nào? | 🌙 Off-peak hours |
| Bắt buộc phải backup không? | ✅ BẮT BUỘC! |

---

**⚡ Ready? Let's migrate!**

```bash
mysqldump -u root -p food > backup_$(date +%Y%m%d_%H%M%S).sql
mysql -u root -p food < migration_uuid_binary_to_varchar.sql
```

