/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vtt.dsp.service.serviceregistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author JLJUHANI
 */
public class TestProperties {
	
	private static final Map<TestProperty, String> PROPERTIES = new HashMap<>();
	
	static {		
		// read configuration properties
		Properties cp = new Properties();
		InputStream in = TestProperties.class.getResourceAsStream("/test.properties");
		try {
			cp.load(in);			
			for( TestProperty prop : TestProperty.values() ) {
				String key = prop.getProperty();
				PROPERTIES.put(prop, cp.getProperty(key));
				
				// if defined as system property overwrite value
				String systemPropValue = System.getProperty(key);
				if( StringUtils.isNotBlank(systemPropValue) ) {
					PROPERTIES.put(prop, systemPropValue);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(TestProperties.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	public static String get(TestProperty prop) {
		return PROPERTIES.get(prop);
	}
}
