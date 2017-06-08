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
import org.wasila.stablehash.StableHash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code ConsistentHash} class implements consistent hashing algorithm.
 *
 * Implementation is based and compatible with serialx/hashring project: {@url https://github.com/serialx/hashring}
 *
 */
public class ConsistentHash<N> implements StableHash<N> {

    private InputValidator<N> validator;

    private final HashFactory hashFactory;
    private final Map<AuxHashKey,N> ring;
    private final List<AuxHashKey> sortedKeys;
    private final List<N> nodes;
    private final Map<N,Integer> weights;

    private ConsistentHash(HashFactory hashFactory) {
        this.hashFactory = hashFactory;
        validator = new InputValidator<>();
        ring = new HashMap<>();
        sortedKeys = new ArrayList<>();
        nodes = new ArrayList<>();
        weights = new HashMap<>();
    }

    /**
     * Constructs instance with given nodes list. All nodes have default weight of 1.
     * Uses default auxiliary hash which is currently MD5 based hash.
     *
     * @param nodes Collection of nodes
     */
    public ConsistentHash(Collection<N> nodes) {
        this(new HashUtil(), nodes);
    }

    /**
     * Constructs instance with given nodes list. All nodes can have arbitrary weight assigned to it.
     * Uses default auxiliary hash which is currently MD5 based hash.
     *
     * @param weights map where nodes are assigned to keys and weights to the corresponding values.
     */
    public ConsistentHash(Map<N,Integer> weights) {
        this(new HashUtil(), weights);
    }

    /**
     * Constructs instance with given nodes list. All nodes have default weight of 1.
     *
     * @param hashFactory Factory of auxiliary hashes
     * @param nodes Collection of nodes
     */
    public ConsistentHash(HashFactory hashFactory, Collection<N> nodes) {
        this(hashFactory);
        this.nodes.addAll(nodes);
        generateCircle();
    }

    /**
     * Constructs instance with given nodes list. All nodes can have arbitrary weight assigned to it.
     *
     * @param hashFactory Factory of auxiliary hashes
     * @param weights map where nodes are assigned to keys and weights to the corresponding values.
     */
    public ConsistentHash(HashFactory hashFactory, Map<N,Integer> weights) {
        this(hashFactory);
        this.nodes.addAll(weights.keySet());
        this.weights.putAll(weights);
        generateCircle();
    }

    private ConsistentHash(HashFactory hashFactory, List<N> nodes, Map<N,Integer> weights) {
        this(hashFactory);
        this.nodes.addAll(nodes);
        this.weights.putAll(weights);
        generateCircle();
    }

    /**
     * Returns new instance of {@code ConsistentHash} with weights updated to the new values.
     *
     * @param newWeights Weights of nodes to be updated. All missing values means that weight
     *                   should be copied to the new instance unchanged.
     * @return           New {@code ConsistentHash} instance
     */
    @Deprecated
    public ConsistentHash updateWeights(Map<N,Integer> newWeights) {
        ConsistentHash hring = this;
        boolean nodesChgFlg;

        if (newWeights.size() != weights.size()) {
            nodesChgFlg = true;
        } else {
            nodesChgFlg = !weights.entrySet().stream().allMatch(
                    entry -> {
                        Integer weight = newWeights.get(entry.getKey());
                        return  weight != null && weight.equals(entry.getValue());
                    }
            );
        }

        if (nodesChgFlg) {
            hring = new ConsistentHash<N>(this.hashFactory, newWeights);
        }

        return hring;
    }

    @Override
    public Optional<N> getNode(String key) {
        validator.validateGetNode(key);
        Optional<Integer> nodePosition = getNodePos(key);
        return nodePosition.map((position) -> ring.get(sortedKeys.get(position)));
    }

    @Override
    public Set<N> getNodes(String key, int size) {
        validator.validateGetNodes(key, size, nodes.size());

        Optional<Integer> pos = getNodePos(key);
        if (!pos.isPresent()) {
            return Collections.emptySet();
        }

        Set<N> returnedValues = new HashSet<>();
        Set<N> resultSlice = new LinkedHashSet<>();
        for (int i = pos.get(); i < pos.get() + sortedKeys.size(); i++) {
            AuxHashKey hashKey = sortedKeys.get(i % sortedKeys.size());
            N val = ring.get(hashKey);

            if (!returnedValues.contains(val)) {
                returnedValues.add(val);
                resultSlice.add(val);
            }
            if (resultSlice.size() == size) {
                break;
            }
        }

        if (resultSlice.size() == size) {
            return resultSlice;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public ConsistentHash<N> addNode(N node) {
        validator.validateAddNode(node);
        return addWeightedNode(node, 1);
    }

    @Override
    public ConsistentHash<N> addWeightedNode(N node, int weight) {
        validator.validateAddWeightedNode(node, weight);

        if (nodes.contains(node)) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(this.weights);
        List<N> newNodes = new ArrayList<>(this.nodes);

        newWeights.put(node, weight);
        newNodes.add(node);

        return new ConsistentHash<N>(this.hashFactory, newNodes, newWeights);
    }

    @Override
    public ConsistentHash<N> updateWeightedNode(N node, int weight) {
        validator.validateUpdateWeightedNode(node, weight);

        if (weights.get(node) != null && weights.get(node) == weight) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(this.weights);

        newWeights.put(node, weight);

        ConsistentHash<N> newhash = new ConsistentHash<N>(this.hashFactory, nodes, newWeights);
        return newhash;
    }

    @Override
    public ConsistentHash<N> removeNode(N node) {
        validator.validateRemoveNode(node);

        List<N> newNodes = new ArrayList<>(nodes);
        newNodes.remove(node);

        if (nodes.size() == newNodes.size()) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(weights);
        newWeights.remove(node);

        return new ConsistentHash<N>(this.hashFactory, newNodes, newWeights);
    }

    private void generateCircle() {
        int totalWeight = nodes.stream().mapToInt(value -> weights.getOrDefault(value, 1)).sum();

        int totalNodes = nodes.size();

        for (N node : nodes) {
            int weight = weights.getOrDefault(node, 1);
            int factor = (int)(Math.floor((40.0d * totalNodes * weight) / totalWeight));

            for (int j=0; j<factor; j++) {
                String nodeKey = node.toString() + "-" + j;

                Iterator<AuxHashKey> it = hashFactory.iterator(nodeKey);
                int i =0;

                // bizzarly, original implementation took only 3 of 4 possible hash keys (md5 has 16 bytes)
                while (it.hasNext() && (i++ < 3)) {
                    AuxHashKey key = it.next();
                    ring.put(key, node);
                    sortedKeys.add(key);
                }
            }
        }
        Collections.sort(sortedKeys);
    }

    private Optional<Integer> getNodePos(String key) {
        if (key == null) {
            throw new NullPointerException("specified key must not be null");
        }
        if (ring.isEmpty()) {
            return Optional.empty();
        }

        AuxHashKey hashKey = hashFactory.iterator(key).next();

        int pos = Collections.binarySearch(sortedKeys, hashKey);
        pos = (pos>=0) ? pos : -(pos+1);
        pos = pos % sortedKeys.size();

        return Optional.of(pos);
    }

}

