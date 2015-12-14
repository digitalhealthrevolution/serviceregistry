package fi.vtt.dsp.serviceframework.platform.roa;

/*
 * #%L
 * Digital Service Framework Common
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 VTT Technical Research Centre of Finland Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.resourceframework.ItemResource;

/**
 * Resource for accessing each individual service binding
 * 
 * @author ELETAI
 * @version 1.0
 * @created 11-syys-2014 13:31:15
 */
public abstract class BindingResource extends ItemResource {

	public BindingResource() {

	}

	public void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * Method returns a representation of the individual binding
	 */
	public Binding[] readBinding() {
		return null;
	}

	/**
	 * Method requests to update existing binding information. Request can be
	 * denied and associated exception thrown by the service if applicable
	 * 
	 * @param updatedBinding
	 */
	public void requestUpdatedBinding(Binding updatedBinding) {

	}

	/**
	 * Method requests to removal of the existing binding information. Request
	 * can be denied and associated exception thrown by the service if
	 * applicable
	 */
	public void requestUnbinding() {

	}

}