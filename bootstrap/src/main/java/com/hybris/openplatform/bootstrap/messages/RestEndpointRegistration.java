package com.hybris.openplatform.bootstrap.messages;

import java.io.Serializable;
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
public class RestEndpointRegistration implements Serializable
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
