-- Índices para búsquedas bajo demanda. Se omiten si la tabla aún no existe
-- durante el primer arranque; Hibernate la crea en ese mismo arranque.
SET @sql = IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'product') = 1,
              'CREATE INDEX idx_product_name ON product (name)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'product') = 1,
              'CREATE INDEX idx_product_bar_code ON product (bar_code)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'customer') = 1,
              'CREATE INDEX idx_customer_name ON customer (name)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
