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
package uk.co.q3c.v7.base.shiro;

public interface URIPermissionFactory {

	/**
	 * Creates a Permission object from the uri. The '/' characters are changed to ':' to facilitate use of Shiro
	 * WildcardPermission
	 * 
	 * @param uri
	 * @return
	 */
	URIViewPermission createViewPermission(String uri);

	/**
	 * Creates a Permission object from the uri. The '/' characters are changed to ':' to facilitate use of Shiro
	 * WildcardPermission. If {@code appendWildCard} is true, a final ':*' is added. The fill translation is, for
	 * example, for a URI of:<br>
	 * <br>
	 * <i>private/deptx/teamy/current projects</i> becomes a Shiro permission of <br>
	 * <br>
	 * <i>uri:view:private:deptx:teamy:current projects</i> with no wildcard, or <br>
	 * <br>
	 * <i>uri:view:private:deptx:teamy:current projects:*</i> with a wildcard
	 * 
	 * @param uri
	 * @param appendWildcard
	 * @return
	 */
	URIViewPermission createViewPermission(String uri, boolean appendWildcard);

}
