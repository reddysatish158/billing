package org.mifosplatform.billing.selfcare.service;

import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SelfCareRepository extends JpaRepository<SelfCare, Long>, JpaSpecificationExecutor<SelfCare>{

	@Query("from SelfCare selfCare where selfCare.clientId =:clientId")
	SelfCare findOneByClientId(@Param("clientId")Long clientId);
	
	@Query("from SelfCare selfCare where selfCare.userName =:userName")
	SelfCare findOneByEmailId(@Param("userName")String userName);

}
