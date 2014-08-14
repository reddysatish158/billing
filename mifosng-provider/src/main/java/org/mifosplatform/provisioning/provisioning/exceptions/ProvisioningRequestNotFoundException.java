package org.mifosplatform.provisioning.provisioning.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ProvisioningRequestNotFoundException extends AbstractPlatformDomainRuleException {

	public ProvisioningRequestNotFoundException(Long provisionId) {
		super("error.msg.provisioning.request.not.found.with.this.identifier","provisioning request not found with this identifier",provisionId);
		
	}

}
