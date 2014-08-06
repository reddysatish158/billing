package org.mifosplatform.logistics.itemdetails.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SerialNumberAlreadyExistException extends AbstractPlatformDomainRuleException {

	public SerialNumberAlreadyExistException(String SerialNumber) {		
		super("error.msg.itemdetails.serialnumber.already.exist", "SerialNumber Already Exist with this "+SerialNumber, SerialNumber);
	}
}
