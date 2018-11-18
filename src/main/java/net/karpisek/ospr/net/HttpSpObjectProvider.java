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
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.jdom2.JDOMException;

import net.karpisek.ospr.net.SharepointOnlineAuthentication.Result;

/**
 * Provider of sharepoint files library object information using http REST API for remote calls.
 */
public class HttpSpObjectProvider implements ISpObjectProvider {
	private HttpClient httpClient;
	private Result authResult;
	private URI uri;

	public HttpSpObjectProvider(
		HttpClient httpClient, 
		SharepointOnlineAuthentication.Result authResult, 
		URI uri
	) {
		this.httpClient = httpClient;
		this.authResult = authResult;
		this.uri = uri;
	}

	@Override
	public SpFolder getFolder(String folder) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		return new GetFolder(authResult, uri, folder).execute(httpClient);
	}

	@Override
	public SpFileProperties getSpFileProperties(String file)
			throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		return new GetSpFileProperties(authResult, uri, file).execute(httpClient);
	}

}
