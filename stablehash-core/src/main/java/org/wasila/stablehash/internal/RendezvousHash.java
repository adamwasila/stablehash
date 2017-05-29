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

import org.wasila.stablehash.StableHash;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Weighted Rendezvous hashing implementation base on algorithm presented by Jason Resch.
 *
 * For more info and comparison to different methods, please visit:
 * http://www.snia.org/sites/default/files/SDC15_presentations/dist_sys/Jason_Resch_New_Consistent_Hashings_Rev.pdf
 *
 * @param <N> Node type
 */
public class RendezvousHash<N> implements StableHash<N> {

    private InputValidator<N> validator;

    private HashUtil hashUtil;
    private final Map<N, Integer> nodes;

    public RendezvousHash() {
        validator = new InputValidator<N>();
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
    public Optional<N> getNode(String key) {
        validator.validateGetNode(key);
        double highestScore = -1;
        N champion = null;
        for (Map.Entry<N, Integer> entry : nodes.entrySet()) {
            double newScore = getWeightedScore(key, entry.getKey(), entry.getValue());
            if (newScore > highestScore) {
                champion = entry.getKey();
                highestScore = newScore;
            }
        }
        return Optional.ofNullable(champion);
    }

    @Override
    public Set<N> getNodes(String key, int size) {
        validator.validateGetNodes(key, size, nodes.size());

        Set<Pair<N, Double>> sortedSet = new TreeSet<>(Collections.reverseOrder(Comparator.comparingDouble(Pair::getLast)));

        for (Map.Entry<N, Integer> entry : nodes.entrySet()) {
            sortedSet.add(new Pair<>(entry.getKey(), getWeightedScore(key, entry.getKey(), entry.getValue())));
        }

        return sortedSet.stream().limit(size).map(pair -> pair.getFirst()).collect(Collectors.toSet());
    }

    @Override
    public RendezvousHash<N> addNode(N node) {
        validator.validateAddNode(node);
        return addWeightedNode(node, 1);
    }

    @Override
    public RendezvousHash<N> addWeightedNode(N node, int weight) {
        validator.validateAddWeightedNode(node, weight);

        Integer oldWeight = nodes.get(node);
        if (oldWeight != null && oldWeight == 1) {
            return this;
        }
        Map<N, Integer> newNodes = new HashMap<>(nodes);
        newNodes.put(node, 1);
        return new RendezvousHash<>(newNodes);
    }

    @Override
    public RendezvousHash<N> updateWeightedNode(N node, int weight) {
        validator.validateUpdateWeightedNode(node, weight);
        return addWeightedNode(node, weight);
    }

    @Override
    public RendezvousHash<N> removeNode(N node) {
        validator.validateRemoveNode(node);
        if (!nodes.containsKey(node)) {
            return this;
        }
        Map<N, Integer> newNodes = new HashMap<>();
        newNodes.putAll(nodes);
        newNodes.remove(node);
        return new RendezvousHash<>(newNodes);
    }

    private double getWeightedScore(String keyString, N node, int weight) {
        HashKey key = hashUtil.genKey(node.toString() + keyString);
        double score = 1.0 / -Math.log(key.toDouble());
        return weight * score;
    }

}
