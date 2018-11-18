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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.JDOMException;
import org.junit.Test;

import com.google.common.collect.Lists;

import net.karpisek.ospr.Ospr;

public class GetFolderTest {
	@Test
	public void testParseFolderResponse() throws IOException, JDOMException {
		String xml = Ospr.readResource("getFolderResponse.xml");
		assertNotNull("Failed to load test resource", xml);
		
		SpFolder folder = new GetFolder(null, URI.create("https//localhost/sites/test1site"), "").parse(xml);
		
		assertEquals("Shared Documents", folder.getName());
		assertEquals("/sites/test1site/Shared Documents", folder.getServerRelativeUrl());
		assertEquals(Instant.parse("2018-11-10T16:25:33Z"), folder.getTimeLastModified());
		assertEquals(Instant.parse("2018-10-27T08:08:40Z"), folder.getTimeCreated());
		assertEquals("4", folder.getItemCount());
		
		ArrayList<String> expectedChildrenNames = Lists.newArrayList("Forms", "folder1" , "test1.txt", "test2.txt", "test3.txt");
		List<String> actualChildrenNames = folder.getChildren().stream().map(each -> each.getName()).collect(Collectors.toList());
		assertEquals(expectedChildrenNames, actualChildrenNames);
		
		assertEquals(Lists.newArrayList("test1.txt", "test2.txt", "test3.txt"), folder.getChildren().stream().filter(each -> each.isFile()).map(each -> each.getName()).collect(Collectors.toList()));
		assertEquals(Lists.newArrayList("Forms", "folder1"), folder.getChildren().stream().filter(each -> each.isFolder()).map(each -> each.getName()).collect(Collectors.toList()));
	}	
}
