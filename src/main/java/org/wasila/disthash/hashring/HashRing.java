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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code HashRing} class implements consistent hashing algorithm.
 *
 * Implementation is based and compatible with serialx/hashring project: {@url https://github.com/serialx/hashring}
 *
 */
public class HashRing implements DistributedHash {
    private final Map<HashKey,String> ring;
    private final List<HashKey> sortedKeys;
    private final List<String> nodes;
    private final Map<String,Integer> weights;

    private HashRing() {
        ring = new HashMap<>();
        sortedKeys = new ArrayList<>();
        nodes = new ArrayList<>();
        weights = new HashMap<>();
    }

    /**
     * Constructs instance with given nodes list. All nodes have default weight of 1.
     *
     * @param nodes List of nodes
     */
    public HashRing(String... nodes) {
        this();
        this.nodes.addAll(Arrays.asList(nodes));
        generateCircle();
    }

    /**
     * Constructs instance with given nodes list. All nodes can have arbitrary weight assigned to it.
     *
     * @param weights map where nodes are assigned to keys and weights to the corresponding values.
     */
    public HashRing(Map<String,Integer> weights) {
        this();
        this.nodes.addAll(weights.keySet());
        this.weights.putAll(weights);
        generateCircle();
    }

    private HashRing(List<String> nodes, Map<String,Integer> weights) {
        this();
        this.nodes.addAll(nodes);
        this.weights.putAll(weights);
        generateCircle();
    }

    /**
     * Returns new instance of {@code HashRing} with weights updated to the new values.
     *
     * @param newWeights Weights of nodes to be updated. All missing values means that weight
     *                   should be copied to the new instance unchanged.
     * @return           New {@code HashRing} instance
     */
    @Deprecated
    public HashRing updateWeights(Map<String,Integer> newWeights) {
        HashRing hring = this;
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
            hring = new HashRing(newWeights);
        }

        return hring;
    }

    /**
     * Returns node for given key.
     *
     * @param stringKey Any string value
     * @return          Node assigned to the key given as an argument
     */
    @Override
    public Optional<String> getNode(String stringKey) {
        Optional<Integer> nodePosition = getNodePos(stringKey);
        return nodePosition.map((position) -> ring.get(sortedKeys.get(position)));
    }

    /**
     * Returns array of nodes for given key.
     *
     * @param stringKey Any string value
     * @param size      Specifies how many nodes are expected to be returned
     * @return          Nodes assigned to the key given as an argument
     */
    @Override
    public Optional<String[]> getNodes(String stringKey, int size) {
        Optional<Integer> pos = getNodePos(stringKey);
        if (!pos.isPresent()) {
            return Optional.empty();
        }

        if (size > nodes.size()) {
            return Optional.empty();
        }

        Set<String> returnedValues = new HashSet<>();
        List<String> resultSlice = new ArrayList<>();
        for (int i = pos.get(); i < pos.get() + sortedKeys.size(); i++) {
            HashKey key = sortedKeys.get(i % sortedKeys.size());
            String val = ring.get(key);

            if (!returnedValues.contains(val)) {
                returnedValues.add(val);
                resultSlice.add(val);
            }
            if (resultSlice.size() == size) {
                break;
            }
        }
        if (resultSlice.size() == size) {
            return Optional.of(resultSlice.toArray(new String[0]));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns new instance of {@code HashRing} updated with new node. Node has weight 1 assigned
     * by default so it is equivalent to call addWeightedNode(nodeName, 1) directly
     *
     * @param nodeName   Name of node to be added
     * @return           New {@code HashRing} instance
     */
    @Override
    public HashRing addNode(String nodeName) {
        return addWeightedNode(nodeName, 1);
    }

    /**
     * Returns new instance of {@code HashRing} updated with new node of given weight.
     *
     * @param nodeName   Name of node to be added
     * @param weight     Weight of node
     * @return           New {@code HashRing} instance
     */
    @Override
    public HashRing addWeightedNode(String nodeName, int weight) {
        if (weight <= 0) {
            return this;
        }

        if (nodes.contains(nodeName)) {
            return this;
        }

        Map<String,Integer> newWeights = new HashMap<>(this.weights);
        List<String> newNodes = new ArrayList<>(this.nodes);

        newWeights.put(nodeName, weight);
        newNodes.add(nodeName);

        HashRing newhash = new HashRing(newNodes, newWeights);
        return newhash;
    }

    /**
     * Returns new instance of {@code HashRing} updated with node and new weight of that node.
     *
     * @param nodeName   Name of node
     * @param weight     Weight of node
     * @return           New {@code HashRing} instance
     */
    @Override
    public HashRing updateWeightedNode(String nodeName, int weight) {
        if (weight <= 0) {
            return this;
        }

        if (weights.get(nodeName) != null && weights.get(nodeName) == weight) {
            return this;
        }

        Map<String,Integer> newWeights = new HashMap<>(this.weights);

        newWeights.put(nodeName, weight);

        HashRing newhash = new HashRing(nodes, newWeights);
        return newhash;
    }

    /**
     * Returns new instance of {@code HashRing} with given node removed.
     *
     * @param nodeName  Name of node
     * @return          New {@code HashRing} instance
     */
    @Override
    public HashRing removeNode(String nodeName) {
        List<String> newNodes = new ArrayList<>(nodes);
        newNodes.remove(nodeName);

        if (nodes.size() == newNodes.size()) {
            return this;
        }

        Map<String,Integer> newWeights = new HashMap<>(weights);
        newWeights.remove(nodeName);

        HashRing newHashRing = new HashRing(newNodes, newWeights);

        return newHashRing;
    }

    private void generateCircle() {
        int totalWeight = nodes.stream().mapToInt(value -> weights.getOrDefault(value, 1)).sum();

        int totalNodes = nodes.size();

        for (String node : nodes) {
            int weight = weights.getOrDefault(node, 1);
            int factor = (int)(Math.floor((40.0d * totalNodes * weight) / totalWeight));

            for (int j=0; j<factor; j++) {
                String nodeKey = node + "-" + j;
                byte[] bKey = hashDigest(nodeKey);
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
        if (ring.isEmpty()) {
            return Optional.empty();
        }

        HashKey key = genKey(stringKey);

        int pos = Collections.binarySearch(sortedKeys, key);

        if (pos>=0) {
            return Optional.of(pos);
        } else {
            return Optional.of(-(pos+1));
        }
    }

    private HashKey genKey(String key) {
        byte[] bKey = hashDigest(key);
        return HashKey.hashVal(bKey);
    }

    private byte[] hashDigest(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(key.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

}

