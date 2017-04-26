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

import java.util.Map;
import java.util.Optional;

public interface DistributedHash {

    Optional<String> getNode(String stringKey);

    Optional<String[]> getNodes(String stringKey, int size);

    HashRing addNode(String nodeName);

    HashRing addWeightedNode(String nodeName, int weight);

    HashRing updateWeightedNode(String nodeName, int weight);

    HashRing removeNode(String nodeName);

    static DistributedHash newConsistentHash(String... nodes) {
        return new HashRing(nodes);
    }

    static DistributedHash newConsistentHash(Map<String,Integer> weightedNodes) {
        return new HashRing(weightedNodes);
    }

}
