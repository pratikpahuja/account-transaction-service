insert into tenant(id, name) values(1, 'enterprise-all-inclusive.com') ON CONFLICT DO NOTHING;
insert into tenant(id, name) values(2, 'betrieb-alles-inklusive.de') ON CONFLICT DO NOTHING;

insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (22000, 1100, 1, 100) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (22001, 1100, 2, -50) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (22002, 1150, 1, 0) ON CONFLICT DO NOTHING;
insert into customer_tenant(id, customer_id, tenant_id, account_balance) values (22003, 1170, 2, 0) ON CONFLICT DO NOTHING;
