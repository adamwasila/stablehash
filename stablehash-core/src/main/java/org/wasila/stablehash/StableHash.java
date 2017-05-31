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

import java.util.Optional;
import java.util.Set;

public interface StableHash<N> {

    /**
     * Returns node for given key.
     *
     * @param key Any string value
     * @return          Node assigned to the key given as an argument
     * @throws NullPointerException     if key value is null
     */
    Optional<N> getNode(String key);

    /**
     * Returns array of nodes for given key.
     *
     * @param key Any string value
     * @param size      Specifies how many nodes are expected to be returned
     * @return          Nodes assigned to the key given as an argument
     * @throws NullPointerException     if key value is null
     * @throws IllegalArgumentException if size is 0 or less or greater than total number of nodes
     */
    Set<N> getNodes(String key, int size);

    /**
     * Returns new instance of {@code StableHash} updated with new node. Node has weight 1 assigned
     * by default so it is equivalent to call addWeightedNode(node, 1) directly
     *
     * @param node   Name of node to be added
     * @return           New {@code StableHash} instance
     * @throws NullPointerException     if {@code node} is null
     */
    StableHash<N> addNode(N node);

    /**
     * Returns new instance of {@code StableHash} updated with new node of given weight.
     *
     * @param node   Name of node to be added
     * @param weight     Weight of node
     * @return           New {@code StableHash} instance
     * @throws NullPointerException     if {@code node} is null
     * @throws IllegalArgumentException if weight is 0 or less
     */
    StableHash<N> addWeightedNode(N node, int weight);

    /**
     * Returns new instance of {@code StableHash} updated with node and new weight of that node.
     *
     * @param node   Name of node
     * @param weight     Weight of node
     * @return           New {@code StableHash} instance
     * @throws NullPointerException     if {@code node} is null
     * @throws IllegalArgumentException if weight is 0 or less
     */
    StableHash<N> updateWeightedNode(N node, int weight);

    /**
     * Returns new instance of {@code StableHash} with given node removed.
     *
     * @param node  Name of node
     * @return          New {@code StableHash} instance
     * @throws NullPointerException     if {@code node} is null
     */
    StableHash<N> removeNode(N node);

}
