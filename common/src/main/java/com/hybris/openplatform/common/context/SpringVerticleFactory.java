package com.hybris.openplatform.common.context;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;


@Component
public class SpringVerticleFactory implements VerticleFactory
{

  private ApplicationContext applicationContext;

  @Override
  public boolean blockingCreate() {
    // Usually verticle instantiation is fast but since our verticles are Spring Beans,
    // they might depend on other beans/resources which are slow to build/lookup.
    return true;
  }

  @Override
  public String prefix() {
    // Just an arbitrary string which must uniquely identify the verticle factory
    return "commonSpringVerticleFactory";
  }

  @Override
  public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
    // Our convention in this example is to give the class name as verticle name
    String clazz = VerticleFactory.removePrefix(verticleName);
    return (Verticle) applicationContext.getBean(Class.forName(clazz));
  }

  @Resource
  public void setApplicationContext(final ApplicationContext applicationContext)
  {
    this.applicationContext = applicationContext;
  }
}
