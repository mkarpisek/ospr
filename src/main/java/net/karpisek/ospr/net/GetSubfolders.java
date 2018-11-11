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
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;

import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;

/**
 * Answers information about all direct subfolders for the requested folder.
 */
public class GetSubfolders {
	private static final Logger LOG = LoggerFactory.getLogger(SharepointOnlineAuthentication.class);
	
	private Result authResult;
	private String sharepointSiteEndpoint;
	private String folder;

	public GetSubfolders(SharepointOnlineAuthentication.Result authResult, String sharepointSiteEndpoint, String folder) {
		this.authResult = authResult;
		this.sharepointSiteEndpoint = sharepointSiteEndpoint;
		this.folder = folder;
	}
	
	public List<Folder> execute(HttpClient httpClient) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		XPathFactory xFactory = XPathFactory.instance();
		SAXBuilder builder = new SAXBuilder();
		LOG.debug("## 5-listSubfolders");
		String url5 = sharepointSiteEndpoint + "/_api/Web/GetFolderByServerRelativeUrl(%27" + UrlEscapers.urlFragmentEscaper().escape(folder) + "%27)/Folders";
		ContentResponse response5 = httpClient.newRequest(url5).method(HttpMethod.GET).header("X-RequestDigest", authResult.getFormDigest()).send();
		LOG.debug("statusCode={}", response5.getStatus());
		String content5 = response5.getContentAsString();
		Verify.verify(Strings.emptyToNull(content5) != null, "response content does not contain data");
		Document doc5 = builder.build(new StringReader(content5));

		Namespace d = Namespace.getNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
		Namespace m = Namespace.getNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
		XPathExpression<Element> expr5 = xFactory.compile("//m:properties", Filters.element(), null, m);
		List<Element> links5 = expr5.evaluate(doc5);
		LOG.debug("//m:properties=" + links5.size());
		List<Folder> results = Lists.newArrayList();
		for (Element properties : links5) {
			results.add(
					new Folder(
							properties.getChildText("Name", d), 
							properties.getChildText("ServerRelativeUrl", d), 
							properties.getChildText("TimeLastModified", d), 
							properties.getChildText("TimeCreated", d), 
							properties.getChildText("ItemCount", d)
					)
			);
		}
		return results;
	}
}
