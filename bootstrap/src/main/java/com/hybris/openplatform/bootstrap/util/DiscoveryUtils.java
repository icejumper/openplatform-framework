package com.hybris.openplatform.bootstrap.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Network and naming discovery utils class
 */
@Component
public class DiscoveryUtils
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryUtils.class);

	public Optional<String> getHostName()
	{
		try
		{
			String myNodeName = System.getenv("MY_NODE_NAME");
			if(Objects.isNull(myNodeName))
			{
				myNodeName = InetAddress.getLocalHost().getHostName();
			}
			return Optional.of(myNodeName);
		}
		catch (UnknownHostException e)
		{
			LOGGER.error("UnknownHostException caught: {}", e);
		}
		return Optional.empty();
	}

	public String resolveName(final String template, final String hostNamePattern, final String... groupReplacements)
	{
		if (template.startsWith("${") && template.contains("}") && template.contains(":"))
		{
			final Optional<String> hostName = getHostName();
			if (hostName.isPresent())
			{
				final String resolvedHostName = hostName.get();
				LOGGER.info("My host name is [{}]", resolvedHostName);
				final String completeAddress = resolvedHostName + ":" + template.substring(template.indexOf(':') + 1);
				final Pattern k8sAgentPattern = Pattern.compile(hostNamePattern);
				final Matcher matcher = k8sAgentPattern.matcher(completeAddress);
				if (matcher.matches() && Objects.nonNull(groupReplacements) && groupReplacements.length == matcher.groupCount())
				{
					StringBuilder result = new StringBuilder(completeAddress);
					int correctionIdx = 0;
					String substitutionString;
					for (int i = 0; i < groupReplacements.length; i++)
					{
						if (groupReplacements[i].startsWith("$"))
						{
							final int groupNumber = Integer.valueOf(groupReplacements[i].substring(1));
							substitutionString = matcher.group(groupNumber);
						}
						else
						{
							substitutionString = groupReplacements[i];
						}
						final int start = matcher.start(i + 1) + correctionIdx;
						final int end = matcher.end(i + 1)  + correctionIdx;
						result = result.replace(start, end, substitutionString);
						correctionIdx += substitutionString.length() - (matcher.end(i + 1)  - matcher.start(i + 1));
					}
					return result.toString();
				}
			}
		}
		return stripBrackets(template);
	}

	private String stripBrackets(final String template)
	{
		StringBuilder result = new StringBuilder(template);
		if(template.startsWith("${"))
		{
			result = result.delete(0,2);
		}
		if(template.contains("}"))
		{
			result = result.deleteCharAt(result.indexOf("}"));
		}
		return result.toString();
	}

}
