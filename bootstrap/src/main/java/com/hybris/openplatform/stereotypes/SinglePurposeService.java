package com.hybris.openplatform.stereotypes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertx.core.http.HttpMethod;


@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SinglePurposeService
{
	String serviceId() default "";
	String serviceDescription() default "Single purpose service";
	String endPointPattern();
	HttpMethod method();
}
