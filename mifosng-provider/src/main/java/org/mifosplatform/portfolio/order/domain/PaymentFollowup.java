package org.mifosplatform.portfolio.order.domain;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "b_payment_followup")
public class PaymentFollowup extends AbstractAuditableCustom<AppUser, Long> {

	

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "followup_date")
	private Date followupDate;
	
	@Column(name = "followup_reason")
	private String followupReason;

	@Column(name = "followup_desc")
	private String followupDesc;

	@Column(name = "current_status")
	private String currentStatus;

	@Column(name = "requested_status")
	private String requestedStatus;
	
	@Column(name = "reactive_date")
	private Date reactiveDate;
	
	public PaymentFollowup(){
		
	}
	
	public PaymentFollowup(Long clientId, Long orderId, LocalDate followupDate, String followupReason, String followupDesc,
			String currentStatus, String requestedStatus) {
		this.clientId=clientId;
		this.orderId=orderId;
		this.followupDate=followupDate.toDate();
		this.followupReason=followupReason;
		this.followupDesc=followupDesc;
		this.currentStatus=currentStatus;
		this.requestedStatus=requestedStatus;
	}

	public static PaymentFollowup fromJson(JsonCommand command, Long clientId, Long orderId, String currentStatus, String requestedStatus) {
		 
		final String followupReason = command.stringValueOfParameterNamed("suspensionReason");
		final LocalDate followupDate=command.localDateValueOfParameterNamed("suspensionDate");
		final String followupDesc = command.stringValueOfParameterNamed("suspensionDescription");
		    return new PaymentFollowup(clientId,orderId,followupDate,followupReason,followupDesc,currentStatus,requestedStatus);
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Date getFollowupDate() {
		return followupDate;
	}

	public String getFollowupReason() {
		return followupReason;
	}

	public String getFollowupDesc() {
		return followupDesc;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public String getRequestedStatus() {
		return requestedStatus;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}

	public void setFollowupReason(String followupReason) {
		this.followupReason = followupReason;
	}

	public void setFollowupDesc(String followupDesc) {
		this.followupDesc = followupDesc;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public void setRequestedStatus(String requestedStatus) {
		this.requestedStatus = requestedStatus;
	}

	public void setReactiveDate(Date reactiveDate) {
		this.reactiveDate = reactiveDate;
	}
 
	
	
}