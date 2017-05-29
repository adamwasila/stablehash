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

    Optional<N> getNode(String key);

    Set<N> getNodes(String key, int size);

    StableHash<N> addNode(N node);

    StableHash<N> addWeightedNode(N node, int weight);

    StableHash<N> updateWeightedNode(N node, int weight);

    StableHash<N> removeNode(N node);

}
