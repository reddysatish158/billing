
delete from stretchy_report where report_name='Clientcounts';
delete from stretchy_report where report_name='PaymodeCollection Chart1';

insert into `stretchy_report`
(`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,
`description`,`core_report`,`use_report`) 
values 
('Clientcounts','Table',Null,'Client' ,
'Select CASE status_enum WHEN 100 THEN "New" WHEN 300 THEN "Active" WHEN 600 THEN "Inactive" ELSE "Pending" END as status, count(0) as ccounts from m_client group by status_enum',
'Clientcounts',1,1) ;

insert into stretchy_report values(null,'PaymodeCollection Chart1','Chart','Pie','Client','select mcv.code_value PayMode ,round(sum(p.amount_paid),2) Collection
from b_payments p, m_code_value mcv ,m_client c, m_office of
where p.paymode_id=mcv.id
and mcv.code_id=11
AND date_format(`payment_date`,\'%Y-%m\')=date_format(now(),\'%Y-%m\')
and p.client_id=c.id 
and c.office_id=of.id
and of.hierarchy like concat((select ino.hierarchy from m_office ino where ino.id = ${officeId}),"%" ) 
group by mcv.code_value','PaymodeCollection Chart1',1,1);

alter table b_clientuser ADD status varchar(10) NOT NULL after password;
update b_clientuser set status = 'ACTIVE';

delete from m_permission where code='UPDATECRASH_MEDIADEVICE';
INSERT INTO m_permission VALUES (null,'organisation', 'UPDATECRASH_MEDIADEVICE', 'MEDIADEVICE', 'UPDATECRASH', 0);