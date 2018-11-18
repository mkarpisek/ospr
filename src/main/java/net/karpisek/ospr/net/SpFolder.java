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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class SpFolder extends SpObject{
	private List<? extends SpObject> children;

	public SpFolder(String name, String serverRelativeUrl, String timeLastModified, String timeCreated, String itemCount, List<? extends SpObject> children) {
		super(name, serverRelativeUrl, timeLastModified, timeCreated);
		this.itemCount = itemCount;
		this.children = Lists.newArrayList(children);
		Collections.sort(this.children, new Comparator<SpObject>() {
			@Override
			public int compare(SpObject o1, SpObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	final String itemCount;
	
	public String getItemCount() {
		return itemCount;
	}
	
	public List<? extends SpObject> getChildren() {
		return children;
	}
	
	public Stream<? extends SpObject> getChildren(Predicate<SpObject> predicate) {
		return children.stream().filter(predicate);
	}
	
	
	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("serverRelativeUrl", serverRelativeUrl)
				.add("timeLastModified", timeLastModified)
				.add("timeCreated", timeCreated)
				.add("itemCount", itemCount)
				.add("children", Joiner.on("|").join(getChildren().stream().map(each -> each.getName()).collect(Collectors.toList())))
				.toString();
	}
}