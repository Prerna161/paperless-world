#!/usr/bin/env bash
set -e

echo "==================================================="
echo "Paperless World: Database Provisioning Script"
echo "==================================================="

read -p "Enter MySQL Username [default: root]: " DB_USER
DB_USER=${DB_USER:-root}
read -s -p "Enter MySQL Password: " DB_PASS
echo ""

echo "[1/2] Creating Schema and Tables (sql/01_schema.sql)..."
mysql -u "$DB_USER" -p"$DB_PASS" < sql/01_schema.sql

echo "[2/2] Loading Demo Seed Data (sql/02_seed_demo.sql)..."
mysql -u "$DB_USER" -p"$DB_PASS" < sql/02_seed_demo.sql

echo ""
echo "==================================================="
echo "Database provisioning complete!"
echo "Demo user: student@paperless.org / password123"
echo "==================================================="
