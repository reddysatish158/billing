/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.activationprocess.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetails;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.logistics.itemdetails.exception.SerialNumberAlreadyExistException;
import org.mifosplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleWritePlatformService;
import org.mifosplatform.logistics.ownedhardware.service.OwnedHardwareWritePlatformService;
import org.mifosplatform.organisation.address.data.AddressData;
import org.mifosplatform.organisation.address.service.AddressReadPlatformService;
import org.mifosplatform.portfolio.activationprocess.serialization.ActivationProcessCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
@Service
public class ActivationProcessWritePlatformServiceJpaRepositoryImpl implements ActivationProcessWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ActivationProcessWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private FromJsonHelper fromJsonHelper;
    private final ClientWritePlatformService clientWritePlatformService;
    private final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService;
    private final OrderWritePlatformService orderWritePlatformService;
    private final GlobalConfigurationRepository configurationRepository;
	private final OwnedHardwareWritePlatformService ownedHardwareWritePlatformService;
	private final AddressReadPlatformService addressReadPlatformService;
	private final ActivationProcessCommandFromApiJsonDeserializer commandFromApiJsonDeserializer;
	private final ItemDetailsRepository itemDetailsRepository;
	
	
	
    @Autowired
    public ActivationProcessWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final FromJsonHelper fromJsonHelper,
    		final ClientWritePlatformService clientWritePlatformService,final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService,
    		final OrderWritePlatformService orderWritePlatformService,final GlobalConfigurationRepository globalConfigurationRepository,
    		final OwnedHardwareWritePlatformService ownedHardwareWritePlatformService, final AddressReadPlatformService addressReadPlatformService,
    		final ActivationProcessCommandFromApiJsonDeserializer commandFromApiJsonDeserializer, final ItemDetailsRepository itemDetailsRepository) {
        
    	this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.clientWritePlatformService = clientWritePlatformService;
        this.oneTimeSaleWritePlatformService = oneTimeSaleWritePlatformService;
        this.orderWritePlatformService = orderWritePlatformService;
        this.configurationRepository = globalConfigurationRepository;
        this.ownedHardwareWritePlatformService = ownedHardwareWritePlatformService;
        this.addressReadPlatformService = addressReadPlatformService;
        this.commandFromApiJsonDeserializer = commandFromApiJsonDeserializer;
        this.itemDetailsRepository = itemDetailsRepository;
        
    }

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
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult activationProcess(final JsonCommand command) {

        try {
            context.authenticatedUser();
            CommandProcessingResult resultClient=null;
            CommandProcessingResult resultSale=null;
            CommandProcessingResult resultAllocate=null;
            CommandProcessingResult resultOrder=null;
            final JsonElement element = fromJsonHelper.parse(command.json());
	        JsonArray clientData = fromJsonHelper.extractJsonArrayNamed("client", element);
	        JsonArray saleData = fromJsonHelper.extractJsonArrayNamed("sale", element);
	        JsonArray owndevices= fromJsonHelper.extractJsonArrayNamed("owndevice", element);
	        JsonArray allocateData = fromJsonHelper.extractJsonArrayNamed("allocate", element);
	        JsonArray bookOrder = fromJsonHelper.extractJsonArrayNamed("bookorder", element);
	        
	        
	       
	        for(JsonElement j:clientData){
           
	        	JsonCommand comm=new JsonCommand(null, j.toString(),j, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
	        	resultClient=this.clientWritePlatformService.createClient(comm);
	        }

	        GlobalConfigurationProperty configuration=configurationRepository.findOneByName(ConfigurationConstants.CPE_TYPE);
	        if(configuration.getValue().equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_SALE)){
	             
	        	for(JsonElement sale:saleData){
	        	  JsonCommand comm=new JsonCommand(null, sale.toString(),sale, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
	        	  resultSale=this.oneTimeSaleWritePlatformService.createOneTimeSale(comm,resultClient.getClientId());
	           }
	        }else if(configuration.getValue().equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_OWN)){
	        	for(JsonElement ownDevice:owndevices){
	        		
	        		  JsonCommand comm=new JsonCommand(null, ownDevice.toString(),ownDevice, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
		        	  resultSale=this.ownedHardwareWritePlatformService.createOwnedHardware(comm,resultClient.getClientId());
	        	}
	        	
	        }
	       
	         for(JsonElement order:bookOrder){
		        	
		        	JsonCommand comm=new JsonCommand(null, order.toString(),order, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null,null);
		        	resultOrder=this.orderWritePlatformService.createOrder(resultClient.getClientId(),comm);
		        
		        }
	        return resultClient;

           
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(-1l).empty();
        }
	
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

	@SuppressWarnings("unused")
	@Override
	public CommandProcessingResult selfRegistrationProcess(JsonCommand command) {

		try {
			context.authenticatedUser();
			commandFromApiJsonDeserializer.validateForCreate(command.json());
			Long id = new Long(1);
			String fullname = command.stringValueOfParameterNamed("fullname");
			String city = command.stringValueOfParameterNamed("city");
			Long phone = command.longValueOfParameterNamed("phone");
			String device = command.stringValueOfParameterNamed("device");
			String email = command.stringValueOfParameterNamed("email");

			ItemDetails detail = itemDetailsRepository.findOneBySerialNo(device);

			if (detail == null) {
				throw new SerialNumberNotFoundException(device);
			}

			if (detail != null && detail.getStatus().equalsIgnoreCase("Used")) {
				throw new SerialNumberAlreadyExistException(device);
			}

			CommandProcessingResult resultClient = null;
			CommandProcessingResult resultSale = null;
			CommandProcessingResult resultOrder = null;

			// client creation
			AddressData addressData = this.addressReadPlatformService.retrieveName(city);
			String dateFormat = "dd MMMM yyyy";

			String activationDate = new SimpleDateFormat(dateFormat).format(new Date());

			JSONObject clientcreation = new JSONObject();
			clientcreation.put("officeId", new Long(1));
			clientcreation.put("clientCategory", new Long(1));
			clientcreation.put("firstname", fullname);
			clientcreation.put("lastname", "Mr.");
			clientcreation.put("phone", phone);
			clientcreation.put("groupId", new Long(1));
			clientcreation.put("addressNo", "Address");
			clientcreation.put("city", addressData.getCity());
			clientcreation.put("state", addressData.getState());
			clientcreation.put("country", addressData.getCountry());
			clientcreation.put("email", email);
			clientcreation.put("locale", "en");
			clientcreation.put("active", true);
			clientcreation.put("dateFormat", dateFormat);
			clientcreation.put("activationDate", activationDate);
			clientcreation.put("flag", false);

			final JsonElement element = fromJsonHelper.parse(clientcreation.toString());
			JsonCommand clientCommand = new JsonCommand(null,
					clientcreation.toString(), element, fromJsonHelper, null,
					null, null, null, null, null, null, null, null, null, null,
					null);
			resultClient = this.clientWritePlatformService.createClient(clientCommand);

			if (resultClient == null) {
				throw new PlatformDataIntegrityException("error.msg.client.creation", "Client Creation Failed","Client Creation Failed");
			}

			// book device

			// GlobalConfigurationProperty
			// configuration=configurationRepository.findOneByName(ConfigurationConstants.CPE_TYPE);
			// if(configuration.getValue().equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_SALE)){

			JSONObject serialNumberObject = new JSONObject();
			serialNumberObject.put("serialNumber", device);
			serialNumberObject.put("clientId", resultClient.getClientId());
			serialNumberObject.put("status", "allocated");
			serialNumberObject.put("itemMasterId", detail.getItemMasterId());
			serialNumberObject.put("isNewHw", "Y");

			JSONArray serialNumber = new JSONArray();
			serialNumber.put(0, serialNumberObject);

			JSONObject bookDevice = new JSONObject();
			bookDevice.put("chargeCode", "NONE");
			bookDevice.put("unitPrice", new Long(100));
			bookDevice.put("itemId", id);
			bookDevice.put("discountId", id);
			bookDevice.put("officeId", id);
			bookDevice.put("totalPrice", new Long(100));
			bookDevice.put("quantity", id);
			bookDevice.put("locale", "en");
			bookDevice.put("dateFormat", dateFormat);
			bookDevice.put("saleType", "SecondSale");
			bookDevice.put("saleDate", activationDate);
			bookDevice.put("serialNumber", serialNumber);

			final JsonElement deviceElement = fromJsonHelper.parse(bookDevice.toString());
			JsonCommand comm = new JsonCommand(null, bookDevice.toString(),
					deviceElement, fromJsonHelper, null, null, null, null,
					null, null, null, null, null, null, null, null);
			resultSale = this.oneTimeSaleWritePlatformService.createOneTimeSale(comm, resultClient.getClientId());

			/*
			 * }else
			 * if(configuration.getValue().equalsIgnoreCase(ConfigurationConstants
			 * .CONFIR_PROPERTY_OWN)){ for(JsonElement ownDevice:owndevices){
			 * 
			 * JsonCommand comm=new JsonCommand(null,
			 * ownDevice.toString(),ownDevice, fromJsonHelper, null, null, null,
			 * null, null, null, null, null, null, null, null,null);
			 * resultSale=this
			 * .ownedHardwareWritePlatformService.createOwnedHardware
			 * (comm,resultClient.getClientId()); }
			 * 
			 * }
			 */

			// book order
			GlobalConfigurationProperty selfregistrationconfiguration = configurationRepository
					.findOneByName(ConfigurationConstants.CONFIR_PROPERTY_SELF_REGISTRATION);
			if (selfregistrationconfiguration != null) {
				if (selfregistrationconfiguration.isEnabled()) {
					JSONObject orderJson = new JSONObject(selfregistrationconfiguration.getValue());
					if (orderJson.getString("paytermCode") != null && Long.valueOf(orderJson.getLong("planCode")) != null
							&& Long.valueOf(orderJson.getLong("contractPeriod")) != null) {
						orderJson.put("locale", "en");
						orderJson.put("isNewplan", true);
						orderJson.put("dateFormat", dateFormat);
						orderJson.put("start_date", activationDate);
						final JsonElement orderElement = fromJsonHelper.parse(orderJson.toString());
						JsonCommand orderCommand = new JsonCommand(null,
								orderJson.toString(), orderElement,
								fromJsonHelper, null, null, null, null, null,
								null, null, null, null, null, null, null);
						resultOrder = this.orderWritePlatformService.createOrder(resultClient.getClientId(),orderCommand);
					}
				}
				if (resultOrder == null) {
					throw new PlatformDataIntegrityException("error.msg.client.order.creation","Book Order Failed for ClientId:"
									+ resultClient.getClientId(),"Book Order Failed");
				}

			}

			// create selfcare record
			SelfCare selfcare = new SelfCare(resultClient.getClientId(),fullname, "1234", email, false);
			if (selfcare == null) {
				throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
			}

			return resultClient;

		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(-1l).empty();
		} catch (JSONException e) {
			return new CommandProcessingResult(-1l).empty();
		}

	}
}
