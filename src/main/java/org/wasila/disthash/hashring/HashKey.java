package org.wasila.disthash.hashring;

/**
 * Created by adam on 24.04.17.
 */
class HashKey implements Comparable<HashKey> {
    final long hashKey;

    public HashKey(long hashKey) {
        this.hashKey = hashKey;
    }

//    func hashVal(bKey []byte) HashKey {
//        return ((HashKey(bKey[3]) << 24) |
//                (HashKey(bKey[2]) << 16) |
//                (HashKey(bKey[1]) << 8) |
//                (HashKey(bKey[0])))
//    }

    public static HashKey hashVal(byte[] keyBytes) {
        return new HashKey((Byte.toUnsignedLong(keyBytes[3]) << 24L) |
                ((Byte.toUnsignedLong(keyBytes[2])) << 16L) |
                ((Byte.toUnsignedLong(keyBytes[1])) << 8L) |
                (Byte.toUnsignedLong(keyBytes[0])));
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
        return "HashKey{" +
                hashKey +
                '}';
    }
}
