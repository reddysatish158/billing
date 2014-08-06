package org.mifosplatform.billing.selfcare.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SelfCareTemporaryRepository  extends JpaRepository<SelfCareTemporary, Long>, JpaSpecificationExecutor<SelfCareTemporary> {

	@Query("from SelfCareTemporary selfCareTemporary where selfCareTemporary.generatedKey =:generatedKey")
	SelfCareTemporary findOneByGeneratedKey(@Param("generatedKey")String generatedKey);

}
