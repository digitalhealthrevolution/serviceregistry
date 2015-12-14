package fi.vtt.dsp.service.serviceregistry.impl.dao;

public class DAOGeneralSystemFault extends Exception {

	private static final long serialVersionUID = 1L;
	private String reason;
	private Exception rootCause;

	public DAOGeneralSystemFault(String reason, Exception rootCause) {
		super();
		this.reason = reason;
		this.rootCause = rootCause;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Exception getRootCause() {
		return rootCause;
	}

	public void setRootCause(Exception rootCause) {
		this.rootCause = rootCause;
	}

}
