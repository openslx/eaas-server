package edu.kit.scc.webreg.exc;

import java.io.Serializable;

public class SamlAuthenticationException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = 1L;

	public SamlAuthenticationException(String msg) {
		super(msg);
	}

	public SamlAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

}
