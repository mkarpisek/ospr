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

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

//TODO: support not having hardcoded "Shared folders"
public class SpUriTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void fromString_site() throws Exception {
		SpUri uri = SpUri.fromString("https://yourdomain.sharepoint.com:80/sites/test1site");
		assertNotNull(uri);
		assertEquals("yourdomain.sharepoint.com", uri.getHost());
		assertEquals(80, uri.getPort());
		assertEquals("yourdomain", uri.getDomain());

		assertEquals("/sites/test1site/Shared Documents", uri.getPath());
		assertEquals("test1site", uri.getSite());
	}
	
	@Test
	public void fromString_library() throws Exception {
		SpUri uri = SpUri.fromString("https://yourdomain.sharepoint.com:80/sites/test1site/Shared Documents/folder1");
		assertNotNull(uri);
		assertEquals("yourdomain.sharepoint.com", uri.getHost());
		assertEquals(80, uri.getPort());
		assertEquals("yourdomain", uri.getDomain());
		assertEquals(URI.create("https://yourdomain.sharepoint.com:80"), uri.getServerUri());

		assertEquals("/sites/test1site/Shared Documents/folder1", uri.getPath());
		assertEquals("test1site", uri.getSite());
	}
	
	@Test
	public void fromString_subfolder() throws Exception {
		SpUri uri = SpUri.fromString("https://yourdomain.sharepoint.com:80/sites/test1site/Shared Documents/folder1/folder11");
		assertNotNull(uri);
		assertEquals("yourdomain.sharepoint.com", uri.getHost());
		assertEquals(80, uri.getPort());
		assertEquals("yourdomain", uri.getDomain());
		assertEquals(URI.create("https://yourdomain.sharepoint.com:80"), uri.getServerUri());

		assertEquals("/sites/test1site/Shared Documents/folder1/folder11", uri.getPath());
		assertEquals("test1site", uri.getSite());
	}
	@Test
	public void fromString_subfolder2() throws Exception {
		SpUri uri = SpUri.fromString("https://yourdomain.sharepoint.com:80/sites/test1site/Shared Documents/folder1/folder11");
		assertNotNull(uri);
		assertEquals("yourdomain.sharepoint.com", uri.getHost());
		assertEquals(80, uri.getPort());
		assertEquals("yourdomain", uri.getDomain());
		assertEquals(URI.create("https://yourdomain.sharepoint.com:80"), uri.getServerUri());

		assertEquals("/sites/test1site/Shared Documents/folder1/folder11", uri.getPath());
		assertEquals("test1site", uri.getSite());
	}
	
	@Test
	public void fromString_missingSite() throws Exception {
	    thrown.expect(IllegalArgumentException.class);
	    thrown.expectMessage("Unexpected number of segments in path([]). It is '0', expected >= '2'");
		SpUri.fromString("https://yourdomain.sharepoint.com:80");
	}

	@Test
	public void fromString_missingSite2() throws Exception {
	    thrown.expect(IllegalArgumentException.class);
	    thrown.expectMessage("Unexpected number of segments in path([sites]). It is '1', expected >= '2'");
		SpUri.fromString("https://yourdomain.sharepoint.com:80/sites");
	}
	
	@Test
	public void getServerUri_noPort() throws Exception {
		assertEquals(URI.create("https://yourdomain.sharepoint.com"), SpUri.fromString("https://yourdomain.sharepoint.com/sites/test1site/Shared Documents/folder1").getServerUri());
	}
	@Test
	public void getServerUri_withPort() throws Exception {
		assertEquals(URI.create("https://yourdomain.sharepoint.com:8080"), SpUri.fromString("https://yourdomain.sharepoint.com:8080/sites/test1site/Shared Documents/folder1").getServerUri());
	}
	
	@Test
	public void getSiteUri_noPort() throws Exception {
		assertEquals(URI.create("https://yourdomain.sharepoint.com/sites/test1site"), SpUri.fromString("https://yourdomain.sharepoint.com/sites/test1site/Shared Documents/folder1").getSiteUri());
	}
	@Test
	public void getSiteUri_withPort() throws Exception {
		assertEquals(URI.create("https://yourdomain.sharepoint.com:8080/sites/test1site"), SpUri.fromString("https://yourdomain.sharepoint.com:8080/sites/test1site/Shared Documents/folder1").getSiteUri());
	}
	
}

