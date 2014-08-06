/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "r_enum_value")
public class EnumDomainService {

    @SuppressWarnings("unused")
    @Column(name = "enum_name", nullable = false)
    private  String enumName;

   @Id
   @Column(name = "enum_id", nullable = false)
   private Long enumId;

    @Column(name = "enum_message_property", nullable = false)
    private String enumMessageProperty;
    
    @Column(name = "enum_value", nullable = false)
    private String enumValue;
    
  	public  EnumDomainService() {
		
	}

	public String getEnumName() {
		return enumName;
	}

	public Long getEnumId() {
		return enumId;
	}

	public String getEnumMessageProperty() {
		return enumMessageProperty;
	}

	public String getEnumValue() {
		return enumValue;
	}
  	
}