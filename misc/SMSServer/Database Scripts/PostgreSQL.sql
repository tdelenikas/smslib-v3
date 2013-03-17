CREATE TABLE smsserver_in
(
  id serial NOT NULL,
  process integer NOT NULL,
  originator varchar(16) NOT NULL,
  type varchar(1) NOT NULL,
  encoding varchar(1) NOT NULL,
  message_date timestamp NOT NULL,
  receive_date timestamp NOT NULL,
  text text NOT NULL,
  original_ref_no varchar(64),
  original_receive_date timestamp,
  gateway_id varchar(64) NOT NULL,
  PRIMARY KEY (id )
);

CREATE TABLE smsserver_calls
(
  id serial NOT NULL,
  call_date timestamp NOT NULL,
  gateway_id varchar(64) NOT NULL,
  caller_id varchar(16) NOT NULL,
  PRIMARY KEY (id )
);

CREATE TABLE smsserver_out
(
  id serial NOT NULL,
  type varchar(1) NOT NULL default 'O',
  recipient varchar(16) NOT NULL,
  text text NOT NULL,
  wap_url text NOT NULL default '',
  wap_expiry_date timestamp,
  wap_signal varchar(1),
  create_date timestamp NOT NULL default current_timestamp,
  originator varchar(16) NOT NULL default '',
  encoding varchar(1) NOT NULL default 'U',
  status_report integer NOT NULL default 1,
  flash_sms integer NOT NULL default 0,
  src_port integer NOT NULL default -1,
  dst_port integer NOT NULL default -1,
  sent_date timestamp,
  ref_no varchar(64),
  priority integer NOT NULL default 0,
  status varchar(1) NOT NULL default 'U',
  errors integer NOT NULL default 0,
  gateway_id varchar(64) NOT NULL default '*',
  PRIMARY KEY (id )
);

