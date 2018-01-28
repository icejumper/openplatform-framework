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
package com.hybris.openplatform.bootstrap.messages;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "clc_registration")
@Data
@EqualsAndHashCode(of = "pattern")
@AllArgsConstructor
@NoArgsConstructor
public class RestEndpointRegistration
{
	@Id
	private String pattern;
	private String method;
	private String serviceName;
	private String serviceAddress;
	private String originatorAddress;
	private String description;
	private String status;

	private Set<String> requiredHeaders;
	private String accessControlAllowOrigin;
	private String acceptedContentType;
}
