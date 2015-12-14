package fi.vtt.dsp.serviceframework.resourceframework;

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


/**
 * Individual item resource with URI and content manipulation methods
 * 
 * @author ELETAI
 * @version 1.0
 * @created 11-syys-2014 13:31:15
 */
public abstract class ItemResource {

	private ResourceRepresentation m_ResourceRepresentation;
	private java.util.List<Link> m_Link;

	public ItemResource() {

	}

	public void finalize() throws Throwable {

	}

	public ResourceRepresentation getResourceRepresentation() {
		return m_ResourceRepresentation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setResourceRepresentation(ResourceRepresentation newVal) {
		m_ResourceRepresentation = newVal;
	}

	public java.util.List<Link> getLink() {
		return m_Link;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLink(java.util.List<Link> newVal) {
		m_Link = newVal;
	}

	/**
	 * Returns individual item representation of this resource
	 */
	public ResourceRepresentation readItem() {
		return null;
	}

	/**
	 * Updates this item resource with the provided content
	 * 
	 * @param updatedItemRepresentation
	 *            Updated item presentation of the resource
	 */
	public java.net.URI updateItem(
			ResourceRepresentation updatedItemRepresentation) {
		return null;
	}

	/**
	 * Removes this item resource
	 */
	public void removeItem() {

	}

}