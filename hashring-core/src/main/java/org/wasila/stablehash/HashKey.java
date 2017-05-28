/**
 * (C) Copyright 2017 Adam Wasila.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wasila.stablehash;

/**
 * The {@code HashKey} class holds hash value of given key. As md5 algorithm is used to generate key
 * only four bytes are taken into account to calculate the hash value.
 *
 * This class is used only internally meaning it is not part of the API.
 */
class HashKey implements Comparable<HashKey> {

    private final long fto = (0xFFFFFFFFFFFFFFFFL >> (64 - 53));
    private final double ftz = (double)(1L << 53);

    final long hashKey;

    private HashKey(long hashKey) {
        this.hashKey = hashKey;
    }

    /**
     * Create new HashKey instance using first four bytes of given byte array.
     *
     * @param keyBytes                        array of bytes; minimum size is 4
     * @return                                HashKey instance
     * @throws ArrayIndexOutOfBoundsException If provided array has less than 4 elements
     */
    public static HashKey hashVal(byte[] keyBytes) {
        return new HashKey((Byte.toUnsignedLong(keyBytes[3]) << 24L) |
                ((Byte.toUnsignedLong(keyBytes[2])) << 16L) |
                ((Byte.toUnsignedLong(keyBytes[1])) << 8L) |
                (Byte.toUnsignedLong(keyBytes[0])));
    }


    public double toDouble() {
        return (this.hashKey & fto) / ftz;
    }

    @Override
    public int compareTo(HashKey comparedKey) {
        return Long.compare(hashKey, comparedKey.hashKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashKey hashKey1 = (HashKey) o;

        return hashKey == hashKey1.hashKey;
    }

    @Override
    public int hashCode() {
        return (int) (hashKey ^ (hashKey >>> 32));
    }

    @Override
    public String toString() {
        return "HashKey{" + hashKey + '}';
    }
}
