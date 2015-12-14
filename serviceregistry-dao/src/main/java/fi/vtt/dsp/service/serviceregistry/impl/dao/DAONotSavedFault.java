package fi.vtt.dsp.service.serviceregistry.impl.dao;

public class DAONotSavedFault extends Exception {

	private String reason;

	public DAONotSavedFault(String reason) {
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
