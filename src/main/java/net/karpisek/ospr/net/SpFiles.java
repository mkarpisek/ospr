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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.jdom2.JDOMException;

public class SpFiles {
	public static void walkFileTree(
			HttpClient httpClient, 
			SharepointOnlineAuthentication.Result authResult, 
			String sharepointSiteEndpoint, 
			String folderName,
			SpFileVisitor visitor
			) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		SpFolder folder = new GetFolder(authResult, sharepointSiteEndpoint, folderName).execute(httpClient);
		walk(
				httpClient, 
				authResult, 
				sharepointSiteEndpoint, 
				folder,
				visitor
				); 	
	}
	
	private static void walk(
			HttpClient httpClient, 
			SharepointOnlineAuthentication.Result authResult, 
			String sharepointSiteEndpoint, 
			SpFolder folder,
			SpFileVisitor visitor
			) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		visitor.preVisitFolder(folder);
		for (SpObject child : folder.getChildren()) {
			if(child.isFolder()){
				walkFileTree(
						httpClient, 
						authResult, 
						sharepointSiteEndpoint, 
						child.getServerRelativeUrl(),
						visitor
						); 				
			}
			else {
				visitor.visitFile((SpFile) child);
			}
		}
		visitor.postVisitFolder(folder);
	}	
}
