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
import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.net.UrlEscapers;

import net.karpisek.ospr.Ospr;

/**
 * Perform authentication into Office 365 Sharepoint (Sharepoint Online) using user credentials.
 * 
 * Result must be passed to any subsequent call to Sharepoint API
 * 
 * @see http://paulryan.com.au/2014/spo-remote-authentication-rest/amp/
 * @see https://erwinkoens.wordpress.com/2015/02/05/sharepoint-online-authentication-without-sharepoint-api/
 * @see http://www.wictorwilen.se/Post/How-to-do-active-authentication-to-Office-365-and-SharePoint-Online.aspx
 * 
 */
public class SharepointOnlineAuthentication {
	public static class Builder{
		private String sharepoint;
		private String browserUserAgent;
		private String stsEndpoint;
		private String username;
		private String password;
		
		public Builder(String sharepoint) {
			this.sharepoint = sharepoint;
		}
		
		public Builder browserUserAgent(String value) {
			this.browserUserAgent = value;
			return this;
		}
		
		public Builder stsEndpoint(String value) {
			this.stsEndpoint = value;
			return this;
		}
		
		public Builder username(String value) {
			this.username = value;
			return this;
		}
		
		public Builder password(String value) {
			this.password = value;
			return this;
		}
		
		public SharepointOnlineAuthentication build() {
			return new SharepointOnlineAuthentication(sharepoint, browserUserAgent, stsEndpoint, username, password);
		}
	}
	
	public static class Result{
		public Result(String sharepoint, String rtFa, String fedAuth, String binarySecurityToken, String formDigest) {
			this.sharepoint = sharepoint;
			this.rtFa = rtFa;
			this.fedAuth = fedAuth;
			this.binarySecurityToken = binarySecurityToken;
			this.formDigest = formDigest;
		}
		
		final String sharepoint;
		final String rtFa;
		final String fedAuth;
		final String binarySecurityToken;
		final String formDigest;
		
		public String getSharepoint() {
			return sharepoint;
		}

		public String getRtFa() {
			return rtFa;
		}

		public String getFedAuth() {
			return fedAuth;
		}

		public String getBinarySecurityToken() {
			return binarySecurityToken;
		}

		public String getFormDigest() {
			return formDigest;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("sharepoint", sharepoint).add("rtFa", rtFa).add("fedAuth", fedAuth).add("binarySecurityToken", binarySecurityToken).add("formDigest", formDigest).toString();
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SharepointOnlineAuthentication.class);
	private String sharepoint;
	private String browserUserAgent;
	private String stsEndpoint;
	private String username;
	private String password;
	
	private SharepointOnlineAuthentication(String sharepoint, String browserUserAgent, String stsEndpoint, String username, String password) {
		this.sharepoint = sharepoint;
		this.browserUserAgent = browserUserAgent;
		this.stsEndpoint = stsEndpoint;
		this.username = username;
		this.password = password;
	}
	
	public Result execute(HttpClient httpClient) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		LOG.debug("## 1-rpsContextCookie");
		ContentResponse response1 = httpClient
				.newRequest(sharepoint + "/_layouts/15/Authenticate.aspx?Source=" + UrlEscapers.urlFragmentEscaper().escape("sharepoint"))
				.method(HttpMethod.GET)
				.agent(browserUserAgent)
				.send();
		LOG.debug("statusCode={}", response1.getStatus());
		LOG.debug("cookies:");
		httpClient.getCookieStore().getCookies().stream().forEach(each -> LOG.debug("\t" + each.getName() + ": " + each.getValue()));

		LOG.debug("## 2-sts.sh");
		String template = Ospr.readResource("sts-request.xml");
		Verify.verify(!Strings.nullToEmpty(template).trim().isEmpty(), "Failed to load template for sts-request");
		String requestContent = template
				.replace("${USERNAME}", username)
				.replace("${PASSWORD}", password)
				.replace("${ENDPOINT}", sharepoint);
		ContentResponse response2 = httpClient
				.newRequest(stsEndpoint)
				.method(HttpMethod.POST)
				.agent(browserUserAgent)
				.content(new StringContentProvider(requestContent))
				.send();

		LOG.debug("statusCode={}",response2.getStatus());
		String content2 = response2.getContentAsString();
		Verify.verify(Strings.emptyToNull(content2) != null, "response content does not contain data");

		SAXBuilder builder = new SAXBuilder();
		Document doc2 = builder.build(new StringReader(content2));

		// use the default implementation
		XPathFactory xFactory = XPathFactory.instance();

		// select all links
		Namespace wsse = Namespace.getNamespace("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		XPathExpression<Element> expr = xFactory.compile("//wsse:BinarySecurityToken", Filters.element(), null, wsse);
		List<Element> links = expr.evaluate(doc2);
		Verify.verify(!links.isEmpty(), "response does not contain wsse:BinarySecurityToken");
		String binarySecurityToken = links.get(0).getText();
		Verify.verify(!binarySecurityToken.isEmpty(), "binarySecurityToken should not be empty");
		LOG.debug("binarySecurityToken=" + binarySecurityToken);

		//
		LOG.debug("## 3-getAccessToken");
		ContentResponse response3 = httpClient.newRequest(sharepoint + "/_forms/default.aspx?wa=wsignin1.0")
				.method(HttpMethod.POST)
				.agent(browserUserAgent)
				.content(new StringContentProvider(binarySecurityToken))
				.send();
		LOG.debug("statusCode={}", response3.getStatus());
		LOG.debug("cookies:");
		httpClient.getCookieStore().getCookies().stream().forEach(each -> LOG.debug("\t" + each.getName() + ": " + each.getValue()));
		HttpCookie rtFa = Ospr.getCookie(httpClient.getCookieStore(), "rtFa");
		HttpCookie fedAuth = Ospr.getCookie(httpClient.getCookieStore(), "FedAuth");
		LOG.debug("rtFa=" + rtFa);
		LOG.debug("fedAuth=" + fedAuth);
		Verify.verify(rtFa != null, "rtFa not found");
		Verify.verify(fedAuth != null, "fedAuth not found");

		LOG.debug("## 4-getRequestDigest");
		ContentResponse response4 = httpClient.newRequest(sharepoint + "/_api/contextinfo")
				.method(HttpMethod.POST)
				.agent(browserUserAgent)
				.content(new StringContentProvider(binarySecurityToken))
				.send();
		LOG.debug("statusCode= {}", response4.getStatus());
		String content4 = response4.getContentAsString();
		Verify.verify(Strings.emptyToNull(content4) != null, "response content does not contain data");
		LOG.debug(content4);

		Document doc4 = builder.build(new StringReader(content4));

		Namespace d = Namespace.getNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
		XPathExpression<Element> expr4 = xFactory.compile("//d:FormDigestValue", Filters.element(), null, d);
		List<Element> links4 = expr4.evaluate(doc4);
		Verify.verify(!links4.isEmpty(), "response does not contain d:FormDigestValue");
		String formDigest = links.get(0).getText();
		Verify.verify(!formDigest.isEmpty(), "formDigest should not be empty");
		LOG.debug("formDigest=" + formDigest);
		
		return new Result(sharepoint, rtFa.getValue(), fedAuth.getValue(), binarySecurityToken, formDigest);
	}
}
