/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnumDomainServiceRepository extends JpaRepository<EnumDomainService, Long>,
        JpaSpecificationExecutor<EnumDomainService> {

	@Query("from EnumDomainService enumDomainService where enumDomainService.enumMessageProperty =:enumMessageProperty and enumDomainService.enumName='order_status'")
	EnumDomainService findOneByEnumMessageProperty(@Param("enumMessageProperty")String enumMessageProperty);
}