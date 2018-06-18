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
package com.hybris.openplatform.gateway.admin.model;

import java.util.Set;

import lombok.Data;


@Data
public class RestEndpointRegistrationRef
{
	private String pattern;
	private String method;
	private String serviceName;
	private String description;
	private String status;

	private Set<String> requiredHeaders;
	private String accessControlAllowOrigin;
}
