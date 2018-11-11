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
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jetty.client.HttpClient;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;

/**
 * Answers information about all direct subfolders for the requested folder (including itself).
 */
public class GetWithAllSubfolders {
	private static final Logger LOG = LoggerFactory.getLogger(SharepointOnlineAuthentication.class);

	private Result authResult;
	private String sharepointSiteEndpoint;
	private String folder;

	public GetWithAllSubfolders(SharepointOnlineAuthentication.Result authResult, String sharepointSiteEndpoint, String folder) {
		this.authResult = authResult;
		this.sharepointSiteEndpoint = sharepointSiteEndpoint;
		this.folder = folder;
	}

	public List<Folder> execute(HttpClient httpClient) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		List<Folder> results = Lists.newArrayList();
		
		//first top-level folder ("=With")
		results.add(new GetFolder(authResult, sharepointSiteEndpoint, folder).execute(httpClient));
		
		//now all subfolders - recursively ("=AllSubfolders")
		Deque<String> paths = Queues.newLinkedBlockingDeque();
		paths.add(folder);
		while (!paths.isEmpty()) {
			String folderPath = paths.removeFirst();
			LOG.debug(folderPath);

			List<Folder> subfolders = new GetSubfolders(authResult, sharepointSiteEndpoint, folderPath).execute(httpClient);
			LOG.debug("folder={}", folderPath);
			LOG.debug("subfoldersSize={} details={}", subfolders.size(), subfolders.stream().map(each -> each.toString()).collect(Collectors.toList()));

			paths.addAll(subfolders.stream().map(each -> each.getServerRelativeUrl()).collect(Collectors.toList()));
			
			results.addAll(subfolders);
		}
		return results;
	}
}
