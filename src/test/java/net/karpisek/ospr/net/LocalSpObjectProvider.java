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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jdom2.JDOMException;

import com.google.common.collect.Lists;

/**
 * Provider of sharepoint files library object information using local file system.
 */
class LocalSpObjectProvider implements ISpObjectProvider{
	public static final Instant NOW = Instant.parse("2018-11-10T15:31:43Z");

	@Override
	public SpFolder getFolder(String folder) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		
		List<SpFolder> folders = Files
				.list(Paths.get(folder))
				.filter(each -> Files.isDirectory(each))
				.map(path -> new SpFolder(SpFilesTest.toUnixString(path.getFileName()), SpFilesTest.toUnixString(path), NOW, NOW, "", Lists.newArrayList()))
				.collect(Collectors.toList());
		List<SpFile> files = Files
				.list(Paths.get(folder))
				.filter(each -> !Files.isDirectory(each))
				.map(path -> new SpFile(SpFilesTest.toUnixString(path.getFileName()), SpFilesTest.toUnixString(path), NOW,NOW,"1234567"))
				.collect(Collectors.toList());
		
		List<SpObject> children = Lists.newArrayList();
		children.addAll(folders);
		children.addAll(files);
		return new SpFolder(
				SpFilesTest.toUnixString(Paths.get(folder).getFileName()),
				SpFilesTest.toUnixString(Paths.get(folder)), 
				NOW, 
				NOW, 
				"",
				children
				);
	}		
}