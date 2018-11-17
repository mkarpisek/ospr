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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.CharStreams;

import net.karpisek.ospr.net.HttpSpObjectProvider;
import net.karpisek.ospr.net.ISpFileVisitor;
import net.karpisek.ospr.net.SharepointOnlineAuthentication;
import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;
import net.karpisek.ospr.net.SpFile;
import net.karpisek.ospr.net.SpFiles;
import net.karpisek.ospr.net.SpFolder;
import net.karpisek.ospr.net.SpUri;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.DefaultExceptionHandler;
import picocli.CommandLine.Help;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.RunLast;

// Office 365 Enterprise E3 Trial can be registered here:
//	https://sharepoint.stackexchange.com/questions/129573/create-a-free-sharepoint-online-site-office-365
//	https://portal.office.com/Signup/Signup.aspx?OfferId=B07A1127-DE83-4a6d-9F85-2C104BDAE8B4&dl=ENTERPRISEPACK&Country=US&culture=en-us&ali=1#0
//
//TODO: support version command parameter - defined and taken from .pom(or manifest.mf) on build time
@Command(name = "java -jar ospr.jar", mixinStandardHelpOptions = true, description="Office 365 Sharepoint File Reporting Tool")
public class Ospr implements Callable<Integer>{
	private static final Logger LOG = LoggerFactory.getLogger(Ospr.class);
	
	public static void main(String[] args) throws Exception {
//		CommandLine.call(new Ospr(), args);
        CommandLine cmd = new CommandLine(new Ospr());
        cmd.registerConverter(SpUri.class, new SpUriConverter());
        cmd.parseWithHandlers(
    		new RunLast().useOut(System.out).useAnsi(Help.Ansi.AUTO), 
    		new DefaultExceptionHandler<List<Object>>().useErr(System.err).useAnsi(Help.Ansi.AUTO), 
    		args
        );
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
	
	private static class SpUriConverter implements ITypeConverter<SpUri> {
	    @Override
		public SpUri convert(String value) throws Exception {
	    	return SpUri.fromString(value);	
	    }
	}

	@Option(names = { "-u", "--user" }, required=true, paramLabel="USERNAME", description = "sharepoint account username in format <userName>@<yourdomain>.onmicrosoft.com")
	private String username;	
	@Option(names = { "-p", "--password" }, required=true, paramLabel="PASSWORD", description = "for sharepoint account to use")
	private String password;								
	@Parameters(arity="1", paramLabel="URL", description = "sharepoint site/library/folder/subfolder url, in format 'https://yourdomain.sharepoint.com/sites/siteName/libraryName/folderName', if URL is for site only uses 'Shared Documents' as default libraryName")
	private SpUri sharepointUri;


	@Override
	public Integer call() throws Exception {
		LOG.info("Start");
		Stopwatch stopwatch = Stopwatch.createStarted();
		String stsEndpoint = "https://login.microsoftonline.com/extSTS.srf";
		String browserUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:17.0) Gecko/20100101 Firefox/17.0";

		// Instantiate HttpClient
		HttpClient httpClient = new HttpClient();

		// Configure HttpClient, for example:
		httpClient.setFollowRedirects(false);

		// Start HttpClient
		try {
			httpClient.start();

			SharepointOnlineAuthentication auth = new SharepointOnlineAuthentication.Builder(sharepointUri)
				.browserUserAgent(browserUserAgent)
				.stsEndpoint(stsEndpoint)
				.username(username)
				.password(password)
				.build();
			
			Stopwatch authStopwatch = Stopwatch.createStarted();
			Result authResult = auth.execute(httpClient);
			LOG.info("authFinished timeMs={}",  authStopwatch.elapsed(TimeUnit.MILLISECONDS));
			
			Path fileTreeWalkFile = Paths.get("fileTreeWalk.txt");
			try(BufferedWriter writer = Files.newBufferedWriter(fileTreeWalkFile)){
				SpFiles.walkFileTree(new HttpSpObjectProvider(httpClient, authResult, sharepointUri.getSiteUri()), sharepointUri.getPath(), new ISpFileVisitor() {
					@Override
					public void preVisitFolder(SpFolder folder) throws IOException {
						LOG.debug("preVisitDirectory={}", folder);
						writer.write(folder.getServerRelativeUrl());
						writer.newLine();
					}
	
					@Override
					public void visitFile(SpFile file) throws IOException {
						LOG.debug("visitFile={}", file);
						writer.write(file.getServerRelativeUrl());
						writer.newLine();
					}
	
					@Override
					public void postVisitFolder(SpFolder folder) throws IOException {
						LOG.debug("postVisitDirectory={}", folder);
					}
				});
			}
			LOG.info("FileTreeWalkDone output={}", fileTreeWalkFile.toAbsolutePath());
			 

			LOG.info("End timeMs={}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
		} finally {
			httpClient.stop();
		}
		return 0;
	}

}
