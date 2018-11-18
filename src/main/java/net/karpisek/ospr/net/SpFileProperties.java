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

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * office documents properties available via rest
 */
public class SpFileProperties{
	public static enum CoreProperty{
		TITLE,
		SUBJECT,
		COMMENT,
		KEYWORDS,
		AUTHOR;
	}

	private EnumMap<CoreProperty, String> coreProperties;
	
	SpFileProperties(EnumMap<CoreProperty, String> coreProperties){
		this.coreProperties = coreProperties;
	}
	
	public String getValue(CoreProperty property, String defaultValue) {
		String string = coreProperties.get(property);
		if(string == null) {
			return defaultValue;
		}
		return string;
	}
	
	public void keysAndValuesDo(BiConsumer<CoreProperty, String> block) {
		for (Entry<CoreProperty, String> entry : coreProperties.entrySet()) {
			block.accept(entry.getKey(), entry.getValue());
		}
	}
	
}
