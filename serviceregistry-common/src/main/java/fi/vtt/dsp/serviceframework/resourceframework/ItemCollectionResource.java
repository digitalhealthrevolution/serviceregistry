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
 * Resource for collection of items with URI and provided methods for content
 * creation and reading.
 * 
 * @author ELETAI
 * @version 1.0
 * @created 11-syys-2014 13:31:15
 */
public abstract class ItemCollectionResource {

	private java.util.List<Link> m_Link;
	private ResourceRepresentation m_ResourceRepresentation;

	public ItemCollectionResource() {

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
	 * Creates and adds a new item into the collection. Returns URI to the
	 * created item resources.
	 * 
	 * @param itemRepresentation
	 *            Representation of the resource to be added into the collection
	 */
	public java.net.URI createItem(ResourceRepresentation itemRepresentation) {
		return null;
	}

	/**
	 * Reads and returns a list of the contained items
	 */
	public ResourceRepresentation[] readListOfItems() {
		return null;
	}

}