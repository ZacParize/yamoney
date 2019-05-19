## Synopsis

Microservice for very simple money transfers with horizontal sharding.

## Installation

1) Simple clone the project (git clone https://github.com/ZacParize/yamoney.git);

2) Execute in local posgtres:

DROP DATABASE IF EXISTS dbpayments;

DROP SCHEMA IF EXISTS dbpayments;

DROP USER IF EXISTS dbpayments;

DROP ROLE IF EXISTS dbpayments;

CREATE ROLE dbpayments WITH LOGIN PASSWORD 'dbpayments';

ALTER ROLE dbpayments SUPERUSER;

CREATE SCHEMA dbpayments AUTHORIZATION dbpayments;

CREATE DATABASE dbpayments OWNER dbpayments;

P.S: for terminating all connections:

SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid <> pg_backend_pid() AND datname = 'dbpayments';

## API Reference

There is no specified API module in project.

But it could be in case of clarification requirements. 

The microservice returns values "as is".