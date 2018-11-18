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

public class SpVersion {
	public static SpVersion fromString(String majorVersion, String minorVersion) {
		return new SpVersion(Integer.parseInt(majorVersion), Integer.parseInt(minorVersion));
	}

	private int major;
	private int minor;
	
	private SpVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	@Override
	public String toString() {
		return String.format("%d.%d", major, minor);
	}
}
