package org.rapidoid.lambda;

/*
 * #%L
 * rapidoid-commons
 * %%
 * Copyright (C) 2014 - 2017 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * @author Nikolche Mihajlovski
 * @since 2.0.0
 */
public interface Predicate<T> {

	@SuppressWarnings("rawtypes")
	public static final Predicate<?> ALWAYS_TRUE = new Predicate() {
		@Override
		public boolean eval(Object param) throws Exception {
			return true;
		}
	};

	@SuppressWarnings("rawtypes")
	public static final Predicate<?> ALWAYS_FALSE = new Predicate() {
		@Override
		public boolean eval(Object param) throws Exception {
			return false;
		}
	};

	boolean eval(T param) throws Exception;

}
