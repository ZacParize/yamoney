## Synopsis

Microservice for very simple money transfers with horizontal sharding.

## Installation

1) Simple clone the project (git clone https://github.com/ZacParize/yamoney.git);

2) Execute in local postgres:

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

Methods:

### 1) POST /api/payment/load?req_id={reqId} - load batch of payments.

BODY: array of payments.

NOTE: batch size is less or equals 500.

Example: 
    
    http://localhost:8001/api/payment/load?req_id=1
    
    [ {"sender":"s1","receiver":"r1","amount":100, "createDate": "2020-06-16T09:30:07.109Z"}
    , {"sender":"r1","receiver":"r2","amount":100} ]

### 2) POST /api/payment/count-by-receiver/{receiver}?req_id={reqId} - count received amount by receiver

BODY: [from, to] range.

Example: 
    
    http://localhost:8001/api/payment/count-by-receiver/r2?req_id=1
    
    { "from" : "2020-01-16T16:30:07.109+07:00", "to" : "2020-12-16T16:30:07.109+07:00" }

### 3) POST /api/payment/count-by-sender/{sender}?req_id={reqId} - count sent amount by sender

BODY: [from, to] range.

Example: 
    
    http://localhost:8001/api/payment/count-by-sender/r2?req_id=1
    
    { "from" : "2020-01-16T16:30:07.109+07:00", "to" : "2020-12-16T16:30:07.109+07:00" }

### 4) POST /api/payment/count-balance/{actor}?req_id={reqId} - count balance by actor

BODY: [from, to] range.

Example: 
    
    http://localhost:8001/api/payment/count-balance/r2?req_id=1
    
    { "from" : "2020-01-16T16:30:07.109+07:00", "to" : "2020-12-16T16:30:07.109+07:00" }