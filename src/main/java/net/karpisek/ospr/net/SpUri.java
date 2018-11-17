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

import java.net.URI;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.net.UrlEscapers;

/**
 * Complete valid string describing sharepoint url for reporting tool is of format:
 * <pre>
 * https://yourdomain.sharepoint.com/sites/siteName
 * https://yourdomain.sharepoint.com/sites/siteName/libraryName
 * https://yourdomain.sharepoint.com/sites/siteName/libraryName/folderName1
 * </pre>
 * 
 * In case libraryName is not filled, default "Shared Documents" will be used.
 * 
 */
public class SpUri {
	/**
	 * Create sharepoint specific intance of URL.
	 * @param string to be parsed as sharepoint specific url.
	 * @return valid usable url
	 * @throws IllegalArgumentException in case something goes wrong or string does not cover needs of format for sharepoint url
	 */
	public static SpUri fromString(String string) {
		String escaped = UrlEscapers.urlFragmentEscaper().escape(string);
		return fromUri(URI.create(escaped));
	}
	
	public static SpUri fromUri(URI uri) {
		//extract and verify domain from host syntax
		String host = uri.getHost();
		
		Preconditions.checkArgument(host.endsWith("sharepoint.com"), "Unexpected server, it is '%s' should be 'sharepoint.com'", host);
		
		List<String> hostSegments = Splitter.on(".").omitEmptyStrings().splitToList(host);
		int hostSegmentsSizeExpected = 3;
		Preconditions.checkArgument(hostSegments.size()>=hostSegmentsSizeExpected, "Unexpected number of segments in host(%s). It is '%s', expected >= %s.", hostSegments, hostSegments.size(), hostSegmentsSizeExpected);
		
		String domain = hostSegments.get(hostSegments.size() - 3);
		
		//verify sites syntax
		String path = uri.getPath();
		List<String> pathSegments = Splitter.on("/").omitEmptyStrings().splitToList(path);
		int pathSegmentsSizeExpected = 2;
		Preconditions.checkArgument(pathSegments.size()>=pathSegmentsSizeExpected, "Unexpected number of segments in path(%s). It is '%s', expected >= '%s'.", pathSegments, pathSegments.size(), pathSegmentsSizeExpected);
		
		String path0 = pathSegments.get(0);
		String path0expected = "sites";
		Preconditions.checkArgument(path0expected.equals(path0), "Unexpected first segment in path(%s). It is '%s', expected '%s'.", pathSegments, path0, path0expected);	
		
		String siteName = pathSegments.get(1);
		String documentLibraryName = pathSegments.size() <= 2 ? null : pathSegments.get(2);
		
		URI complete = uri;
		if(documentLibraryName == null) {
			//append default "Shared Library" if not entered
			complete = URI.create(uri.toString() + UrlEscapers.urlPathSegmentEscaper().escape("/Shared Documents"));
		}
		
		return new SpUri(complete, domain, siteName);
	}

	private URI uri;
	private String domain;
	private String site;
	
	private SpUri(URI uri, String domain, String site) {
		this.uri = uri;
		this.domain = domain;
		this.site = site;
	}
	
	/**
	 * Answers 'domain' part of url in format used by sharepoint authentication https://domain.sharepoint.com.
	 */
	public String getDomain() {
		return domain;
	}
	
	public String getHost() {
		return uri.getHost();
	}
	
	public String getPath() {
		return uri.getPath();
	}

	public int getPort() {
		return uri.getPort();
	}

	public String getSite() {
		return site;
	}
	
	/**
	 * Answers sharepoint url without path. 
	 * For example:
	 * <pre>
	 * https://yourdomain.sharepoint.com
	 * <pre>
	 */
	public URI getServerUri() {
		return URI.create(String.format("%s://%s%s", uri.getScheme(), uri.getHost(), uri.getPort() == -1 ? "" : ":" + uri.getPort()));
	}
	
	/**
	 * Answers sharepoint url including site name. 
	 * For example:
	 * <pre>
	 * https://yourdomain.sharepoint.com/sites/siteName
	 * <pre>
	 */
	public URI getSiteUri() {
		return URI.create(String.format("%s://%s%s/sites/%s", uri.getScheme(), uri.getHost(), uri.getPort() == -1 ? "" : ":" + uri.getPort(), getSite()));
	}
}
