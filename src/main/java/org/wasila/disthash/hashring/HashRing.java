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

public class HashRing {
    private Map<HashKey,String> ring = new HashMap<>();
    private List<HashKey> sortedKeys = new ArrayList<>();
    private List<String> nodes = new ArrayList<>();
    private Map<String,Integer> weights = new HashMap<>();

    public HashRing(String... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        generateCircle();
    }

    public HashRing(Map<String,Integer> weights) {
        this.nodes = new ArrayList<>(weights.keySet());
        this.weights = weights;
        generateCircle();
    }

    private HashRing(List<String> nodes, Map<String,Integer> weights) {
        this.nodes = nodes;
        this.weights = weights;
        generateCircle();
    }

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

    public Optional<String> getNode(String stringKey) {
        Optional<Integer> nodePosition = getNodePos(stringKey);
        return nodePosition.map((position) -> ring.get(sortedKeys.get(position)));
    }

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

    public HashRing addNode(String nodeName) {
        return addWeightedNode(nodeName, 1);
    }

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

