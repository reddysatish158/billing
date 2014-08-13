INSERT IGNORE INTO  m_permission VALUES(null, 'portfolio', 'CREATE_PARENTCLIENT', 'PARENTCLIENT', 'CREATE', '0');
insert IGNORE into m_permission values (null,'Ordering','CONFIRM_PROVISIONINGDETAILS','PROVISIONINGDETAILS','CONFIRM',0);
  
Drop procedure IF EXISTS parentclient; 
DELIMITER //
create procedure parentclient() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'parent_id'
     and TABLE_NAME = 'm_client'
     and TABLE_SCHEMA = DATABASE())THEN
alter table m_client add column parent_id bigint(20) DEFAULT NULL;

END IF;
END //
DELIMITER ;
call parentclient();

Drop procedure IF EXISTS parentclient; 
 
CREATE OR REPLACE VIEW `billdetails_v` AS select `b`.`client_id` AS `client_id`,
`a`.`id` AS `transId`,date_format(`b`.`invoice_date`,'%Y-%m-%d') AS `transDate`,
'SERVICE_CHARGES' AS `transType`,`a`.`charge_amount` AS `amount`,
concat(date_format(`a`.`charge_start_date`,'%Y-%m-%d'),' to ',
date_format(`a`.`charge_end_date`,'%Y-%m-%d')) AS `description`,
`c`.`plan_id` AS `planCode` from ((`b_charge` `a` join `b_invoice` `b`) join `b_orders` `c`) 
where ((`a`.`invoice_id` = `b`.`id`) and (`a`.`order_id` = `c`.`id`) and isnull(`a`.`bill_id`)
 and (`b`.`invoice_date` <= now())) 
union all 
select `b`.`client_id` AS `client_id`,`a`.`id` AS `transId`,
date_format(`b`.`invoice_date`,'%Y-%m-%d') AS `transDate`,
'TAXES' AS `transType`,`a`.`tax_amount` AS `amount`,
`a`.`tax_code` AS `description`,NULL AS `planCode` from (`b_charge_tax` `a` join `b_invoice` `b`) 
where ((`a`.`invoice_id` = `b`.`id`) and isnull(`a`.`bill_id`) and 
(`b`.`invoice_date` <= now())) 
union all 
select `b_adjustments`.`client_id` AS `client_id`,`b_adjustments`.`id` AS `transId`,
date_format(`b_adjustments`.`adjustment_date`,'%Y-%m-%d') AS `transDate`,'ADJUSTMENT' AS `transType`,
(case `b_adjustments`.`adjustment_type` 
when 'DEBIT' then `b_adjustments`.`adjustment_amount`
when 'CREDIT' then -(`b_adjustments`.`adjustment_amount`) end) AS `amount`,
`b_adjustments`.`remarks` AS `remarks`,
`b_adjustments`.`adjustment_type` AS `adjustment_type` from `b_adjustments` 
where (isnull(`b_adjustments`.`bill_id`) and (`b_adjustments`.`adjustment_date` <= now())) 
union all 
select `pa`.`client_id` AS `client_id`,`pa`.`id` AS `transId`,
date_format(`pa`.`payment_date`,'%Y-%m-%d') AS `transDate`,
concat('PAYMENT',' - ',`p`.`code_value`) AS `transType`,
`pa`.`amount_paid` AS `invoiceAmount`,`pa`.`Remarks` AS `remarks`,
`p`.`code_value` AS `code_value` from (`b_payments` `pa` join `m_code_value` `p`) 
where (isnull(`pa`.`bill_id`) and (`pa`.`payment_date` <= now()) and 
(`pa`.`paymode_id` = `p`.`id`)) 
union all 
select `b`.`client_id` AS `client_id`,`a`.`id` AS `transId`,
date_format(`c`.`sale_date`,'%Y-%m-%d') AS `transDate`,
'ONETIME_CHARGES' AS `transType`,`c`.`total_price` AS `amount`,
`c`.`charge_code` AS `charge_code`,`c`.`item_id` AS `item_id` 
from ((`b_charge` `a` join `b_invoice` `b`) join `b_onetime_sale` `c`) 
where ((`a`.`invoice_id` = `b`.`id`) and (`a`.`order_id` = `c`.`id`) 
and isnull(`a`.`bill_id`) and (`c`.`sale_date` <= now()));

CREATE OR REPLACE VIEW `bmaster_vw_sn`
AS
   SELECT DISTINCT `bm`.`id` AS `Bill_Id`,
                   `bm`.`Bill_No` AS `Bill_No`,
                   `bm`.`Client_id` AS `Client_id`,
                   cast(`bm`.`Bill_date` AS date) AS `Bill_date`,
                   `bm`.`Bill_startdate` AS `Bill_startdate`,
                   `bm`.`Bill_enddate` AS `Bill_enddate`,
                   `bm`.`Due_date` AS `Due_date`,
                   `bm`.`Previous_balance` AS `Previous_balance`,
                   `bm`.`Charges_amount` AS `Charges_amount`,
                   `bm`.`Adjustment_amount` AS `Adjustment_amount`,
                   `bm`.`Tax_amount` AS `Tax_amount`,
                   `bm`.`Paid_amount` AS `Paid_amount`,
                   `bm`.`Due_amount` AS `Due_amount`,
                   `bm`.`filename` AS `filename`,
                   `bm`.`Promotion_description` AS `Promotion_description`,
                   `bm`.`Bill_Period` AS `Bill_Period`,
                   `bm`.`adjustments_payments` AS `adjustments_payments`,
                   `ca`.`address_id` AS `address_id`,
                   `ca`.`address_key` AS `address_key`,
                   `ca`.`address_no` AS `address_no`,
                   `ca`.`street` AS `street`,
                   `ca`.`zip` AS `zip`,
                   `ca`.`city` AS `city`,
                   `ca`.`state` AS `state`,
                   `ca`.`country` AS `country`,
                   concat(`ca`.`address_no`,
                          ',',
                          `ca`.`street`,
                          ',',
                          `ca`.`city`,
                          ',',
                          `ca`.`state`,
                          ',',
                          `ca`.`country`,
                          '-',
                          `ca`.`zip`)
                      AS `address`,
                   `ca`.`is_deleted` AS `is_deleted`,
                   `c`.`account_no` AS `account_no`,
                   `c`.`external_id` AS `external_id`,
                   `c`.`status_enum` AS `status_enum`,
                   `c`.`activation_date` AS `activation_date`,
                   `c`.`office_id` AS `office_id`,
                   `c`.`firstname` AS `firstname`,
                   `c`.`middlename` AS `middlename`,
                   `c`.`lastname` AS `lastname`,
                   `c`.`fullname` AS `fullname`,
                   `c`.`display_name` AS `display_name`,
                   `c`.`image_key` AS `image_key`,
                   `c`.`category_type` AS `category_type`,
                   round((`bm`.`Charges_amount` * 0.17), 2) AS `excise_duty`,
                   round(`bm`.`Tax_amount`, 2) AS `vat`
     FROM (   (   `b_bill_master` `bm`
               JOIN
                  `m_client` `c`
               ON ((`bm`.`Client_id` = `c`.`id`)))
           LEFT JOIN
              `b_client_address` `ca`
           ON ((    (`ca`.`client_id` = `c`.`id`)
                AND (`ca`.`address_key` = 'PRIMARY'))));
				
				CREATE OR REPLACE VIEW `bdetails_vw_sn`
AS
   SELECT DISTINCT
          `bm`.`id` AS `Bill_Id`,
          `bm`.`Bill_No` AS `Bill_No`,
          `bm`.`Client_id` AS `Client_id`,
          cast(`bm`.`Bill_date` AS date) AS `Bill_date`,
          `bm`.`Bill_startdate` AS `Bill_startdate`,
          `bm`.`Bill_enddate` AS `Bill_enddate`,
          `bm`.`Due_date` AS `Due_date`,
          `bm`.`Previous_balance` AS `Previous_balance`,
          `bm`.`Charges_amount` AS `Charges_amount`,
          `bm`.`Adjustment_amount` AS `Adjustment_amount`,
          `bm`.`Tax_amount` AS `Tax_amount`,
          `bm`.`Paid_amount` AS `Paid_amount`,
          `bm`.`Due_amount` AS `Due_amount`,
          `bm`.`filename` AS `filename`,
          `bm`.`Promotion_description` AS `Promotion_description`,
          `bm`.`Bill_Period` AS `Bill_Period`,
          `bm`.`adjustments_payments` AS `adjustments_payments`,
          `bpm`.`plan_description` AS `plan_description`,
          `bo`.`billing_frequency` AS `billing_frequency`,
          concat(cast(`bo`.`start_date` AS date),
                 ' To ',
                 cast(`bo`.`end_date` AS date))
             AS `billDuration`,
          round((`bm`.`Charges_amount` * 0.17), 2) AS `excise_duty`,
          round((`bm`.`Tax_amount` - (`bm`.`Charges_amount` * 0.17)), 2)
             AS `vat`,
			  `bm`.`Charges_amount` + (`bm`.`Charges_amount` * 0.17) +
             (`bm`.`Tax_amount` - (`bm`.`Charges_amount` * 0.17))  bill_amt
     FROM (   (   `b_bill_master` `bm`
               JOIN
                  `b_orders` `bo`
               ON ((    (`bm`.`Client_id` = `bo`.`client_id`)
                    AND (`bo`.`order_status` = 1)
                    AND (`bm`.`Bill_date` BETWEEN `bo`.`start_date`
                                              AND `bo`.`end_date`))))
           JOIN
              `b_plan_master` `bpm`
           ON ((`bo`.`plan_id` = `bpm`.`id`)));
