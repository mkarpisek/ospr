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

//TODO: fix exception handling
public interface ISpObjectProvider {
	SpFolder getFolder(String folder) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException;
	SpFileProperties getSpFileProperties(String file) throws InterruptedException, TimeoutException, ExecutionException, IOException, JDOMException;
}
