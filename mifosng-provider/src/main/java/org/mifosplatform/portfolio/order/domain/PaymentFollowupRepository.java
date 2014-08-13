package org.mifosplatform.portfolio.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentFollowupRepository  extends JpaRepository<PaymentFollowup, Long>,
JpaSpecificationExecutor<PaymentFollowup>{
	
	@Query("from PaymentFollowup paymentFollowup where paymentFollowup.id =(select max(followup.id) from PaymentFollowup followup where followup.orderId =:orderId" +
			" and followup.requestedStatus ='SUSPENDED')")
    PaymentFollowup findOneByorderId(@Param("orderId") Long orderId);

}
