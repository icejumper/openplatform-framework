package com.hybris.openplatform.bootstrap.constants;

public class Constants
{
	private Constants() {}

	public static final String _TENANT_ = "tenant";
	public static final String REGISTRATIONS_STORAGE_NAME_PROP = "storage.registrations.name";

	public static final String DEFAULT_ENCODING = "utf-8";

	public static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
	public static final String HTTP_CONTENT_TYPE_APPLICATION_JSON = "application/json";
	public static final String HTTP_CONTENT_TYPE_APPLICATION_HYBRIS_VENDOR_JSON = "application/vnd+specific+json";
	public static final String HTTP_CONTENT_TYPE_TEXT_HTML = "text/html";
	public static final String HTTP_RESPONSE_CODE_HEADER = "Http-Response-Code";
	public static final String MESSAGE_PATH_PARAMS_HEADER = "pathParams";

	public static final int HTTP_STATUS_CODE_SUCCESS = 200;
	public static final int HTTP_STATUS_CODE_CREATED = 201;
	public static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
	public static final int HTTP_STATUS_CODE_FORBIDDEN = 403;
	public static final int HTTP_STATUS_CODE_BAD_REQUEST = 400;

	public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

}
