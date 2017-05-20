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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RendezvousHash<N> implements DistributedHash<N> {

    private HashUtil hashUtil;

    private final Map<N, Integer> nodes;

    public RendezvousHash() {
        hashUtil = new HashUtil();
        nodes = new HashMap<>();
    }

    public RendezvousHash(Collection<N> nodesList) {
        this();
        nodes.putAll(nodesList.stream().collect(Collectors.toMap(node -> node, node -> 1)));
    }

    public RendezvousHash(Map<N, Integer> weightedNodesList) {
        this();
        this.nodes.putAll(weightedNodesList);
    }

    @Override
    public Optional<N> getNode(String stringKey) {
        double highestScore = -1;
        N champion = null;
        for (Map.Entry<N, Integer> entry : nodes.entrySet()) {
            double newScore = getWeightedScore(stringKey, entry.getKey(), entry.getValue());
            if (newScore > highestScore) {
                champion = entry.getKey();
                highestScore = newScore;
            }
        }
        return Optional.ofNullable(champion);
    }

    @Override
    public Set<N> getNodes(String stringKey, int size) {
        return null;
    }

    @Override
    public RendezvousHash<N> addNode(N nodeName) {
        return null;
    }

    @Override
    public RendezvousHash<N> addWeightedNode(N nodeName, int weight) {
        return null;
    }

    @Override
    public RendezvousHash<N> updateWeightedNode(N nodeName, int weight) {
        return null;
    }

    @Override
    public RendezvousHash<N> removeNode(N nodeName) {
        return null;
    }

    private double getWeightedScore(String keyString, N node, int weight) {
        HashKey key = hashUtil.genKey(node.toString() + keyString);
        double score = key.toDouble();
        score = 1.0 / - Math.log(score);
        return weight * score;
    }

}
