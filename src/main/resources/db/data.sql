INSERT INTO CUSTOMER (C_NAME) VALUES
  ('bob'),
  ('louise');

INSERT INTO TRANSACTIONS (T_AMOUNT, T_DATE, DESCRIPTION, CATEGORY, VENDOR, CUSTOMER_ID) VALUES
  ('700', '2021-1-19', 'Belcher', 'restaurants', 'bobs burgers', 1),
  ('50', '2021-2-19', 'Belcher', 'restaurants', 'bobs burgers', 1),
  ('600', '2021-2-19', 'Belcher', 'restaurants', 'bobs burgers', 1),

  ('60', '2021-1-19', 'Belcher', 'bill', 'llyods bank', 1),
  ('60', '2021-1-20', 'Belcher', 'bill', 'barclays bank', 1),
  ('100', '2021-2-19', 'Belcher', 'bill', 'llyods bank', 1),

  ('700', '2021-2-19', 'Belcher', 'restaurants', 'bobs burgers', 2),
  ('700', '2021-2-19', 'Belcher', 'restaurants', 'bobs burgers', 2)
