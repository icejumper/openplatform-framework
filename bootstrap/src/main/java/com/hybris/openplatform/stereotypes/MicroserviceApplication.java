package com.hybris.openplatform.stereotypes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan("com.hybris.openplatform.common")
@SpringBootApplication
public @interface MicroserviceApplication
{
	String value() default "";
	String profile() default "";
}
