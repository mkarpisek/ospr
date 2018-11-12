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

public abstract class SpObject {

	protected final String name;
	protected final String serverRelativeUrl;
	protected final String timeLastModified;
	protected final String timeCreated;

	public SpObject(String name, String serverRelativeUrl, String timeLastModified, String timeCreated) {
		this.name=name;
		this.serverRelativeUrl=serverRelativeUrl;
		this.timeLastModified=timeLastModified;
		this.timeCreated=timeCreated;
	}

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
	
	public boolean isFile() {
		return false;
	}
	
	public boolean isFolder() {
		return false;
	}

}
