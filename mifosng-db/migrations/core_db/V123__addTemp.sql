CREATE TABLE IF NOT EXISTS `temp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `generate_key` varchar(33) NOT NULL,
  `status` varchar(10) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `generate_key` (`generate_key`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1 ;

INSERT IGNORE INTO m_permission VALUES (null,'billing', 'REGISTER_SELFCARE', 'SELFCARE', 'REGISTER', 0);

INSERT IGNORE INTO m_permission VALUES (null,'billing', 'EMAILVERIFICATION_SELFCARE', 'SELFCARE', 'EMAILVERIFICATION', 0);

INSERT IGNORE INTO m_permission VALUES (null,'billing', 'SELFREGISTRATION_ACTIVATE', 'ACTIVATE', 'SELFREGISTRATION', 0);

INSERT IGNORE INTO `c_configuration`(`id`,`name`,`enabled`,`value`) values (null,'Register_plan',1,'{"billAlign":false,"planCode":12,"contractPeriod":8,"paytermCode":"Monthly"}');



