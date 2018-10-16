package edu.kit.scc.webreg.exc;

public class NotAuthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotAuthorizedException() {
	}

	public NotAuthorizedException(String arg0) {
		super(arg0);
	}

	public NotAuthorizedException(Throwable arg0) {
		super(arg0);
	}

	public NotAuthorizedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
