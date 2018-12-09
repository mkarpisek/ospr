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
package net.karpisek.ospr.pdf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MetadataTest {

	@Test
	public void testStandardMetadata() throws InvalidPasswordException, IOException {
		List<String> expected = Lists.newArrayList("Author=Martin Karpisek", "CreationDate=D:20181209140710+01'00'", "Creator=Writer",
				"Keywords=keyword1, keyword2", "Producer=LibreOffice 5.4", "Subject=PDF Subject", "Title=PDF Title");

		List<String> actual = Lists.newArrayList();
		try (InputStream is = Files.newInputStream(Paths.get("src/test/resources/test1site/documents/test.pdf")); PDDocument doc = PDDocument.load(is)) {

			PDDocumentInformation docInfo = doc.getDocumentInformation();
			actual = docInfo.getMetadataKeys().stream().map(e -> e + "=" + docInfo.getCustomMetadataValue(e)).collect(Collectors.toList());
		}

		assertEquals(Joiner.on("\n").join(expected), Joiner.on("\n").join(actual));
	}
}