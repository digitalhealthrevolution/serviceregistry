/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vtt.dsp.service.serviceregistry.auth.token;

/**
 *
 * @author JLJUHANI
 */
public interface AccessToken {

	Agent getAgent();

	String getBasicAuthPassword();

	String getBasicAuthUsername();

	boolean isAuthentic();

	boolean isValid();
	
}
