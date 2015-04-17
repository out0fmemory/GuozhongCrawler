/**
 * 
 */
package com.guozhong;

/**
 * @author jonasabreu
 * 
 */
public enum Status {

	CREATED_201(201, 201),
	ACCEPTED_202(202, 202),
	NAI_203(203, 203),
	NO_CONTENT_204(204, 204),
	RESET_CONTENT_205(205, 205),
	PARTIAL_CONTENT(206, 206),
	OK(200, 299),

	MOVED_PERMANENTLY_301(301, 301),
	FOUND_302(302, 302),
	SEE_OTHER_303(303, 303),
	NOT_MODIFIED_304(304, 304),
	USE_PROXY_305(305, 305),
	TEMPORARY_REDIRECTION(307, 307),
	REDIRECTION(300, 399),

	BAD_REQUEST(400, 400),
	UNAUTHORIZED(401, 401),
	FORBIDDEN(403, 403),
	NOT_FOUND(404, 404),
	METHOD_NOT_ALLOWED(405, 405),
	NOT_ACCEPTABLE(406, 406),
	PROXY_AUTHENTICATION_REQUIRED(407, 407),
	REQUEST_TIMEOUT(408, 408),
	CONFLICT(409, 409),
	GONE(410, 410),
	LENGTH_REQUIRED(411, 411),
	PRECONDITION_FAILED(412, 412),
	REQUEST_ENTITY_TOO_LARGE(413, 413),
	REQUEST_URI_TOO_LONG(414, 414),
	UNSUPPORTED_MEDIA_TYPE(415, 415),
	REQUEST_RANGE_NOT_SATISFIABLE(416, 416),
	EXPECTATION_FAILED(417, 417),
	CLIENT_ERROR(400, 499),

	INTERNAL_SERVER_ERROR(500, 500),
	NOT_IMPLEMENTED(501, 501),
	BAD_GATEWAY(502, 502),
	SERVICE_UNAVAILABLE(503, 503),
	GATEWAY_TIMEOUT(504, 504),
	HTTP_VERSION_NOT_SUPPORTED(505, 505),
	SERVER_ERROR(500, 599),

	UNSPECIFIED_ERROR(1, 999);

	private final int begin;
	private final int end;

	private Status(final int begin, final int end) {
		this.begin = begin;
		this.end = end;
	}

	public static Status fromHttpCode(final int code) {
		for (Status status : values()) {
			if ((status.begin <= code) && (status.end >= code)) {
				return status;
			}
		}
		return UNSPECIFIED_ERROR;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}


}
