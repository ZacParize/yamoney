CREATE EXTENSION postgres_fdw;

CREATE SERVER payments_1_server FOREIGN DATA WRAPPER postgres_fdw OPTIONS(host '127.0.0.1', port '5432', dbname 'dbpayments');

CREATE SERVER payments_2_server FOREIGN DATA WRAPPER postgres_fdw OPTIONS(host '127.0.0.1', port '5432', dbname 'dbpayments');

CREATE SERVER payments_3_server FOREIGN DATA WRAPPER postgres_fdw OPTIONS(host '127.0.0.1', port '5432', dbname 'dbpayments');

CREATE USER MAPPING FOR dbpayments SERVER payments_1_server OPTIONS(user 'dbpayments', password 'dbpayments');

CREATE USER MAPPING FOR dbpayments SERVER payments_2_server OPTIONS(user 'dbpayments', password 'dbpayments');

CREATE USER MAPPING FOR dbpayments SERVER payments_3_server OPTIONS(user 'dbpayments', password 'dbpayments');

CREATE TABLE IF NOT EXISTS payments (
  id                 BIGSERIAL NOT NULL PRIMARY KEY,
  sender             CHARACTER VARYING NOT NULL,
  receiver           CHARACTER VARYING NOT NULL,
  create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
  amount             REAL NOT NULL
);

ALTER TABLE payments OWNER TO dbpayments;

create or replace function insert_data(in _sender CHARACTER VARYING, in _receiver CHARACTER VARYING, in _create_date TIMESTAMP WITH TIME ZONE, in _amount double precision, out _id bigint) as
$$declare
  _temp_create_date TIMESTAMP WITH TIME ZONE;
begin
  _id := nextval('payments_id_seq');
  _temp_create_date = _create_date;
  if (_temp_create_date IS NULL) THEN
    _temp_create_date = now();
  end if;
  insert into payments(id, sender, receiver, create_date, amount) values(_id, _sender, _receiver, _temp_create_date, _amount);
end;
$$ language plpgsql;

CREATE TABLE IF NOT EXISTS payments_11 (
  CHECK ( substring(sender,1,1) SIMILAR TO '[a-iA-I]%' )
) INHERITS (payments);

CREATE FOREIGN TABLE IF NOT EXISTS payments_1 (
  id                 BIGSERIAL NOT NULL,
  sender             CHARACTER VARYING NOT NULL,
  receiver           CHARACTER VARYING NOT NULL,
  create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
  amount             REAL NOT NULL
) SERVER payments_1_server OPTIONS (schema_name 'public', table_name 'payments_11');

ALTER TABLE payments_11 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_1 AS ON INSERT TO payments
WHERE ( substring(sender,1,1) SIMILAR TO '[a-iA-I]%' )
DO INSTEAD ( INSERT INTO payments_1 VALUES (NEW.*) );

CREATE TABLE IF NOT EXISTS payments_22 (
CHECK ( substring(sender,1,1) SIMILAR TO '[j-rJ-R]%' )
) INHERITS (payments);

CREATE FOREIGN TABLE IF NOT EXISTS payments_2 (
  id                 BIGSERIAL NOT NULL,
  sender             CHARACTER VARYING NOT NULL,
  receiver           CHARACTER VARYING NOT NULL,
  create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
  amount             REAL NOT NULL
  ) SERVER payments_2_server OPTIONS (schema_name 'public', table_name 'payments_22');

ALTER TABLE payments_22 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_2 AS ON INSERT TO payments
WHERE ( substring(sender,1,1) SIMILAR TO '[j-rJ-R]%' )
DO INSTEAD ( INSERT INTO payments_2 VALUES (NEW.*) );

CREATE TABLE IF NOT EXISTS payments_33 (
CHECK ( substring(sender,1,1) NOT SIMILAR TO '[a-rA-R]%' )
) INHERITS (payments);

CREATE FOREIGN TABLE IF NOT EXISTS payments_3 (
  id                 BIGSERIAL NOT NULL,
  sender             CHARACTER VARYING NOT NULL,
  receiver           CHARACTER VARYING NOT NULL,
  create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
  amount             REAL NOT NULL
  ) SERVER payments_3_server OPTIONS (schema_name 'public', table_name 'payments_33');

ALTER TABLE payments_33 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_3 AS ON INSERT TO payments
WHERE ( substring(sender,1,1) NOT SIMILAR TO '[a-rA-R]%' )
DO INSTEAD ( INSERT INTO payments_3 VALUES (NEW.*) );

CREATE INDEX payments_11_sender_idx ON payments_11 (sender);

CREATE INDEX payments_22_sender_idx ON payments_22 (sender);

CREATE INDEX payments_33_sender_idx ON payments_33 (sender);

CREATE INDEX payments_11_receiver_idx ON payments_11 (receiver);

CREATE INDEX payments_22_receiver_idx ON payments_22 (receiver);

CREATE INDEX payments_33_receiver_idx ON payments_33 (receiver);

CREATE INDEX payments_11_sender_create_date_idx ON payments_11 (sender, create_date);

CREATE INDEX payments_22_sender_create_date_idx ON payments_22 (sender, create_date);

CREATE INDEX payments_33_sender_create_date_idx ON payments_33 (sender, create_date);

CREATE INDEX payments_11_receiver_create_date_idx ON payments_11 (receiver, create_date);

CREATE INDEX payments_22_receiver_create_date_idx ON payments_22 (receiver, create_date);

CREATE INDEX payments_33_receiver_create_date_idx ON payments_33 (receiver, create_date);

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO dbpayments;