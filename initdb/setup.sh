#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
  CREATE DATABASE transaction_db;
  CREATE DATABASE transaction_test_db;
EOSQL
