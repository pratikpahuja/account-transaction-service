insert into tenant(id, name) values(1, 'tenant1') ON CONFLICT DO NOTHING;
insert into tenant(id, name) values(2, 'tenant2') ON CONFLICT DO NOTHING;

insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12000, 100, 1, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12001, 100, 2, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12002, 110, 1, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12003, 110, 2, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12004, 120, 1, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (12005, 120, 2, 0) ON CONFLICT DO NOTHING;
