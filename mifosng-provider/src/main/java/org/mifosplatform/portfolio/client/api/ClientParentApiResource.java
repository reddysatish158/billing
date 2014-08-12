package org.mifosplatform.portfolio.client.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.service.ClientCategoryData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


	@Path("/parentclient")
	@Component
	@Scope("singleton")
	public class ClientParentApiResource {
		 private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("id","displayName","accountNo"));
	     private final String resourceNameForPermissions = "PARENT";
		 private final PlatformSecurityContext context;
		 private final DefaultToApiJsonSerializer<ClientCategoryData> toApiJsonSerializer;
		 private final ApiRequestParameterHelper apiRequestParameterHelper;
		 private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
		 private final ClientReadPlatformService clientReadPlatformService;
		 private final ClientRepositoryWrapper clientRepository;
		    
		    
	 @Autowired
      public ClientParentApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<ClientCategoryData> toApiJsonSerializer, 
		    final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
     		final ClientReadPlatformService clientReadPlatformService,final ClientRepositoryWrapper clientRepository){
		   this.context = context;
           this.toApiJsonSerializer = toApiJsonSerializer;
           this.apiRequestParameterHelper = apiRequestParameterHelper;
		   this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
           this.clientReadPlatformService =  clientReadPlatformService;
           this.clientRepository = clientRepository;
				
			   }		 
	
	
          @GET
	      @Consumes({MediaType.APPLICATION_JSON})
	      @Produces({MediaType.APPLICATION_JSON})
	      public String retriveParentclients(@QueryParam("query") final String query, @Context final UriInfo uriInfo){
		        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		        if(query != null && query.length()>0){
			    List<ClientCategoryData> parentClientData = this.clientReadPlatformService.retrievingParentClients(query);
			   final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			    return this.toApiJsonSerializer.serialize(settings, parentClientData, RESPONSE_DATA_PARAMETERS);
		        }else{
			   return null;
		     }
        }
   
          @PUT
          @Path("{clientId}")
          @Consumes({ MediaType.APPLICATION_JSON })
	      @Produces({ MediaType.APPLICATION_JSON })
	      public String createParentDetails(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
	        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientParent(clientId).withJson(apiRequestBodyAsJson).build();
	        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	        return this.toApiJsonSerializer.serialize(result);
   
           }
          
          @GET
          @Path("{clientId}")
	      @Consumes({MediaType.APPLICATION_JSON})
	      @Produces({MediaType.APPLICATION_JSON})
	      public String retriveClientParent(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo){
		        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		        Client client=this.clientRepository.findOneWithNotFoundDetection(clientId);
			    ClientCategoryData parentClientData = this.clientReadPlatformService.retrievingClientParentData(client.getParentId());
			    return this.toApiJsonSerializer.serialize(parentClientData);
		        
        }
          
	}