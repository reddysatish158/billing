package org.mifosplatform.billing.selfcare.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SelfCareEmailIdDuplicateException extends AbstractPlatformDomainRuleException{

	public SelfCareEmailIdDuplicateException(final String emailId){
		 super("error.msg.billing.emailId.duplicate.found", "EmailId already exist with this " + emailId);
	}
	
	
	

}