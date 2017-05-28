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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private final Map<HashKey,N> ring;
    private final List<HashKey> sortedKeys;
    private final List<N> nodes;
    private final Map<N,Integer> weights;

    private final HashUtil hashUtil;

    private ConsistentHash() {
        hashUtil = new HashUtil();
        ring = new HashMap<>();
        sortedKeys = new ArrayList<>();
        nodes = new ArrayList<>();
        weights = new HashMap<>();
    }

    /**
     * Constructs instance with given nodes list. All nodes have default weight of 1.
     *
     * @param nodes Collection of nodes
     */
    public ConsistentHash(Collection<N> nodes) {
        this();
        this.nodes.addAll(nodes);
        generateCircle();
    }

    /**
     * Constructs instance with given nodes list. All nodes can have arbitrary weight assigned to it.
     *
     * @param weights map where nodes are assigned to keys and weights to the corresponding values.
     */
    public ConsistentHash(Map<N,Integer> weights) {
        this();
        this.nodes.addAll(weights.keySet());
        this.weights.putAll(weights);
        generateCircle();
    }

    private ConsistentHash(List<N> nodes, Map<N,Integer> weights) {
        this();
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
            hring = new ConsistentHash<N>(newWeights);
        }

        return hring;
    }

    /**
     * Returns node for given key.
     *
     * @param stringKey Any string value
     * @return          Node assigned to the key given as an argument
     * @throws NullPointerException     if key value is null
     */
    @Override
    public Optional<N> getNode(String stringKey) {
        Optional<Integer> nodePosition = getNodePos(stringKey);
        return nodePosition.map((position) -> ring.get(sortedKeys.get(position)));
    }

    /**
     * Returns array of nodes for given key.
     *
     * @param stringKey Any string value
     * @param size      Specifies how many nodes are expected to be returned
     * @return          Nodes assigned to the key given as an argument
     * @throws NullPointerException     if key value is null
     * @throws IllegalArgumentException if size is 0 or less or greater than total number of nodes
     */
    @Override
    public Set<N> getNodes(String stringKey, int size) {
        if (stringKey == null) {
            throw new NullPointerException("nodeName must not be null");
        }
        if (size < 1 || size > nodes.size()) {
            throw new IllegalArgumentException("size outside of expected range (0," + nodes.size() +"): " + size);
        }

        Optional<Integer> pos = getNodePos(stringKey);
        if (!pos.isPresent()) {
            return Collections.emptySet();
        }

        Set<N> returnedValues = new HashSet<>();
        Set<N> resultSlice = new LinkedHashSet<>();
        for (int i = pos.get(); i < pos.get() + sortedKeys.size(); i++) {
            HashKey key = sortedKeys.get(i % sortedKeys.size());
            N val = ring.get(key);

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

    /**
     * Returns new instance of {@code ConsistentHash} updated with new node. Node has weight 1 assigned
     * by default so it is equivalent to call addWeightedNode(nodeName, 1) directly
     *
     * @param nodeName   Name of node to be added
     * @return           New {@code ConsistentHash} instance
     * @throws NullPointerException     if {@code nodeName} is null
     */
    @Override
    public ConsistentHash addNode(N nodeName) {
        return addWeightedNode(nodeName, 1);
    }

    /**
     * Returns new instance of {@code ConsistentHash} updated with new node of given weight.
     *
     * @param nodeName   Name of node to be added
     * @param weight     Weight of node
     * @return           New {@code ConsistentHash} instance
     * @throws NullPointerException     if {@code nodeName} is null
     * @throws IllegalArgumentException if weight is 0 or less
     */
    @Override
    public ConsistentHash addWeightedNode(N nodeName, int weight) {
        if (nodeName == null) {
            throw new NullPointerException("nodeName must not be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Invalid weight value: " + weight);
        }

        if (nodes.contains(nodeName)) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(this.weights);
        List<N> newNodes = new ArrayList<>(this.nodes);

        newWeights.put(nodeName, weight);
        newNodes.add(nodeName);

        ConsistentHash newhash = new ConsistentHash(newNodes, newWeights);
        return newhash;
    }

    /**
     * Returns new instance of {@code ConsistentHash} updated with node and new weight of that node.
     *
     * @param nodeName   Name of node
     * @param weight     Weight of node
     * @return           New {@code ConsistentHash} instance
     * @throws NullPointerException     if {@code nodeName} is null
     * @throws IllegalArgumentException if weight is 0 or less
     */
    @Override
    public ConsistentHash updateWeightedNode(N nodeName, int weight) {
        if (nodeName == null) {
            throw new NullPointerException("nodeName must not be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Invalid weight value: " + weight);
        }

        if (weights.get(nodeName) != null && weights.get(nodeName) == weight) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(this.weights);

        newWeights.put(nodeName, weight);

        ConsistentHash newhash = new ConsistentHash(nodes, newWeights);
        return newhash;
    }

    /**
     * Returns new instance of {@code ConsistentHash} with given node removed.
     *
     * @param nodeName  Name of node
     * @return          New {@code ConsistentHash} instance
     * @throws NullPointerException     if {@code nodeName} is null
     */
    @Override
    public ConsistentHash removeNode(N nodeName) {
        if (nodeName == null) {
            throw new NullPointerException("nodeName must not be null");
        }

        List<N> newNodes = new ArrayList<>(nodes);
        newNodes.remove(nodeName);

        if (nodes.size() == newNodes.size()) {
            return this;
        }

        Map<N,Integer> newWeights = new HashMap<>(weights);
        newWeights.remove(nodeName);

        return new ConsistentHash(newNodes, newWeights);
    }

    private void generateCircle() {
        int totalWeight = nodes.stream().mapToInt(value -> weights.getOrDefault(value, 1)).sum();

        int totalNodes = nodes.size();

        for (N node : nodes) {
            int weight = weights.getOrDefault(node, 1);
            int factor = (int)(Math.floor((40.0d * totalNodes * weight) / totalWeight));

            for (int j=0; j<factor; j++) {
                String nodeKey = node.toString() + "-" + j;
                byte[] bKey = hashUtil.hashDigest(nodeKey);
                for (int i=0; i<3; i++) {
                    HashKey key = HashKey.hashVal(Arrays.copyOfRange(bKey, i*4, i*4+4));
                    ring.put(key, node);
                    sortedKeys.add(key);
                }
            }
        }
        Collections.sort(sortedKeys);
    }

    private Optional<Integer> getNodePos(String stringKey) {
        if (stringKey == null) {
            throw new NullPointerException("specified key must not be null");
        }
        if (ring.isEmpty()) {
            return Optional.empty();
        }

        HashKey key = hashUtil.genKey(stringKey);

        int pos = Collections.binarySearch(sortedKeys, key);
        pos = (pos>=0) ? pos : -(pos+1);
        pos = pos % sortedKeys.size();

        return Optional.of(pos);
    }

}

