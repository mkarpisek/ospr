/*******************************************************************************
 * Copyright (c) 2018 Martin Karpisek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Martin Karpisek <martin.karpisek@gmail.com> - initial API and implementation 
 *******************************************************************************/
package net.karpisek.ospr.net;

import com.google.common.base.MoreObjects;

public class Folder{
	public Folder(String name, String serverRelativeUrl, String timeLastModified, String timeCreated, String itemCount) {
		this.name = name;
		this.serverRelativeUrl = serverRelativeUrl;
		this.timeLastModified = timeLastModified;
		this.timeCreated = timeCreated;
		this.itemCount = itemCount;
	}
	final String name;
	final String serverRelativeUrl;
	final String timeLastModified;
	final String timeCreated;
	final String itemCount;
	
	public String getName() {
		return name;
	}

	public String getServerRelativeUrl() {
		return serverRelativeUrl;
	}

	public String getTimeLastModified() {
		return timeLastModified;
	}

	public String getTimeCreated() {
		return timeCreated;
	}

	public String getItemCount() {
		return itemCount;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("serverRelativeUrl", serverRelativeUrl)
				.add("timeLastModified", timeLastModified)
				.add("timeCreated", timeCreated)
				.add("itemCount", itemCount)
				.toString();
	}
}