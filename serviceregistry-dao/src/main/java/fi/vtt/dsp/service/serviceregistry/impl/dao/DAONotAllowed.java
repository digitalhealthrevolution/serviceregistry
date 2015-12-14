package fi.vtt.dsp.service.serviceregistry.impl.dao;

public class DAONotAllowed extends Exception {

	private String reason;

	public DAONotAllowed(String reason) {
		super();
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
