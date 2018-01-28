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
package com.hybris.openplatform.common.rxjava.observers;

import java.util.function.Consumer;

import rx.Observer;


public class OnErrorObserver<T> implements Observer<T>
{
	private Consumer<Throwable> errorConsumer;

	public OnErrorObserver(Consumer<Throwable> errorConsumer)
	{
		this.errorConsumer = errorConsumer;
	}

	@Override
	public void onCompleted()
	{
		// empty
	}

	@Override
	public void onError(final Throwable error)
	{
		errorConsumer.accept(error);
	}

	@Override
	public void onNext(final T t)
	{
		// empty
	}
}
