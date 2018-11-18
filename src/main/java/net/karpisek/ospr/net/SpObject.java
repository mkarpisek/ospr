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

import java.time.Instant;

public abstract class SpObject {

	protected final String name;
	protected final String serverRelativeUrl;
	protected final Instant timeLastModified;
	protected final Instant timeCreated;

	public SpObject(String name, String serverRelativeUrl, Instant timeLastModified, Instant timeCreated) {
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

	public Instant getTimeLastModified() {
		return timeLastModified;
	}

	public Instant getTimeCreated() {
		return timeCreated;
	}
	
	public boolean isFile() {
		return false;
	}
	
	public boolean isFolder() {
		return false;
	}

}
