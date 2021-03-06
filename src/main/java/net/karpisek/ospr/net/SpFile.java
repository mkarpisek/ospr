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

import com.google.common.base.MoreObjects;

public class SpFile extends SpObject{
	private final int length;
	private SpVersion version;

	public SpFile(String name, String serverRelativeUrl, Instant timeLastModified, Instant timeCreated, int length, SpVersion version) {
		super(name, serverRelativeUrl, timeLastModified, timeCreated);
		this.length = length;
		this.version = version;
	}
	
	public int getLength() {
		return length;
	}
	
	public SpVersion getVersion() {
		return version;
	}
	
	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("serverRelativeUrl", serverRelativeUrl)
				.add("timeLastModified", timeLastModified)
				.add("timeCreated", timeCreated)
				.add("length", length)
				.add("version", version)
				.toString();
	}
}