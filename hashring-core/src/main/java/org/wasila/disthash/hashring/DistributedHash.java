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
package org.wasila.disthash.hashring;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DistributedHash<N> {

    Optional<N> getNode(String stringKey);

    Set<N> getNodes(String stringKey, int size);

    DistributedHash addNode(N nodeName);

    DistributedHash addWeightedNode(N nodeName, int weight);

    DistributedHash updateWeightedNode(N nodeName, int weight);

    DistributedHash removeNode(N nodeName);

    static <N> DistributedHash newConsistentHash(Collection<N> nodes) {
        return new HashRing<N>(nodes);
    }

    static <N> DistributedHash newConsistentHash(Map<N,Integer> weightedNodes) {
        return new HashRing<N>(weightedNodes);
    }

}
