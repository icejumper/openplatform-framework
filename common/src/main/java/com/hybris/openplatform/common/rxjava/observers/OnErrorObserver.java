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
