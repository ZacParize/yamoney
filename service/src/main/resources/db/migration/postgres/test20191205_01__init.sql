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

CREATE TABLE IF NOT EXISTS payments_1(
  CHECK ( substring(sender,1,1) SIMILAR TO '[a-iA-I]%' )
) INHERITS (payments);

ALTER TABLE payments_1 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_1 AS ON INSERT TO payments
  WHERE ( substring(sender,1,1) SIMILAR TO '[a-iA-I]%' )
  DO INSTEAD ( INSERT INTO payments_1 VALUES (NEW.*) );

CREATE TABLE IF NOT EXISTS payments_2 (
  CHECK ( substring(sender,1,1) SIMILAR TO '[j-rJ-R]%' )
) INHERITS (payments);

ALTER TABLE payments_2 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_2 AS ON INSERT TO payments
  WHERE ( substring(sender,1,1) SIMILAR TO '[j-rJ-R]%' )
  DO INSTEAD ( INSERT INTO payments_2 VALUES (NEW.*) );

CREATE TABLE IF NOT EXISTS payments_3 (
  CHECK ( substring(sender,1,1) NOT SIMILAR TO '[a-rA-R]%' )
) INHERITS (payments);

ALTER TABLE payments_3 OWNER TO dbpayments;

CREATE OR REPLACE RULE payments_insert_to_3 AS ON INSERT TO payments
  WHERE ( substring(sender,1,1) NOT SIMILAR TO '[a-rA-R]%' )
  DO INSTEAD ( INSERT INTO payments_3 VALUES (NEW.*) );

CREATE INDEX payments_1_sender_idx ON payments_1 (sender);

CREATE INDEX payments_2_sender_idx ON payments_2 (sender);

CREATE INDEX payments_3_sender_idx ON payments_3 (sender);

CREATE INDEX payments_1_receiver_idx ON payments_1 (receiver);

CREATE INDEX payments_2_receiver_idx ON payments_2 (receiver);

CREATE INDEX payments_3_receiver_idx ON payments_3 (receiver);

CREATE INDEX payments_1_sender_create_date_idx ON payments_1 (sender, create_date);

CREATE INDEX payments_2_sender_create_date_idx ON payments_2 (sender, create_date);

CREATE INDEX payments_3_sender_create_date_idx ON payments_3 (sender, create_date);

CREATE INDEX payments_1_receiver_create_date_idx ON payments_1 (receiver, create_date);

CREATE INDEX payments_2_receiver_create_date_idx ON payments_2 (receiver, create_date);

CREATE INDEX payments_3_receiver_create_date_idx ON payments_3 (receiver, create_date);

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO dbpayments;

select insert_data('sender1', 'receiver1', null, 100);

select insert_data('sender2', 'receiver1', null, 50);

select insert_data('sender2', 'receiver2', null, 150);

select insert_data('receiver2', 'sender2', null, 150);

select insert_data('receiver1', 'receiver2', null, 20);