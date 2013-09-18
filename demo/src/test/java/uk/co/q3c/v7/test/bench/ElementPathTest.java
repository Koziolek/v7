/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.test.bench;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ElementPathTest {

	@Test
	public void treeNode0Expand() {

		// given
		String expected = "ROOT::PID_SDefaultUserNavigationTree#n[0]/expand";
		// when
		String actual = ElementPath.path().id("DefaultUserNavigationTree").index(0).expand().get();
		// then
		assertThat(actual, is(expected));

	}

	@Test
	public void treeNode0_1Expand() {

		// given
		String expected = "ROOT::PID_SDefaultUserNavigationTree#n[0]/n[1]/expand";
		// when
		String actual = ElementPath.path().id("DefaultUserNavigationTree").index(0, 1).expand().get();
		// then
		assertThat(actual, is(expected));

	}

}
