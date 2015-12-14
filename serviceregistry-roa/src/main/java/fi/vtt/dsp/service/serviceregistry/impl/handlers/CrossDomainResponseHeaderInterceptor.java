package fi.vtt.dsp.service.serviceregistry.impl.handlers;

import java.util.Arrays;

import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class CrossDomainResponseHeaderInterceptor
		extends
			AbstractOutDatabindingInterceptor {

	public CrossDomainResponseHeaderInterceptor() {
		super(Phase.MARSHAL);
	}

	@Override
	public void handleMessage(Message outMessage) throws Fault {
		// Constructor is MetadataMap<K, List<V>>
		@SuppressWarnings("unchecked")
		MetadataMap<String, String> headers = (MetadataMap<String, String>) outMessage
				.get(Message.PROTOCOL_HEADERS);

		if (headers == null) {
			headers = new MetadataMap<String, String>();
			outMessage.put(Message.PROTOCOL_HEADERS, headers);
		}
		try {
			headers.put("Access-Control-Allow-Origin", Arrays.asList("*"));
			headers.put("Access-Control-Allow-Methods",
					Arrays.asList("GET", "POST", "OPTIONS", "PUT", "DELETE"));
			headers.put("Access-Control-Allow-Headers", Arrays.asList(
					"X-PROTOTYPE-VERSION",
					"X-REQUESTED-WITH, Content-Type, Authorization"));
			headers.put("Access-Control-Expose-Headers",
					Arrays.asList("Location"));
		} catch (Exception ce) {
			throw new Fault(ce);
		}
	}

}
