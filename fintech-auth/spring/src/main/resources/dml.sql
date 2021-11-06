insert into client(id, birthdate, contract, phone)
values (nextval('client_id_seq'), date '2021-10-06', 'C1', 'P1')
ON CONFLICT (phone) DO NOTHING;
