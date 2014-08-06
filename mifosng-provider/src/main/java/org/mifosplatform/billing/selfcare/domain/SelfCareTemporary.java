package org.mifosplatform.billing.selfcare.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name="temp")
public class SelfCareTemporary extends AbstractAuditableCustom<AppUser, Long>{
	
	@Column(name="username")
	private String userName;
	
	@Column(name="generate_key")
	private String generatedKey;
	
	@Column(name="status")
	private String status;
	
	public SelfCareTemporary(){
		
	}
	
	public SelfCareTemporary(String userName, String generatedKey){
		
		this.userName = userName;
		this.generatedKey = generatedKey;
		this.status="INACTIVE";
		
	}
	public static SelfCareTemporary fromJson(JsonCommand command) {
		String userName = command.stringValueOfParameterNamed("userName");
		SelfCareTemporary selfCareTemporary = new SelfCareTemporary();
		selfCareTemporary.setUserName(userName);
		selfCareTemporary.setStatus("INACTIVE");
		return selfCareTemporary;
		
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGeneratedKey() {
		return generatedKey;
	}

	public void setGeneratedKey(String generatedKey) {
		this.generatedKey = generatedKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
