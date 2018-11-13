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

import org.jdom2.JDOMException;

public class SpFiles {
	public static void walkFileTree(
			ISpObjectProvider objectProvider, 
			String folderName,
			ISpFileVisitor visitor
			) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		SpFolder folder = objectProvider.getFolder(folderName);
		walk(
				objectProvider, 
				folder,
				visitor
				); 	
	}
	
	private static void walk(
			ISpObjectProvider objectProvider, 
			SpFolder folder,
			ISpFileVisitor visitor
			) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException {
		visitor.preVisitFolder(folder);
		for (SpObject child : folder.getChildren()) {
			if(child.isFolder()){
				walkFileTree(
						objectProvider,
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
