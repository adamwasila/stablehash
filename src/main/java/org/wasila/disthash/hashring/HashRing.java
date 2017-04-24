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
//    package hashring
//
//    import (
//            "crypto/md5"
//            "fmt"
//            "math"
//            "sort"
//            )
//
//    type HashKey uint32
//    type HashKeyOrder []HashKey
//
//    func (h HashKeyOrder) Len() int           { return len(h) }
//    func (h HashKeyOrder) Swap(i, j int)      { h[i], h[j] = h[j], h[i] }
//    func (h HashKeyOrder) Less(i, j int) bool { return h[i] < h[j] }
//
//    type HashRing struct {
//        ring       map[HashKey]string
//        sortedKeys []HashKey
//        nodes      []string
//        weights    map[string]int
//    }

    private Map<HashKey,String> ring = new HashMap<>();
    private List<HashKey> sortedKeys = new ArrayList<>();
    private List<String> nodes = new ArrayList<>();
    private Map<String,Integer> weights = new HashMap<>();

//    func New(nodes []string) *HashRing {
//        hashRing := &HashRing{
//            ring:       make(map[HashKey]string),
//                    sortedKeys: make([]HashKey, 0),
//            nodes:      nodes,
//                    weights:    make(map[string]int),
//        }
//        hashRing.generateCircle()
//        return hashRing
//    }

    public HashRing(String... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        generateCircle();
    }

//    func NewWithWeights(weights map[string]int) *HashRing {
//        nodes := make([]string, 0, len(weights))
//        for node, _ := range weights {
//            nodes = append(nodes, node)
//        }
//        hashRing := &HashRing{
//            ring:       make(map[HashKey]string),
//                    sortedKeys: make([]HashKey, 0),
//            nodes:      nodes,
//                    weights:    weights,
//        }
//        hashRing.generateCircle()
//        return hashRing
//    }

    public HashRing(Map<String,Integer> weights) {
        this.nodes = new ArrayList<>(weights.keySet());
        this.weights = weights;
        generateCircle();
    }

    public HashRing(List<String> nodes, Map<String,Integer> weights) {
        this.nodes = nodes;
        this.weights = weights;
        generateCircle();
    }

//    func (h *HashRing) UpdateWithWeights(weights map[string]int) {
//        nodesChgFlg := false
//        if len(weights) != len(h.weights) {
//            nodesChgFlg = true
//        } else {
//            for node, newWeight := range weights {
//                oldWeight, ok := h.weights[node]
//                if !ok || oldWeight != newWeight {
//                    nodesChgFlg = true
//                    break
//                }
//            }
//        }
//
//        if nodesChgFlg {
//            newhring := NewWithWeights(weights)
//            h.weights = newhring.weights
//            h.nodes = newhring.nodes
//            h.ring = newhring.ring
//            h.sortedKeys = newhring.sortedKeys
//        }
//    }

    public void updateWeights(Map<String,Integer> newWeights) throws NoSuchAlgorithmException {
        boolean nodesChgFlg = false;

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
            HashRing hring = new HashRing(newWeights);
            this.weights = hring.weights;
            this.nodes = hring.nodes;
            this.ring = hring.ring;
            this.sortedKeys = hring.sortedKeys;
        }
    }

//
//    func (h *HashRing) generateCircle() {
//        totalWeight := 0
//        for _, node := range h.nodes {
//            if weight, ok := h.weights[node]; ok {
//                totalWeight += weight
//            } else {
//                totalWeight += 1
//            }
//        }
//
//        for _, node := range h.nodes {
//            weight := 1
//
//            if _, ok := h.weights[node]; ok {
//                weight = h.weights[node]
//            }
//
//            factor := math.Floor(float64(40*len(h.nodes)*weight) / float64(totalWeight))
//
//            for j := 0; j < int(factor); j++ {
//                nodeKey := fmt.Sprintf("%s-%d", node, j)
//                bKey := hashDigest(nodeKey)
//
//                for i := 0; i < 3; i++ {
//                    key := hashVal(bKey[i*4 : i*4+4])
//                    h.ring[key] = node
//                    h.sortedKeys = append(h.sortedKeys, key)
//                }
//            }
//        }
//
//        sort.Sort(HashKeyOrder(h.sortedKeys))
//    }

    public void generateCircle() {
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

//
//    func (h *HashRing) GetNode(stringKey string) (node string, ok bool) {
//        pos, ok := h.GetNodePos(stringKey)
//        if !ok {
//            return "", false
//        }
//        return h.ring[h.sortedKeys[pos]], true
//    }

    public Optional<String> getNode(String stringKey) {
        Optional<Integer> nodePosition = getNodePos(stringKey);
        return nodePosition.map((position) -> ring.get(sortedKeys.get(position)));
    }

//
//    func (h *HashRing) GetNodePos(stringKey string) (pos int, ok bool) {
//        if len(h.ring) == 0 {
//            return 0, false
//        }
//
//        key := h.GenKey(stringKey)
//
//        nodes := h.sortedKeys
//        pos = sort.Search(len(nodes), func(i int) bool { return nodes[i] > key })
//
//        if pos == len(nodes) {
//            // Wrap the search, should return first node
//            return 0, true
//        } else {
//            return pos, true
//        }
//    }

    public Optional<Integer> getNodePos(String stringKey) {
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

//
//    func (h *HashRing) GenKey(key string) HashKey {
//        bKey := hashDigest(key)
//        return hashVal(bKey[0:4])
//    }

    public HashKey genKey(String key) {
        byte[] bKey = hashDigest(key);
        return HashKey.hashVal(bKey);
    }

//    func (h *HashRing) GetNodes(stringKey string, size int) (nodes []string, ok bool) {
//        pos, ok := h.GetNodePos(stringKey)
//        if !ok {
//            return []string{}, false
//        }
//
//        if size > len(h.nodes) {
//            return []string{}, false
//        }
//
//        returnedValues := make(map[string]bool, size)
//        //mergedSortedKeys := append(h.sortedKeys[pos:], h.sortedKeys[:pos]...)
//        resultSlice := make([]string, 0, size)
//
//        for i := pos; i < pos+len(h.sortedKeys); i++ {
//            key := h.sortedKeys[i%len(h.sortedKeys)]
//            val := h.ring[key]
//            if !returnedValues[val] {
//                returnedValues[val] = true
//                resultSlice = append(resultSlice, val)
//            }
//            if len(returnedValues) == size {
//                break
//            }
//        }
//
//        return resultSlice, len(resultSlice) == size
//    }

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

//    func (h *HashRing) AddNode(node string) *HashRing {
//        return h.AddWeightedNode(node, 1)
//    }

    public HashRing addNode(String nodeName) {
        return addWeightedNode(nodeName, 1);
    }

//    func (h *HashRing) AddWeightedNode(node string, weight int) *HashRing {
//        if weight <= 0 {
//            return h
//        }
//
//        for _, eNode := range h.nodes {
//            if eNode == node {
//                return h
//            }
//        }
//
//        nodes := make([]string, len(h.nodes), len(h.nodes)+1)
//        copy(nodes, h.nodes)
//        nodes = append(nodes, node)
//
//        weights := make(map[string]int)
//        for eNode, eWeight := range h.weights {
//            weights[eNode] = eWeight
//        }
//        weights[node] = weight
//
//        hashRing := &HashRing{
//            ring:       make(map[HashKey]string),
//            sortedKeys: make([]HashKey, 0),
//            nodes:      nodes,
//            weights:    weights,
//        }
//        hashRing.generateCircle()
//        return hashRing
//    }

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

//    func (h *HashRing) UpdateWeightedNode(node string, weight int) *HashRing {
//        if weight <= 0 {
//            return h
//        }
//
//	/* node is not need to update for node is not existed or weight is not changed */
//        if oldWeight, ok := h.weights[node]; (!ok) || (ok && oldWeight == weight) {
//            return h
//        }
//
//        nodes := make([]string, len(h.nodes), len(h.nodes))
//        copy(nodes, h.nodes)
//
//        weights := make(map[string]int)
//        for eNode, eWeight := range h.weights {
//            weights[eNode] = eWeight
//        }
//        weights[node] = weight
//
//        hashRing := &HashRing{
//            ring:       make(map[HashKey]string),
//                    sortedKeys: make([]HashKey, 0),
//            nodes:      nodes,
//                    weights:    weights,
//        }
//        hashRing.generateCircle()
//        return hashRing
//    }

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

//    func (h *HashRing) RemoveNode(node string) *HashRing {
//        nodes := make([]string, 0)
//        for _, eNode := range h.nodes {
//            if eNode != nod   e {
//                nodes = append(nodes, eNode)
//            }
//        }
//
//	/* if node isn't exist in hashring, don't refresh hashring */
//        if len(nodes) == len(h.nodes) {
//            return h
//        }
//
//        weights := make(map[string]int)
//        for eNode, eWeight := range h.weights {
//            if eNode != node {
//                weights[eNode] = eWeight
//            }
//        }
//
//        hashRing := &HashRing{
//            ring:       make(map[HashKey]string),
//                    sortedKeys: make([]HashKey, 0),
//            nodes:      nodes,
//                    weights:    weights,
//        }
//        hashRing.generateCircle()
//        return hashRing
//    }


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

//    func hashDigest(key string) []byte {
//        m := md5.New()
//        m.Write([]byte(key))
//        return m.Sum(nil)
//    }

    private byte[] hashDigest(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(key.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

}

