package fi.vtt.dsp.service.serviceregistry.impl.roa;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @param <T>
 */
public abstract class StaticResourceAuthIT<T> extends ResourceAuthIT<T> {
	
	@Test
	@Override
	@Ignore
	public void test_UpdateNotFound() {
		// ignore not found for static resource
	}	
	
	@Test
	@Override
	@Ignore	
	public void test_DeleteNotFound() {
		// ignore not found for static resource
	}	
}
