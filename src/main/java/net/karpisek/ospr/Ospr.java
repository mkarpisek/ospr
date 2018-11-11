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
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.CharStreams;

import net.karpisek.ospr.net.Folder;
import net.karpisek.ospr.net.GetWithAllSubfolders;
import net.karpisek.ospr.net.SharepointOnlineAuthentication;
import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;

// Office 365 Enterprise E3 Trial can be registered here:
//	https://sharepoint.stackexchange.com/questions/129573/create-a-free-sharepoint-online-site-office-365
//	https://portal.office.com/Signup/Signup.aspx?OfferId=B07A1127-DE83-4a6d-9F85-2C104BDAE8B4&dl=ENTERPRISEPACK&Country=US&culture=en-us&ali=1#0
//
public class Ospr {
	private static final Logger LOG = LoggerFactory.getLogger(Ospr.class);

	//usage 
	public static void main(String[] args) throws Exception {
		if(args.length != 4) {
			LOG.info("Usage: java -jar ospr.jar  <sharepointUrl> <username> <password> <site>");
			LOG.info("  <sharepointUrl> in format https://<yourdomain>.sharepoint.com");
			LOG.info("  <userName> in format: <userName>@<yourdomain>.onmicrosoft.com");
			LOG.info("  <password> for <username>");
			LOG.info("  <site> is name of site on <yourdomain>.sharepoint.com for which we want to get file report");
			return;
		}
		LOG.info("Start");
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

			SharepointOnlineAuthentication auth = new SharepointOnlineAuthentication.Builder(sharepoint)
				.browserUserAgent(browserUserAgent)
				.stsEndpoint(stsEndpoint)
				.username(username)
				.password(password)
				.build();
			
			Stopwatch authStopwatch = Stopwatch.createStarted();
			Result authResult = auth.execute(httpClient);
			LOG.info("authFinished timeMs={}",  authStopwatch.elapsed(TimeUnit.MILLISECONDS));
			 
			Stopwatch gettWithAllSubfoldersStopwatch = Stopwatch.createStarted();
			List<Folder> folders = new GetWithAllSubfolders(authResult, sharepointSiteEndpoint, rootFolder).execute(httpClient);
			LOG.info("getWithAllFoldersFinished count={} timeMs={}", folders.size(), gettWithAllSubfoldersStopwatch.elapsed(TimeUnit.MILLISECONDS));
			folders.forEach(folder -> {
				LOG.info(folder.toString());
			});

			LOG.info("End timeMs={}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
		} finally {
			httpClient.stop();
		}
	}

	public static String readResource(String name) throws IOException {
		try (InputStream stream = Ospr.class.getClassLoader().getResourceAsStream(name)) {
			return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
		}
	}

	public static HttpCookie getCookie(CookieStore store, String key) {
		for (HttpCookie cookie : store.getCookies()) {
			if (key.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}
}
