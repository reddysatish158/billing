package org.mifosplatform.provisioning.processscheduledjobs.service;

public interface SheduleJobWritePlatformService {

	//void runSheduledJobs();

	void processInvoice();
	
	void processRequest();
	
	void processResponse();
	
	void processSimulator();
	
	void generateStatment();
	
	void processingMessages();
	
	void processingAutoExipryOrders();
	
	void processNotify();
	
	void processMiddleware();
	
	void eventActionProcessor();
	
	void reportEmail();
    


	
}