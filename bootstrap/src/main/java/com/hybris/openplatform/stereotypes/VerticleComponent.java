package com.hybris.openplatform.stereotypes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;


@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface VerticleComponent
{
	enum VerticleType {
		WORKER,
		STANDARD,
		DEFAULT
	}

	String value() default "";
	VerticleType worker() default VerticleType.DEFAULT;
	int instances() default 0;
}
