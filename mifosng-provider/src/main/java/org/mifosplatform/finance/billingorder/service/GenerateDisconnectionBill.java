package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.mifosplatform.billing.taxmaster.data.TaxMappingRateData;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.domain.BillingOrderRepository;
import org.mifosplatform.finance.data.DiscountMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenerateDisconnectionBill {

	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final InvoiceTaxPlatformService invoiceTaxPlatformService;
	private final BillingOrderRepository billingOrderRepository;
	// private final OrderRepository orderRepository;

	@Autowired
	public GenerateDisconnectionBill(
			BillingOrderReadPlatformService billingOrderReadPlatformService,
			InvoiceTaxPlatformService invoiceTaxPlatformService,
			final BillingOrderRepository billingOrderRepository) {
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.invoiceTaxPlatformService = invoiceTaxPlatformService;
		this.billingOrderRepository = billingOrderRepository;
		// this.orderRepository = orderRepository;
	}

	BigDecimal pricePerMonth = null;
	LocalDate startDate = null;
	LocalDate endDate = null;
	BigDecimal price = null;
	LocalDate billEndDate = null;
	LocalDate invoiceTillDate = null;
	LocalDate nextbillDate = null;
	BillingOrderCommand billingOrderCommand = null;

	public boolean isChargeTypeNRC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("NRC")) {
			chargeType = true;
		}
		return chargeType;
	}

	public boolean isChargeTypeRC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("RC")) {
			chargeType = true;
		}
		return chargeType;
	}

	public boolean isChargeTypeUC(BillingOrderData billingOrderData) {
		boolean chargeType = false;
		if (billingOrderData.getChargeType().equals("UC")) {
			chargeType = true;
		}
		return chargeType;
	}

	// Monthly Bill
	public BillingOrderCommand getReverseMonthyBill(BillingOrderData billingOrderData,
			DiscountMasterData discountMasterData, LocalDate disconnectionDate) {
		
	    BigDecimal discountAmount = BigDecimal.ZERO;
	    BigDecimal netAmount=BigDecimal.ZERO;
		BigDecimal disconnectionCreditForMonths = BigDecimal.ZERO;
		BigDecimal disconnectionCreditPerday = BigDecimal.ZERO;
		BigDecimal disconnectionCreditForDays = BigDecimal.ZERO;
		List<InvoiceTaxCommand> listOfTaxes =new ArrayList<InvoiceTaxCommand>();
		int numberOfDays = 0;
		int totalDays = 0;
		
		billEndDate = new LocalDate(billingOrderData.getBillEndDate());
		price = billingOrderData.getPrice();
		
		// If Invoice till date not equal to null
		if (billingOrderData.getInvoiceTillDate() != null) {
			
           if(discountMasterData.getdiscountRate() !=null&& (billingOrderData.getBillStartDate().after(discountMasterData.getDiscountStartDate().toDate())
        		   ||(billingOrderData.getBillStartDate().compareTo(discountMasterData.getDiscountStartDate().toDate())==0))){

    		if (discountMasterData.getDiscounType().equalsIgnoreCase("percentage")){
    			discountAmount = price.multiply(discountMasterData.getdiscountRate().divide(new BigDecimal(100)));
	               price = price.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    		 }else if(discountMasterData.getDiscounType().equalsIgnoreCase("flat")){
    		     price = price.subtract(discountMasterData.getdiscountRate());
              }
           }
			
			this.startDate = disconnectionDate;
			this.endDate = new LocalDate(billingOrderData.getInvoiceTillDate());
			invoiceTillDate = new LocalDate(billingOrderData.getInvoiceTillDate());
			int maxDaysOfMonth =startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
			int  maximumDaysInYear = startDate.dayOfYear().withMaximumValue().getDayOfYear();
			LocalDate nextDurationDate=startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
			totalDays =Days.daysBetween(startDate, nextDurationDate).getDays()+1;
			int numberOfMonths = Months.monthsBetween(disconnectionDate, invoiceTillDate).getMonths();
			System.out.println(numberOfMonths);
			if (billingOrderData.getBillingAlign().equalsIgnoreCase("N")) {
				LocalDate tempBillEndDate = invoiceTillDate.minusMonths(numberOfMonths);
				numberOfDays = Days.daysBetween(disconnectionDate, tempBillEndDate).getDays();
				System.out.println(numberOfDays);
				
			} else if (billingOrderData.getBillingAlign().equalsIgnoreCase("Y")) {
                if(numberOfMonths>0){
				 LocalDate tempBillEndDate = invoiceTillDate.minusMonths(numberOfMonths).dayOfMonth().withMaximumValue();
				 numberOfDays = Days.daysBetween(disconnectionDate, tempBillEndDate).getDays();
				 System.out.println(numberOfDays);
                }
				else{
					/*LocalDate tempBillEndDate = invoiceTillDate.minusMonths(numberOfMonths).dayOfMonth().withMaximumValue()*/;
					numberOfDays = Days.daysBetween(disconnectionDate, invoiceTillDate).getDays();
					System.out.println(numberOfDays);
				}
			}
            //calculate amount for one month
			if (numberOfMonths != 0) {
			   if(billingOrderData.getChargeDuration()==12){
				    netAmount=price.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
				    disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
			     }else if(billingOrderData.getChargeDuration()==6){
					netAmount=price.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
					disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
				}else if(billingOrderData.getChargeDuration()==3){
					netAmount=price.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
					disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
				}else if(billingOrderData.getChargeDuration()==2){
					netAmount=price.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
					disconnectionCreditForMonths = netAmount.multiply(new BigDecimal(numberOfMonths));
				}else{
				    disconnectionCreditForMonths = price.multiply(new BigDecimal(numberOfMonths));//monthly
			   }
			}//calculate amount for one Day
			 if(billingOrderData.getChargeDuration()==12){
			        disconnectionCreditPerday = price.divide(new BigDecimal(maximumDaysInYear), 2,RoundingMode.HALF_UP);
			 }else if(billingOrderData.getChargeDuration()==6){
			        disconnectionCreditPerday = price.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
			 }else if(billingOrderData.getChargeDuration()==3){
			        disconnectionCreditPerday = price.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
			 }else if(billingOrderData.getChargeDuration()==2){
			        disconnectionCreditPerday = price.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
			 }else{
				   disconnectionCreditPerday = price.divide(new BigDecimal(maxDaysOfMonth), 2,RoundingMode.HALF_UP);
			 }
			if (numberOfDays != 0) {
				disconnectionCreditForDays = disconnectionCreditPerday.multiply(new BigDecimal(numberOfDays));
			}
			price = disconnectionCreditForMonths.add(disconnectionCreditForDays);//final case
			
			this.startDate=invoiceTillDate;
			this.endDate = disconnectionDate;
			this.invoiceTillDate =disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			billingOrderData.setChargeType("DC");
			
			 listOfTaxes = this.calculateTax(billingOrderData, price,disconnectionDate);
			
			
		 }else if (billingOrderData.getInvoiceTillDate() == null) { //If Invoice till date equal to null
			 
		
			this.startDate = new LocalDate(billingOrderData.getBillStartDate());
			this.endDate = disconnectionDate;
			this.invoiceTillDate = disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);
			int maxDaysOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
			int maxDaysInYear =startDate.dayOfYear().withMaximumValue().getDayOfYear();
			LocalDate nextDurationDate	=startDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
		    numberOfDays = Days.daysBetween(startDate, invoiceTillDate).getDays()+1;
			
			if(billingOrderData.getChargeDuration()==12){
			   netAmount = price.divide(new BigDecimal(maxDaysInYear),2,RoundingMode.HALF_UP);
			}else if(billingOrderData.getChargeDuration()==6){
			   totalDays = Days.daysBetween(startDate, nextDurationDate).getDays()+1;
			   netAmount = price.divide(new BigDecimal(totalDays),2,RoundingMode.HALF_UP);
			}else if(billingOrderData.getChargeDuration()==3){
			   totalDays = Days.daysBetween(startDate, nextDurationDate).getDays()+1;
			   netAmount = price.divide(new BigDecimal(totalDays),2,RoundingMode.HALF_UP);
		    }else if(billingOrderData.getChargeDuration()==2){
			   totalDays = Days.daysBetween(startDate, nextDurationDate).getDays()+1;
			   netAmount = price.divide(new BigDecimal(totalDays),2,RoundingMode.HALF_UP);
		    }else{
			   netAmount = price.divide(new BigDecimal(maxDaysOfMonth ),2,RoundingMode.HALF_UP);
			}
			price=netAmount.multiply(new BigDecimal(numberOfDays));
			
			 listOfTaxes = this.calculateTax(billingOrderData, price,disconnectionDate);
				
		}
		
		 return this.createBillingOrderCommand(billingOrderData, startDate,
					  endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,	discountMasterData);

	
	}

	// Reverse Weekly Bill
	public BillingOrderCommand getReverseWeeklyBill(BillingOrderData billingOrderData,
			DiscountMasterData discountMasterData, LocalDate disconnectionDate) {

		BigDecimal disconnectionCreditForWeeks = BigDecimal.ZERO;
		BigDecimal disconnectionCreditPerday = BigDecimal.ZERO;
		BigDecimal disconnectionCreditForDays = BigDecimal.ZERO;
		BigDecimal discountAmount = BigDecimal.ZERO;
		BigDecimal netAmount=BigDecimal.ZERO;
		int numberOfDays = 0;
		
		billEndDate = new LocalDate(billingOrderData.getBillEndDate());
		price = billingOrderData.getPrice();

		if (billingOrderData.getInvoiceTillDate() != null) {
			
			  if(discountMasterData.getdiscountRate() !=null && (billingOrderData.getBillStartDate().after(discountMasterData.getDiscountStartDate().toDate())
	        		   ||(billingOrderData.getBillStartDate().compareTo(discountMasterData.getDiscountStartDate().toDate())==0))){

		    		if (discountMasterData.getDiscounType().equalsIgnoreCase("percentage")){
		    			   discountAmount = price.multiply(discountMasterData.getdiscountRate().divide(new BigDecimal(100)));
			               price = price.subtract(discountAmount);
		    		}else if(discountMasterData.getDiscounType().equalsIgnoreCase("flat")){
		    			  price = price.subtract(discountMasterData.getdiscountRate());
		    		
		              }
		           }
		
			
			this.startDate = disconnectionDate;
			this.endDate = new LocalDate(billingOrderData.getInvoiceTillDate());
			invoiceTillDate = new LocalDate(billingOrderData.getInvoiceTillDate());
			int numberOfWeeks = Weeks.weeksBetween(disconnectionDate, invoiceTillDate).getWeeks();
			System.out.println(numberOfWeeks);

			if (billingOrderData.getBillingAlign().equalsIgnoreCase("N")) {
		     	LocalDate tempBillEndDate = invoiceTillDate.minusWeeks(numberOfWeeks);
				numberOfDays = Days.daysBetween(disconnectionDate, tempBillEndDate).getDays();
				System.out.println(	numberOfDays);
				
			} else if (billingOrderData.getBillingAlign().equalsIgnoreCase("Y")) {
				LocalDate tempBillEndDate = invoiceTillDate.minusWeeks(numberOfWeeks).dayOfWeek().withMaximumValue();
				numberOfDays = Days.daysBetween(disconnectionDate, tempBillEndDate).getDays();
				System.out.println(	numberOfDays);
			}

			if (numberOfWeeks != 0) {
				 if(billingOrderData.getChargeDuration() == 2) {
					netAmount=price.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
					disconnectionCreditForWeeks = netAmount.multiply(new BigDecimal(numberOfWeeks));
				}else{
				disconnectionCreditForWeeks = price.multiply(new BigDecimal(numberOfWeeks));	
			}
			}
			if (billingOrderData.getChargeDuration() == 2){
		
			disconnectionCreditPerday = price.divide(new BigDecimal(14), 2,RoundingMode.HALF_UP);
			}else{
			disconnectionCreditPerday = price.divide(new BigDecimal(7), 2,RoundingMode.HALF_UP);
			}
			if (numberOfDays != 0) {
				disconnectionCreditForDays = disconnectionCreditPerday.multiply(new BigDecimal(numberOfDays));
			}

			price = disconnectionCreditForWeeks.add(disconnectionCreditForDays);
			
			billingOrderData.setChargeType("DC");

			this.startDate=invoiceTillDate;
			this.endDate = disconnectionDate;
			this.invoiceTillDate =disconnectionDate;
			this.nextbillDate = invoiceTillDate.plusDays(1);

		}
		
		 else if (billingOrderData.getInvoiceTillDate() == null) {

				this.startDate = new LocalDate(billingOrderData.getBillStartDate());
				this.endDate = disconnectionDate;
				this.invoiceTillDate = disconnectionDate;
				this.nextbillDate = invoiceTillDate.plusDays(1);
				numberOfDays = Days.daysBetween(startDate, nextbillDate).getDays();
				System.out.println(numberOfDays);
				if (billingOrderData.getChargeDuration() == 2){
				 netAmount=price.divide(new BigDecimal(14), 2,RoundingMode.HALF_UP);
				}else{
			     netAmount = price.divide(new BigDecimal(7),2,RoundingMode.HALF_UP);
				}
				price=netAmount.multiply(new BigDecimal(numberOfDays));
						
			}

		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(billingOrderData, price,disconnectionDate);

		return this.createBillingOrderCommand(billingOrderData, startDate,
				endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,discountMasterData);

	}

/*	// Disconnection credit price
	private BigDecimal getDisconnectionCredit(LocalDate startDate,
			LocalDate endDate, BigDecimal amount, String durationType) {

		int currentDay = startDate.getDayOfMonth();

		int totalDays = 0;
		if (startDate.isEqual(endDate)) {
			totalDays = 0;
		} else {
			totalDays = Days.daysBetween(startDate, endDate).getDays() + 1;
		}
		pricePerMonth = amount;
		BigDecimal pricePerDay = BigDecimal.ZERO;

		if (durationType.equalsIgnoreCase("month(s)")) {
			pricePerDay = pricePerMonth.divide(new BigDecimal(30), 2,
					RoundingMode.HALF_UP);

		} else if (durationType.equalsIgnoreCase("week(s)")) {
			pricePerDay = pricePerMonth.divide(new BigDecimal(7), 2,
					RoundingMode.HALF_UP);
		}

		return pricePerDay.multiply(new BigDecimal(totalDays));

	}
*/
/*	// order cancelled bill
	public BillingOrderCommand getCancelledOrderBill(
			BillingOrderData billingOrderData,
			DiscountMasterData discountMasterData) {

		if (billingOrderData.getInvoiceTillDate() == null)
			startDate = new LocalDate(billingOrderData.getStartDate());
		else
			startDate = new LocalDate(billingOrderData.getNextBillableDate());

		endDate = new LocalDate(billingOrderData.getBillEndDate());

		price = this
				.getDisconnectionCredit(startDate, endDate,
						billingOrderData.getPrice(),
						billingOrderData.getDurationType());

		nextbillDate = new LocalDate().plusYears(1000);

		invoiceTillDate = endDate;

		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(
				billingOrderData, price);

		return this.createBillingOrderCommand(billingOrderData, startDate,
				endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,
				discountMasterData);

	}*/

	// Per day weekly price
	public BigDecimal getWeeklyPricePerDay(BillingOrderData billingOrderData) {
		Integer billingDays = 7 * billingOrderData.getChargeDuration();

		return billingOrderData.getPrice().divide(new BigDecimal(billingDays),
				2, RoundingMode.HALF_UP);
	}

	// Daily Bill
	public BillingOrderCommand getDailyBill(BillingOrderData billingOrderData,
			DiscountMasterData discountMasterData) {

		startDate = new LocalDate(billingOrderData.getBillStartDate());
		endDate = startDate;
		invoiceTillDate = endDate;
		nextbillDate = invoiceTillDate.plusDays(1);
		price = billingOrderData.getPrice();

		List<InvoiceTaxCommand> listOfTaxes = this.calculateTax(
				billingOrderData, price,null);

		return this.createBillingOrderCommand(billingOrderData, startDate,
				endDate, invoiceTillDate, nextbillDate, price, listOfTaxes,
				discountMasterData);

	}

	// Tax Calculation
	public List<InvoiceTaxCommand> calculateTax(
			BillingOrderData billingOrderData, BigDecimal billPrice, LocalDate disconnectionDate) {

		List<TaxMappingRateData> taxMappingRateDatas = billingOrderReadPlatformService.retrieveTaxMappingDate(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		if(taxMappingRateDatas.isEmpty()){
			
			 taxMappingRateDatas = billingOrderReadPlatformService.retrieveDefaultTaxMappingDate(billingOrderData.getClientId(),billingOrderData.getChargeCode());
		}
		List<InvoiceTaxCommand> invoiceTaxCommand = this.generateInvoiceTax(taxMappingRateDatas, billPrice, billingOrderData,disconnectionDate);
		// List<InvoiceTax> listOfTaxes =
		// invoiceTaxPlatformService.createInvoiceTax(invoiceTaxCommand);
		return invoiceTaxCommand;
	}
	
	// Generate Invoice Tax
	public List<InvoiceTaxCommand> generateInvoiceTax(
			List<TaxMappingRateData> taxMappingRateDatas, BigDecimal price,
			BillingOrderData billingOrderData,LocalDate disconnectionDate) {
		   BigDecimal taxPercentage = null;
		   BigDecimal taxAmount = null;
		   BigDecimal taxFlat = null;
		   String taxCode = null;
		List<InvoiceTaxCommand> invoiceTaxCommands = new ArrayList<InvoiceTaxCommand>();
		 InvoiceTaxCommand invoiceTaxCommand = null;
		if (taxMappingRateDatas != null) {

			for (TaxMappingRateData taxMappingRateData : taxMappingRateDatas) {

				if (taxMappingRateData.getTaxType().equalsIgnoreCase("Percentage")) {
					taxPercentage = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					taxAmount = price.multiply(taxPercentage.divide(new BigDecimal(100)));
				} else if(taxMappingRateData.getTaxType().equalsIgnoreCase("Flat")) {
					taxFlat = taxMappingRateData.getRate();
					taxCode = taxMappingRateData.getTaxCode();
					if(billingOrderData.getChargeType().equalsIgnoreCase("RC")){
					      taxAmount =taxFlat;
					}else{
					BigDecimal numberOfMonthsPrice = BigDecimal.ZERO;
					BigDecimal numberOfDaysPrice = BigDecimal.ZERO;
					BigDecimal pricePerDay = BigDecimal.ZERO;
					 BigDecimal pricePerMonth = BigDecimal.ZERO;
					LocalDate durationDate=disconnectionDate.plusMonths(billingOrderData.getChargeDuration()).minusDays(1);
					int totalDays = Days.daysBetween(disconnectionDate, durationDate).getDays();
				    int numberOfMonths = Months.monthsBetween(disconnectionDate,new LocalDate(billingOrderData.getInvoiceTillDate())).getMonths();
				    int maximumDaysInMonth = disconnectionDate.dayOfMonth().withMaximumValue().getDayOfMonth();
			        int  maximumDaysInYear = new LocalDate().dayOfYear().withMaximumValue().getDayOfYear();
					int numberOfDays = 0;
					if(numberOfMonths !=0){
						LocalDate tempDate = new LocalDate(billingOrderData.getInvoiceTillDate()).minusMonths(numberOfMonths);
						numberOfDays = Days.daysBetween(new LocalDate(), tempDate).getDays();	
					}else{
						numberOfDays = Days.daysBetween(disconnectionDate,new LocalDate(billingOrderData.getInvoiceTillDate())).getDays();
					}
					if(billingOrderData.getDurationType().equalsIgnoreCase("month(s)")){
					     if(billingOrderData.getChargeDuration()==12){
						       pricePerMonth = taxFlat.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
					           numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxFlat.divide(new BigDecimal(maximumDaysInYear), 2,RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      }else if(billingOrderData.getChargeDuration()==6){
						       pricePerMonth = taxFlat.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
						       numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxFlat.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      }else if(billingOrderData.getChargeDuration()==3){
						       pricePerMonth = taxFlat.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
						       numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxFlat.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      }else if(billingOrderData.getChargeDuration()==2){
						       pricePerMonth = taxFlat.divide(new BigDecimal(billingOrderData.getChargeDuration()), 2,RoundingMode.HALF_UP);
						       numberOfMonthsPrice = pricePerMonth.multiply(new BigDecimal(numberOfMonths));
				    	       pricePerDay = taxFlat.divide(new BigDecimal(totalDays), 2,RoundingMode.HALF_UP);
				    	       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				      }else{
				    	       numberOfMonthsPrice = taxFlat.multiply(new BigDecimal(numberOfMonths));
						       pricePerDay = taxFlat.divide(new BigDecimal(maximumDaysInMonth), 2,RoundingMode.HALF_UP);
						       numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				       }
					} else{ 
						if(billingOrderData.getChargeDuration()==2){
				    	      pricePerDay = taxFlat.divide(new BigDecimal(14), 2,RoundingMode.HALF_UP);
				    	      numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				       }else{
				    	     pricePerDay = taxFlat.divide(new BigDecimal(7), 2,RoundingMode.HALF_UP);
			    	         numberOfDaysPrice = pricePerDay.multiply(new BigDecimal(numberOfDays));
				       }
					}  
					taxAmount = numberOfDaysPrice.add(numberOfMonthsPrice);
					
					// taxAmount = taxFlat;
				}
				}
			
				invoiceTaxCommand = new InvoiceTaxCommand(billingOrderData.getClientId(), null, null,
						taxCode, null, taxPercentage, taxAmount);
				invoiceTaxCommands.add(invoiceTaxCommand);
			}

	    }
		return invoiceTaxCommands;

	}

	// create billing order command
	public BillingOrderCommand createBillingOrderCommand(
			BillingOrderData billingOrderData, LocalDate chargeStartDate,
			LocalDate chargeEndDate, LocalDate invoiceTillDate,
			LocalDate nextBillableDate, BigDecimal price,
			List<InvoiceTaxCommand> listOfTaxes,
			DiscountMasterData discountMasterData) {

		return new BillingOrderCommand(billingOrderData.getClientOrderId(),
				billingOrderData.getOderPriceId(),
				billingOrderData.getClientId(), chargeStartDate.toDate(),
				nextBillableDate.toDate(), chargeEndDate.toDate(),
				billingOrderData.getBillingFrequency(),
				billingOrderData.getChargeCode(),
				billingOrderData.getChargeType(),
				billingOrderData.getChargeDuration(),
				billingOrderData.getDurationType(), invoiceTillDate.toDate(),
				price, billingOrderData.getBillingAlign(), listOfTaxes,
				billingOrderData.getStartDate(), billingOrderData.getEndDate(),
				discountMasterData, billingOrderData.getTaxInclusive());
	}
	
	public BillingOrderCommand getDisconnectionCreditMonthyBill(
			BillingOrderData billingOrderData,
			DiscountMasterData discountMasterData, LocalDate disconnectionDate) {

		return null;
	}

}