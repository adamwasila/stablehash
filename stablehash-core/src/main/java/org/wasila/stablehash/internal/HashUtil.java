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
package org.wasila.stablehash.internal;

import org.wasila.stablehash.AuxHashKey;
import org.wasila.stablehash.HashFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

class HashUtil implements HashFactory {

    public Iterator<AuxHashKey> iterator(String key) {
        return new Iterator<AuxHashKey>() {
            private byte[] hash = hashDigest(key);
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return idx+4 <= hash.length;
            }

            @Override
            public HashKey next() {
                idx += 4;
                return HashKey.hashVal(hash, idx-4);
            }
        };
    }

    private byte[] hashDigest(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(key.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

}
