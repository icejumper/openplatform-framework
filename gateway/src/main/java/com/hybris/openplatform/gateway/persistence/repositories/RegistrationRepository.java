/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.openplatform.gateway.persistence.repositories;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
@Profile({"persist"})
public interface RegistrationRepository extends CrudRepository<RestEndpointRegistration, String>
{
	
	RestEndpointRegistration findByPattern(String pattern);

	@Query("SELECT r FROM RestEndpointRegistration r WHERE r.pattern IN :patterns")
	Iterable<RestEndpointRegistration> findAllByPatterns(@Param("patterns") Iterable<String> patterns);

}
