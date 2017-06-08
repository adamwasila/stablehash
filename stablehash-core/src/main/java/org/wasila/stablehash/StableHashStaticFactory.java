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

import org.wasila.stablehash.internal.ConsistentHash;
import org.wasila.stablehash.internal.RendezvousHash;

import java.util.Collection;
import java.util.Map;

public class StableHashStaticFactory {

    public static <N> StableHash<N> newConsistentHash(HashFactory hashFactory, Collection<N> nodes) {
        return new ConsistentHash<>(hashFactory, nodes);
    }

    public static <N> StableHash<N> newConsistentHash(HashFactory hashFactory, Map<N, Integer> weightedNodes) {
        return new ConsistentHash<>(hashFactory, weightedNodes);
    }

    public static <N> StableHash<N> newConsistentHash(Collection<N> nodes) {
        return new ConsistentHash<>(nodes);
    }

    public static <N> StableHash<N> newConsistentHash(Map<N, Integer> weightedNodes) {
        return new ConsistentHash<>(weightedNodes);
    }

    public static <N> StableHash<N> newRendezvousHash(HashFactory hashFactory, Collection<N> nodes) {
        return new RendezvousHash<>(hashFactory, nodes);
    }

    public static <N> StableHash<N> newRendezvousHash(HashFactory hashFactory, Map<N, Integer> weightedNodes) {
        return new RendezvousHash<>(hashFactory, weightedNodes);
    }

    public static <N> StableHash<N> newRendezvousHash(Collection<N> nodes) {
        return new RendezvousHash<>(nodes);
    }

    public static <N> StableHash<N> newRendezvousHash(Map<N, Integer> weightedNodes) {
        return new RendezvousHash<>(weightedNodes);
    }

}
