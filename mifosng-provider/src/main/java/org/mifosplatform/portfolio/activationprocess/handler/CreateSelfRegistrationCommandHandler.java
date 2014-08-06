package org.mifosplatform.portfolio.activationprocess.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.activationprocess.service.ActivationProcessWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSelfRegistrationCommandHandler implements NewCommandSourceHandler {

	private final ActivationProcessWritePlatformService activationProcessWritePlatformService;

    @Autowired
    public CreateSelfRegistrationCommandHandler(final ActivationProcessWritePlatformService activationProcessWritePlatformService) {
        this.activationProcessWritePlatformService = activationProcessWritePlatformService;
    }

    @Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
    	 return this.activationProcessWritePlatformService.selfRegistrationProcess(command);
	}

}
