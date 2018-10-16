package edu.kit.scc.webreg.exc;

public class MisconfiguredServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MisconfiguredServiceException() {
	}

	public MisconfiguredServiceException(String arg0) {
		super(arg0);
	}

	public MisconfiguredServiceException(Throwable arg0) {
		super(arg0);
	}

	public MisconfiguredServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
