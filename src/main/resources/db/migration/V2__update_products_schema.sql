ALTER TABLE products
    ALTER COLUMN name TYPE VARCHAR(100),
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN category_id SET NOT NULL,
    ALTER COLUMN description TYPE VARCHAR(255),
    ALTER COLUMN price set NOT NULL,
    ALTER COLUMN code TYPE INTEGER USING (regexp_replace(code, 'PROD-', '')::integer),
    ADD CONSTRAINT chk_product_name_not_empty CHECK (char_length(trim(name)) > 0),
    ADD CONSTRAINT chk_product_price_positive CHECK (price > 0);