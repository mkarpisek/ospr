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
package net.karpisek.ospr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;

// Office 365 Enterprise E3 Trial can be registered here:
//	https://sharepoint.stackexchange.com/questions/129573/create-a-free-sharepoint-online-site-office-365
//	https://portal.office.com/Signup/Signup.aspx?OfferId=B07A1127-DE83-4a6d-9F85-2C104BDAE8B4&dl=ENTERPRISEPACK&Country=US&culture=en-us&ali=1#0
//
// Sharepoint Online login based on:
//	http://paulryan.com.au/2014/spo-remote-authentication-rest/amp/
//	https://erwinkoens.wordpress.com/2015/02/05/sharepoint-online-authentication-without-sharepoint-api/
//	http://www.wictorwilen.se/Post/How-to-do-active-authentication-to-Office-365-and-SharePoint-Online.aspx
public class Ospr {
	//usage 
	public static void main(String[] args) throws Exception {
		if(args.length != 4) {
			System.out.println("Usage: java -jar ospr.jar  <sharepointUrl> <username> <password> <site>");
			System.out.println("  <sharepointUrl> in format https://<yourdomain>.sharepoint.com");
			System.out.println("  <userName> in format: <userName>@<yourdomain>.onmicrosoft.com");
			System.out.println("  <password> for <username>");
			System.out.println("  <site> is name of site on <yourdomain>.sharepoint.com for which we want to get file report");
			return;
		}
		System.out.println("Start");
		Stopwatch stopwatch = Stopwatch.createStarted();
		String username = args[1];			 
		String password = args[2];									
		String sharepoint = args[0];
		String sharepointSite = args[3];
		String sharepointSiteEndpoint = sharepoint + "/sites/" + sharepointSite;
		String stsEndpoint = "https://login.microsoftonline.com/extSTS.srf";
		String browserUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:17.0) Gecko/20100101 Firefox/17.0";
		String rootFolder = "Shared Documents";

		// Instantiate HttpClient
		HttpClient httpClient = new HttpClient();

		// Configure HttpClient, for example:
		httpClient.setFollowRedirects(false);

		// Start HttpClient
		try {
			httpClient.start();

			System.out.println("## 1-rpsContextCookie.sh");
			ContentResponse response1 = httpClient
					.newRequest(sharepoint + "/_layouts/15/Authenticate.aspx?Source=" + UrlEscapers.urlFragmentEscaper().escape("sharepoint"))
					.method(HttpMethod.GET).agent(browserUserAgent).send();
			System.out.println(response1.getStatus());
			System.out.println("cookies:");
			httpClient.getCookieStore().getCookies().stream().forEach(each -> System.out.println("\t" + each.getName() + ": " + each.getValue()));

			System.out.println("## 2-sts.sh");
			String template = readResource("sts-request.xml");
			Verify.verify(!Strings.nullToEmpty(template).trim().isEmpty(), "Failed to load template for sts-request");
			String requestContent = template.replace("${USERNAME}", username).replace("${PASSWORD}", password).replace("${ENDPOINT}", sharepoint);
			ContentResponse response2 = httpClient.newRequest(stsEndpoint).method(HttpMethod.POST).agent(browserUserAgent)
					.content(new StringContentProvider(requestContent)).send();

			System.out.println(response2.getStatus());
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
			System.out.println("binarySecurityToken=" + binarySecurityToken);

			//
			System.out.println("## 3-getAccessToken");
			ContentResponse response3 = httpClient.newRequest(sharepoint + "/_forms/default.aspx?wa=wsignin1.0").method(HttpMethod.POST).agent(browserUserAgent)
					.content(new StringContentProvider(binarySecurityToken)).send();
			System.out.println(response3.getStatus());
			System.out.println("cookies:");
			httpClient.getCookieStore().getCookies().stream().forEach(each -> System.out.println("\t" + each.getName() + ": " + each.getValue()));
			HttpCookie rtFa = getCookie(httpClient.getCookieStore(), "rtFa");
			HttpCookie fedAuth = getCookie(httpClient.getCookieStore(), "FedAuth");
			System.out.println("rtFa=" + rtFa);
			System.out.println("fedAuth=" + fedAuth);
			Verify.verify(rtFa != null, "rtFa not found");
			Verify.verify(fedAuth != null, "fedAuth not found");

			System.out.println("## 4-getRequestDigest");
			ContentResponse response4 = httpClient.newRequest(sharepoint + "/_api/contextinfo").method(HttpMethod.POST).agent(browserUserAgent)
					.content(new StringContentProvider(binarySecurityToken)).send();
			System.out.println(response4.getStatus());
			String content4 = response4.getContentAsString();
			Verify.verify(Strings.emptyToNull(content4) != null, "response content does not contain data");
			System.out.println(content4);

			Document doc4 = builder.build(new StringReader(content4));

			Namespace d = Namespace.getNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
			XPathExpression<Element> expr4 = xFactory.compile("//d:FormDigestValue", Filters.element(), null, d);
			List<Element> links4 = expr4.evaluate(doc4);
			Verify.verify(!links4.isEmpty(), "response does not contain d:FormDigestValue");
			String formDigest = links.get(0).getText();
			Verify.verify(!formDigest.isEmpty(), "formDigest should not be empty");
			System.out.println("formDigest=" + formDigest);

			System.out.println("## 5-listSubfolders");
			String url5 = sharepointSiteEndpoint + "/_api/Web/GetFolderByServerRelativeUrl(%27" + UrlEscapers.urlFragmentEscaper().escape(rootFolder)
					+ "%27)/Folders";
			ContentResponse response5 = httpClient.newRequest(url5).method(HttpMethod.GET).header("X-RequestDigest", formDigest).agent(browserUserAgent).send();
			System.out.println(response5.getStatus());
			String content5 = response5.getContentAsString();
			Files.write(FileSystems.getDefault().getPath("D:\\Odbavit\\sharepoint\\response.xml"), content5.getBytes());
			Verify.verify(Strings.emptyToNull(content4) != null, "response content does not contain data");
			Document doc5 = builder.build(new StringReader(content5));

			Namespace m = Namespace.getNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
			XPathExpression<Element> expr5 = xFactory.compile("//m:properties", Filters.element(), null, m);
			List<Element> links5 = expr5.evaluate(doc5);
			System.out.println("//m:properties=" + links5.size());
			int elementCount = 0;
			for (Element properties : links5) {
				System.out.println("\t[" + elementCount + "]");
				for (String propertyName : Lists.newArrayList("Name", "ServerRelativeUrl", "TimeLastModified", "TimeCreated", "ItemCount")) {
					String value = Strings.nullToEmpty(properties.getChildText(propertyName, d));
					System.out.println("\t\t" + propertyName + ": '" + value + "'");
				}
				elementCount++;
			}

			System.out.println("End (t=" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "[ms])");
		} finally {
			httpClient.stop();
		}
	}

	private static HttpCookie getCookie(CookieStore store, String key) {
		for (HttpCookie cookie : store.getCookies()) {
			if (key.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	private static String readResource(String name) throws IOException {
		try (InputStream stream = Ospr.class.getClassLoader().getResourceAsStream(name)) {
			return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
		}
	}
}
