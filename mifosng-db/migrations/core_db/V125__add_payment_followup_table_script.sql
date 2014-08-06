CREATE TABLE IF NOT EXISTS `b_payment_followup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `followup_date` datetime NOT NULL,
  `followup_reason` varchar(50) NOT NULL,
  `followup_desc` varchar(200) NOT NULL,
  `current_status` varchar(20) NOT NULL,
  `requested_status` varchar(10) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert IGNORE into m_code VALUES (null,'Suspension Reason',0,'Reason for Order Suspension');
select @a_lid:=last_insert_id();
insert IGNORE into m_code_value VALUES (null,@a_lid,'Payment Due',0);
insert IGNORE into m_code_value VALUES (null,@a_lid,'Vacations',0);