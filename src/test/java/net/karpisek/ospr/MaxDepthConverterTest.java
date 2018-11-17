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

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MaxDepthConverterTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void test_0() throws Exception {
		assertEquals(0, new Ospr.MaxDepthConverter().convert("0").intValue());
	}
	@Test
	public void test_1() throws Exception {
		assertEquals(1, new Ospr.MaxDepthConverter().convert("1").intValue());
	}
	@Test
	public void test_2() throws Exception {
		assertEquals(2, new Ospr.MaxDepthConverter().convert("2").intValue());
	}
	@Test
	public void test_unlimited() throws Exception {
		assertEquals(Ospr.UNLIMITED_DEPTH, new Ospr.MaxDepthConverter().convert(Integer.toString(Ospr.UNLIMITED_DEPTH)).intValue());
	}
	@Test
	public void test_unsupported() throws Exception {
	    thrown.expect(IllegalArgumentException.class);
	    thrown.expectMessage("Illegal maximal traversal depth value, must be >= 0 or -1 for unlimited (is '-2')");
		new Ospr.MaxDepthConverter().convert("-2").intValue();
	}
}
