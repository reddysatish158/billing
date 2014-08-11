INSERT IGNORE INTO  m_permission VALUES(null, 'portfolio', 'CREATE_PARENTCLIENT', 'PARENTCLIENT', 'CREATE', '0');
  
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
