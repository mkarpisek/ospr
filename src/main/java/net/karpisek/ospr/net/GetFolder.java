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
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
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
 * Answers information about request folder.
 */
public class GetFolder {
	private static final Logger LOG = LoggerFactory.getLogger(SharepointOnlineAuthentication.class);
	
	private Result authResult;
	private URI siteUri;
	private String folderServerRelativeUrl;

	public GetFolder(SharepointOnlineAuthentication.Result authResult, URI siteUri, String folderServerRelativeUrl) {
		this.authResult = authResult;
		this.siteUri = siteUri;
		this.folderServerRelativeUrl = folderServerRelativeUrl;
	}
	
	//TODO: would want to have better reaction for invalid (not found) folders 
	public SpFolder execute(HttpClient httpClient) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		String url = siteUri + "/_api/Web/GetFolderByServerRelativeUrl(%27" + UrlEscapers.urlFragmentEscaper().escape(folderServerRelativeUrl) + "%27)?$expand=Folders,Files";
		
		ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("X-RequestDigest", authResult.getFormDigest()).send();
		LOG.debug("statusCode={}", response.getStatus());
		Verify.verify(response.getStatus() == HttpStatus.OK_200, "response status not ok, statusCode={}", response.getStatus());
		
		String content = response.getContentAsString();
		LOG.debug("content={}", response.getStatus());
		Verify.verify(Strings.emptyToNull(content) != null, "response content does not contain data");
		
		return parse(content);
	}
	
	public SpFolder parse(String xml) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(xml));

		Namespace a = Namespace.getNamespace("a", "http://www.w3.org/2005/Atom");
		Namespace d = Namespace.getNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
		Namespace m = Namespace.getNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
		
		List<SpObject> children = Lists.newArrayList();
		XPathExpression<Element> filesExpr = XPathFactory.instance().compile("//a:feed/a:entry[a:category[@term='SP.File']]/a:content/m:properties", Filters.element(), null, a,d,m);
		List<Element> filesElements = filesExpr.evaluate(doc);
		for (Element properties : filesElements) {
			children.add(
				new SpFile(
					properties.getChildText("Name", d), 
					properties.getChildText("ServerRelativeUrl", d), 
					properties.getChildText("TimeLastModified", d), 
					properties.getChildText("TimeCreated", d), 
					properties.getChildText("Length", d)
				)
			);
		}		
		
		XPathExpression<Element> foldersExpr = XPathFactory.instance().compile("//a:feed/a:entry[a:category[@term='SP.Folder']]/a:content/m:properties", Filters.element(), null, a,d,m);
		List<Element> folderElements = foldersExpr.evaluate(doc);
		for (Element properties : folderElements) {
			children.add(
				new SpFolder(
					properties.getChildText("Name", d), 
					properties.getChildText("ServerRelativeUrl", d), 
					properties.getChildText("TimeLastModified", d), 
					properties.getChildText("TimeCreated", d), 
					properties.getChildText("ItemCount", d),
					Lists.newArrayList()
				)
			);
		}	
		
		XPathExpression<Element> expr5 = XPathFactory.instance().compile("/a:entry/a:content/m:properties", Filters.element(), null, a,d,m);
		List<Element> links5 = expr5.evaluate(doc);
		Verify.verify(links5.size() == 1, "This is strange, should be exactly one properties element about");
		return new SpFolder(
			links5.get(0).getChildText("Name", d), 
			links5.get(0).getChildText("ServerRelativeUrl", d), 
			links5.get(0).getChildText("TimeLastModified", d), 
			links5.get(0).getChildText("TimeCreated", d), 
			links5.get(0).getChildText("ItemCount", d),
			children
		);
	}
}
