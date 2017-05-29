package org.wasila.stablehash;

import org.wasila.stablehash.internal.ConsistentHash;
import org.wasila.stablehash.internal.RendezvousHash;

import java.util.Collection;
import java.util.Map;

public class StableHashStaticFactory {

    static <N> StableHash<N> newConsistentHash(Collection<N> nodes) {
        return new ConsistentHash<N>(nodes);
    }

    static <N> StableHash<N> newConsistentHash(Map<N, Integer> weightedNodes) {
        return new ConsistentHash<N>(weightedNodes);
    }

    static <N> StableHash<N> newRendezvousHash(Collection<N> nodes) {
        return new RendezvousHash<N>(nodes);
    }

    static <N> StableHash<N> newRendezvousHash(Map<N, Integer> weightedNodes) {
        return new RendezvousHash<>(weightedNodes);
    }

}
