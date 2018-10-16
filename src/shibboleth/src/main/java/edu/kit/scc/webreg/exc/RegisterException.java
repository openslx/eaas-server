package edu.kit.scc.webreg.exc;

public class RegisterException extends Exception {

	private static final long serialVersionUID = 1L;

	public RegisterException() {
		super();
	}

	public RegisterException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RegisterException(String arg0) {
		super(arg0);
	}

	public RegisterException(Throwable arg0) {
		super(arg0);
	}

}
