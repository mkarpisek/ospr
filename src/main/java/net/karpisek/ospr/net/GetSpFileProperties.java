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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
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
import com.google.common.collect.Maps;
import com.google.common.net.UrlEscapers;

import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;
import net.karpisek.ospr.net.SpFileProperties.CoreProperty;

/**
 * Answers information about request folder.
 */
public class GetSpFileProperties {
	private static final Logger LOG = LoggerFactory.getLogger(SharepointOnlineAuthentication.class);
	
	public static Instant getChildInstant(Element element, String cname, Namespace ns) {
		String text = element.getChildText(cname, ns);
		if(text == null) {
			return null;
		}
		return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(text));
	}
	
	public static int getChildInt(Element element, String cname, Namespace ns) {
		String text = element.getChildText(cname, ns);
		if(text == null) {
			return 0;
		}
		return Integer.parseInt(text);
	}

	private Result authResult;
	private URI siteUri;
	private String fileServerRelativeUrl;

	public GetSpFileProperties(SharepointOnlineAuthentication.Result authResult, URI siteUri, String folderServerRelativeUrl) {
		this.authResult = authResult;
		this.siteUri = siteUri;
		this.fileServerRelativeUrl = folderServerRelativeUrl;
	}
	
	//TODO: would want to have better reaction for invalid (not found) folders 
	public SpFileProperties execute(HttpClient httpClient) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		String url = siteUri + "/_api/Web/GetFileByServerRelativePath(decodedurl=%27" + UrlEscapers.urlFragmentEscaper().escape(fileServerRelativeUrl) + "%27)/Properties";
		
		ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("X-RequestDigest", authResult.getFormDigest()).send();
		LOG.debug("statusCode={}", response.getStatus());
		Verify.verify(response.getStatus() == HttpStatus.OK_200, "response status not ok, statusCode={}", response.getStatus());
		
		String content = response.getContentAsString();
		LOG.debug("content={}", response.getStatus());
		Verify.verify(Strings.emptyToNull(content) != null, "response content does not contain data");
		
		return parse(content);
	}
	
	public SpFileProperties parse(String xml) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(xml));

		//TODO: should extract common namespaces used in project + xpath handling on jdom2
		Namespace a = Namespace.getNamespace("a", "http://www.w3.org/2005/Atom");
		Namespace d = Namespace.getNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
		Namespace m = Namespace.getNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");

		//TODO: reworking mapping handling when more properties is needed (and if custom ones too)
		EnumMap<SpFileProperties.CoreProperty, String> mapping = Maps.newEnumMap(SpFileProperties.CoreProperty.class);
		mapping.put(CoreProperty.TITLE, "vti_x005f_title");
		mapping.put(CoreProperty.SUBJECT, "Subject");
		mapping.put(CoreProperty.COMMENT, "OData__x005f_Comments");
		mapping.put(CoreProperty.KEYWORDS, "Keywords");
		mapping.put(CoreProperty.AUTHOR, "OData__x005f_Author");

		List<SpObject> children = Lists.newArrayList();
		XPathExpression<Element> filesExpr = XPathFactory.instance().compile("//m:properties", Filters.element(), null, a,d,m);
		List<Element> elements = filesExpr.evaluate(doc);
		Verify.verify(elements.size() == 1, "Something strange, expected 1 properties element, found %s", elements.size());
			
		Element properties = elements.get(0);
		EnumMap<SpFileProperties.CoreProperty, String> propertyValues = Maps.newEnumMap(SpFileProperties.CoreProperty.class);
		for (CoreProperty property : SpFileProperties.CoreProperty.values()) {
			String value = properties.getChildText(mapping.get(property), d);
			propertyValues.put(property, Strings.nullToEmpty(value));
		}
		return new SpFileProperties(propertyValues);
	}
}
