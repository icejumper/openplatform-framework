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
