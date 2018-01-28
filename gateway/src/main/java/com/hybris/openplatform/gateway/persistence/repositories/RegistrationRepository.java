package com.hybris.openplatform.gateway.persistence.repositories;

import com.hybris.openplatform.bootstrap.messages.RestEndpointRegistration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
@Profile({"persist"})
public interface RegistrationRepository extends CrudRepository<RestEndpointRegistration, String>
{
	
	RestEndpointRegistration findByPattern(String pattern);

	@Query("SELECT r FROM RestEndpointRegistration r WHERE r.pattern IN :patterns")
	Iterable<RestEndpointRegistration> findAllByPatterns(@Param("patterns") Iterable<String> patterns);

}
