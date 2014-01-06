package org.mifosplatform.billing.paymentsgateway.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.paymentsgateway.data.PaymentGatewayData;
import org.mifosplatform.billing.paymode.data.McodeData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayReadPlatformServiceImpl implements PaymentGatewayReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public PaymentGatewayReadPlatformServiceImpl (final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public Long retrieveClientIdForProvisioning(String serialNum) {
		try{
		String sql = "select client_id as clientId from b_item_detail where serial_no like '%"+serialNum+"%' ";
		return jdbcTemplate.queryForLong(sql);
		} catch(EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public List<PaymentGatewayData> retrievePaymentGatewayData() {
		try{
			PaymentMapper mapper=new PaymentMapper();
			String sql = "select "+mapper.schema();
			return jdbcTemplate.query(sql, mapper, new Object[] {});
			} catch(EmptyResultDataAccessException e){
				return null;
			}
	}
	
	private static final class PaymentMapper implements RowMapper<PaymentGatewayData> {

		public String schema() {
			return " p.id as id,p.key_id as serialNo,p.party_id as phoneNo,p.payment_date as paymentDate," +
					" p.amount_paid as amountPaid,p.receipt_no as receiptNo,p.t_details as clientName,p.status as status," +
					" p.obs_id as paymentId from b_paymentgateway p";
		}
		
		@Override
		public PaymentGatewayData mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long id = rs.getLong("id");
			String serialNo = rs.getString("serialNo");
			String phoneNo = rs.getString("phoneNo");
			LocalDate paymentDate=JdbcSupport.getLocalDate(rs,"paymentDate");
			BigDecimal amountPaid = rs.getBigDecimal("amountPaid");
			String receiptNo = rs.getString("receiptNo");
			String clientName = rs.getString("clientName");
			String status = rs.getString("status");
			Long paymentId = rs.getLong("paymentId");
			
			
			return new PaymentGatewayData(id,serialNo,phoneNo,paymentDate,amountPaid,receiptNo,clientName,status,paymentId);
		}

	}

}