/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.pm.services;
/*
 * Copyright 2006-2007 The Herringroe Team.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Arrays;
import java.util.Iterator;

/**
 * <code><pre>
 * public int hashCode(){
 *     return new HashCodeBuilder().
 *     		append(field1).
 *     		append(field2).
 *     		append(field3).
 *     		getHashCode();
 * }
 * </pre></code>
 * 
 * @see Object#hashCode()
 * 
 * @author Katsunori Koyanagi
 * @version 1.0
 */
public class HashCodeBuilder {

	private static class PrimeNumberIterator implements Iterator<Integer> {

		private int idx = 0;

		PrimeNumberIterator() {
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return true;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Integer next() {
			this.idx++;
			if (this.idx == HashCodeBuilder.PRIME_NUMBERS.length) {
				this.idx = 0;
			}

			return HashCodeBuilder.PRIME_NUMBERS[this.idx];
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final int[] PRIME_NUMBERS = new int[] { 13, 31, 73, 127,
			179, 233, 283, 353, 419, 467, 547, 607, 661, 739, 811, 877, 947,
			1019, 1087, 1153, 1229, 1297, 1381, 1453, 1523, 1597, 1663, 1741,
			1823, 1901, 1993, 2063, 2131, 2221, 2293, 2371, 2437, 2539, 2621,
			2689, 2749, 2833, 2909, 3001, 3083, 3187, 3259, 3343, 3433, 3517,
			3581, 3659, 3733, 3823, 3911, 4001, 4073, 4153, 4241, 4327, 4421,
			4507, 4591, 4663, 4759, 4861, 4943, 5009, 5099, 5189, 5281, 5393,
			5449, 5527, 5641, 5701, 5801, 5861, 5953, 6067, 6143, 6229, 6311,
			6373, 6481, 6577, 6679, 6763, 6841, 6947, 7001, 7109, 7211, 7307,
			7417, 7507, 7573, 7649, 7727, 7841, 7927, 8039, 8117, 8221, 8293,
			8389, 8513, 8599, 8681, 8747, 8837, 8933, 9013, 9127, 9203, 9293,
			9391, 9461, 9539, 9643, 9739, 9817, 9901, 10009, 10103, 10181,
			10273, 10357 };

	private int hashCode;

	private PrimeNumberIterator primeNumbers;

	public HashCodeBuilder() {
		this.primeNumbers = new PrimeNumberIterator();
		this.hashCode = 0;
	}

	public HashCodeBuilder append(Object value) {
		this.hashCode = this.hashCode
				+ this.computeHashCode(this.primeNumbers.next(), value);
		return this;
	}

	private int computeHashCode(int prime, Object value) {
		if (value == null) {
			return 0;
		}

		int hash;
		if (value.getClass().isArray()) {
			Class<?> type = value.getClass().getComponentType();
			if (type == int.class) {
				hash = Arrays.hashCode((int[]) value);
			} else if (type == double.class) {
				hash = Arrays.hashCode((int[]) value);
			} else if (type == char.class) {
				hash = Arrays.hashCode((char[]) value);
			} else if (type == boolean.class) {
				hash = Arrays.hashCode((boolean[]) value);
			} else if (type == long.class) {
				hash = Arrays.hashCode((long[]) value);
			} else if (type == float.class) {
				hash = Arrays.hashCode((float[]) value);
			} else if (type == short.class) {
				hash = Arrays.hashCode((short[]) value);
			} else if (type == byte.class) {
				hash = Arrays.hashCode((byte[]) value);
			} else {
				PrimeNumberIterator iterator = new PrimeNumberIterator();
				Object[] values = (Object[]) value;
				hash = 0;
				for (Object element : values) {
					hash = hash
							+ this.computeHashCode(prime * iterator.next(),
									element);
				}
			}
		} else {
			hash = value.hashCode();
		}

		return hash * prime;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public int getHashCode() {
		return this.hashCode;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
