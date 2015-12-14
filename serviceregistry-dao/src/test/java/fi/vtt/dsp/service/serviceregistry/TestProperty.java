/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vtt.dsp.service.serviceregistry;

/**
 *
 * @author JLJUHANI
 */
public enum TestProperty {

	SERVICE_URL("service.url"),
	AUTH_SECRET("auth.secret"),
	DB_ADDRESS("db.ip"),
	DB_PORT("db.port"),
	DB_NAME("db.name"),
	DB_USER_NAME("db.user"),
	DB_USER_PWD("db.pwd"),
	AS_ADDRESS("as.ip"),
	AS_PORT("as.port");

	private final String property;

	private TestProperty(String property) {
		this.property = property;
	}

	public String getProperty() {
		return property;
	}
}
