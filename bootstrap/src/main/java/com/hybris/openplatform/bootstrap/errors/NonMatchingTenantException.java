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
package com.hybris.openplatform.bootstrap.errors;

/**
 * Exception to be thrown in the case of non-matching tenant in the workflow
 */
public class NonMatchingTenantException extends RuntimeException
{
	public NonMatchingTenantException(final String message)
	{
		super(message);
	}
}
