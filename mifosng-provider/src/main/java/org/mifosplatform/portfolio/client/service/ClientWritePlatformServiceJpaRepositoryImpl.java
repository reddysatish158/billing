/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.domain.StatusTypeEnum;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.organisation.address.domain.Address;
import org.mifosplatform.organisation.address.domain.AddressRepository;
import org.mifosplatform.organisation.groupsDetails.domain.GroupsDetails;
import org.mifosplatform.organisation.groupsDetails.domain.GroupsDetailsRepository;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.data.ClientDataValidator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.InvalidClientStateTransitionException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParameters;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParametersRepository;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final OfficeRepository officeRepository;
    private final GroupsDetailsRepository groupsDetailsRepository;
    private final PlanRepository planRepository;
    private final OrderRepository orderRepository;
    private final ServiceParametersRepository serviceParametersRepository;
    private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final AddressRepository addressRepository;
    private final CodeValueRepository codeValueRepository;
    private final OrderReadPlatformService orderReadPlatformService;
    private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
    private final ProvisioningWritePlatformService ProvisioningWritePlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
  
    
   

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final AddressRepository addressRepository,
            final ClientRepositoryWrapper clientRepository, final OfficeRepository officeRepository,final ClientDataValidator fromApiJsonDeserializer, 
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,
            final ServiceParametersRepository serviceParametersRepository,final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
            final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final CodeValueRepository codeValueRepository,
            final OrderReadPlatformService orderReadPlatformService,final ProvisioningWritePlatformService  ProvisioningWritePlatformService,
            final GroupsDetailsRepository groupsDetailsRepository,final OrderRepository orderRepository,final PlanRepository planRepository,
            final PrepareRequestWriteplatformService prepareRequestWriteplatformService,final ClientReadPlatformService clientReadPlatformService) {
    	
        this.context = context;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.orderRepository=orderRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.codeValueRepository=codeValueRepository;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.serviceParametersRepository = serviceParametersRepository;
        this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
        this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;
        this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.addressRepository=addressRepository;
        this.orderReadPlatformService=orderReadPlatformService;
        this.ProvisioningWritePlatformService=ProvisioningWritePlatformService;
        this.groupsDetailsRepository=groupsDetailsRepository;
        this.planRepository=planRepository;
        this.clientReadPlatformService = clientReadPlatformService;
       
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId,final JsonCommand command) {
    	/*

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        if (client.isNotPending()) { throw new ClientMustBePendingToBeDeletedException(clientId); }

        List<Note> relatedNotes = this.noteRepository.findByClientId(clientId);
        this.noteRepository.deleteInBatch(relatedNotes);

        this.clientRepository.delete(client);

        return new CommandProcessingResultBuilder() //
                .withOfficeId(client.officeId()) //
                .withClientId(clientId) //
                .withEntityId(clientId) //
                .build();
    */

        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);
            final CodeValue closureReason = this.codeValueRepository.findByCodeNameAndId(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);
            
            List<OrderData> orderDatas=this.orderReadPlatformService.getActivePlans(clientId, null);
            
            if(!orderDatas.isEmpty()){
            	
            	 throw new ActivePlansFoundException(clientId);
            }

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is alread closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } 

            if (client.isNotPending() && client.getActivationLocalDate().isAfter(closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationLocalDate());
            }

            client.close(currentUser,closureReason, closureDate.toDate());
            this.clientRepository.saveAndFlush(client);
            
            List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CLOSE_CLIENT);
			if(actionDetaislDatas.size() != 0){
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(), clientId.toString());
			}

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    
    	}

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }else if (realCause.getMessage().contains("email_key")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.email", "Client with email `" + email
                    + "` already exists", "email", email);
            
        }else if (realCause.getMessage().contains("login_key")) {
            final String login = command.stringValueOfParameterNamed("login");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.login", "Client with login `" + login
                    + "` already exists", "login", login);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);

            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);

            Group clientParentGroup = null;
           /* if (groupId != null) {
                clientParentGroup = this.groupRepository.findOne(groupId);
                if (clientParentGroup == null) { throw new GroupNotFoundException(groupId); }
            }*/

            final Client newClient = Client.createNew(clientOffice, clientParentGroup, command);
            this.clientRepository.save(newClient);
           final Address address = Address.fromJson(newClient.getId(),command);
			this.addressRepository.save(address);

            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                        .determineClientAccountNoGenerator(newClient.getId());
                newClient.updateAccountNo(accountNoGenerator.generate());
                this.clientRepository.save(newClient);
            }
            
            List<ActionDetaislData> actionDetailsDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_CLIENT);
            if(!actionDetailsDatas.isEmpty()){
            this.actiondetailsWritePlatformService.AddNewActions(actionDetailsDatas,newClient.getId(),newClient.getId().toString());
            }
            
            transactionHistoryWritePlatformService.saveTransactionHistory(newClient.getId(), "New Client", newClient.getActivationDate(),
            		"Name:"+newClient.getName(),"ImageKey:"+newClient.imageKey(),"AccountNumber:"+newClient.getAccountNo());
            
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientOffice.getId()) //
                    .withClientId(newClient.getId())
                    .withResourceIdAsString(newClient.getId().toString())//
                    .withGroupId(groupId) //
                    .withEntityId(newClient.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            
            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }
            final Map<String, Object> changes = clientForUpdate.update(command);
            clientForUpdate.setOffice(clientOffice);
            this.clientRepository.saveAndFlush(clientForUpdate);
            
            if (changes.containsKey(ClientApiConstants.groupParamName)) {
            	
            	   List<ServiceParameters> serviceParameters=this.serviceParametersRepository.findGroupNameByclientId(clientId);
            	   String newGroup=null;
            	   if(clientForUpdate.getGroupName() != null){
            		   GroupsDetails groupsDetails=this.groupsDetailsRepository.findOne(clientForUpdate.getGroupName());
            		   newGroup=groupsDetails.getGroupName();
            	   }
            		   for(ServiceParameters serviceParameter:serviceParameters){
            		   
            		   Order  order=this.orderRepository.findOne(serviceParameters.get(0).getOrderId());
            		   
            		   Plan plan=this.planRepository.findOne(order.getPlanId());
            		   String oldGroup=serviceParameter.getParameterValue();
            		   if(newGroup == null){
            			   newGroup=plan.getPlanCode();
            		   }
            		   serviceParameter.setParameterValue(newGroup);
            		   this.serviceParametersRepository.saveAndFlush(serviceParameter);
            		   
                      if(order.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
            		    CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, UserActionStatusTypeEnum.CHANGE_GROUP.toString());
               	        this.ProvisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getCode(),UserActionStatusTypeEnum.CHANGE_GROUP.toString(),
               			processingResult.resourceId(),oldGroup,null,order.getId());
                      }
            	   }
            		
            	}
           
            transactionHistoryWritePlatformService.saveTransactionHistory(clientForUpdate.getId(), "Update Client", clientForUpdate.getActivationDate(),
            		"Changes:"+changes.toString(),"Name:"+clientForUpdate.getName(),"ImageKey:"+clientForUpdate.imageKey(),"AccountNumber:"+clientForUpdate.getAccountNo());
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientForUpdate.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            client.activate(fmt, activationDate);

            this.clientRepository.saveAndFlush(client);
            transactionHistoryWritePlatformService.saveTransactionHistory(client.getId(), "ActivateClient", client.getActivationDate(),
            		"Name:"+client.getName(),"ImageKey:"+client.imageKey(),"AccountNumber:"+client.getAccountNo());

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final String imageName, final InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private CommandProcessingResult updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.updateImageKey(imageLocation);
        this.clientRepository.save(client);

        return new CommandProcessingResult(clientId);
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

	@Override
	public CommandProcessingResult updateClientTaxExemption(Long clientId,JsonCommand command) {
		
		Client clientTaxStatus=null;
		
		try{
			 clientTaxStatus = this.clientRepository.findOneWithNotFoundDetection(clientId);
			 char taxValue=clientTaxStatus.getTaxExemption();
			 final boolean taxStatus=command.booleanPrimitiveValueOfParameterNamed("taxExemption");
			 if(taxStatus){
				  taxValue='Y';
				  clientTaxStatus.setTaxExemption(taxValue);
			 }else{
				 taxValue='N';
				 clientTaxStatus.setTaxExemption(taxValue);
			 }
		}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
	            return CommandProcessingResult.empty();
		}
		return new CommandProcessingResultBuilder().withEntityId(clientTaxStatus.getId()).build();
	}

	@Override
	public CommandProcessingResult updateClientBillMode(Long clientId,JsonCommand command) {
		
		Client clientBillMode=null;
	
		try{
			 this.fromApiJsonDeserializer.ValidateBillMode(command);
			 clientBillMode=this.clientRepository.findOneWithNotFoundDetection(clientId);
			 final String billMode=command.stringValueOfParameterNamed("billMode");
			 if(billMode.equals(clientBillMode.getBillMode())==false){
				 clientBillMode.setBillMode(billMode);
			 }else{
				 
			 }
		}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
	            return CommandProcessingResult.empty();
		}
		
		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientBillMode.getId()).build();
	}

	@Override
	public CommandProcessingResult createClientParent(Long entityId,JsonCommand command) {
		Client childClient=null;
		Client parentClient=null;
		try {
			this.fromApiJsonDeserializer.ValidateParent(command);
			final String parentAcntId=command.stringValueOfParameterNamed("accountNo");
			childClient = this.clientRepository.findOneWithNotFoundDetection(entityId);
			Boolean count =this.clientReadPlatformService.countChildClients(entityId);
			parentClient=this.clientRepository.findOneWithAccountId(parentAcntId);
	    	if(parentClient.getParentId() == null && !parentClient.getId().equals(childClient.getId())&&count.equals(false)){	
				childClient.setParentId(parentClient.getId());
				this.clientRepository.save(childClient);
			}else if(parentClient.getId().equals(childClient.getId())){
				final String errorMessage="himself can not be parent to his account.";
				throw new InvalidClientStateTransitionException("Not parent", "himself.can.not.be.parent.to his.account", errorMessage);
			}else if(count){ 
				final String errorMessage="he is already parent to some other clients";
				throw new InvalidClientStateTransitionException("Not Parent", "he.is. already. a parent.to.some other clients", errorMessage);
			}else{
				final String errorMessage="can not be parent to this account.";
				throw new InvalidClientStateTransitionException("Not parent", "can.not.be.parent.to this.account", errorMessage);
			  }
			
			}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
	            return CommandProcessingResult.empty();
		}
		 return new CommandProcessingResultBuilder().withEntityId(childClient.getId()).build();
	}

}