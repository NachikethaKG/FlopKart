-- Initialize individual databases for FlopKart microservices
SELECT 'CREATE DATABASE flopkart_user_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'flopkart_user_db')\gexec

SELECT 'CREATE DATABASE flopkart_product_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'flopkart_product_db')\gexec

SELECT 'CREATE DATABASE flopkart_order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'flopkart_order_db')\gexec

SELECT 'CREATE DATABASE flopkart_payment_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'flopkart_payment_db')\gexec
