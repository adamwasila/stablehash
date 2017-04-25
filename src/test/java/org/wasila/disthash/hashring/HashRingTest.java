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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HashRingTest {

    DistributedHash hashRing;

    public void expectNode(String key, String expectedNode) {
        String node = hashRing.getNode(key).get();
        Assert.assertEquals(expectedNode, node);
    }

    public void expectNodes(String key, String... expectedNodes) {
        String[] nodes = hashRing.getNodes(key, expectedNodes.length).get();
        Assert.assertArrayEquals(expectedNodes, nodes);
    }

    public void expectNodesABC() {
        // Python hash_ring module test case
        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "c");
        expectNode("test5", "a");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");
    }

    public void expectNodesABCD() {
        expectNodesABC();
    }

    @Test
    public void expectNodeRangesABC() {
        hashRing = DistributedHash.newConsistentHash("a", "b", "c");

        expectNodes("test", new String[] {"a", "b"});
        expectNodes("test", new String[] {"a", "b"});
        expectNodes("test1", new String[] {"b", "c"});
        expectNodes("test2", new String[] {"b", "a"});
        expectNodes("test3", new String[] {"c", "a"});
        expectNodes("test4", new String[] {"c", "b"});
        expectNodes("test5", new String[] {"a", "c"});
        expectNodes("aaaa", new String[] {"b", "a"});
        expectNodes("bbbb", new String[] {"a", "b"});
    }

    @Test
    public void expectNodeRangesABCD() {
        hashRing = DistributedHash.newConsistentHash("a", "b", "c");
        hashRing = hashRing.addNode("d");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "c");
        expectNode("test5", "a");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

        hashRing = hashRing.addNode("e");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "c");
        expectNode("test5", "a");
        expectNode("aaaa", "b");
        expectNode("bbbb", "e"); // Migrated to e from a

        expectNodes("test", new String[] {"a", "b"});

        hashRing = hashRing.addNode("f");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "f"); // Migrated to f from b
        expectNode("test3", "f"); // Migrated to f from c
        expectNode("test4", "c");
        expectNode("test5", "f"); // Migrated to f from a
        expectNode("aaaa", "b");
        expectNode("bbbb", "e");

        expectNodes("test", new String[] {"a", "b"});
    }

    @Test
    public void testDuplicateNodes() {
        hashRing = DistributedHash.newConsistentHash("a", "a", "a", "a", "b");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "a");
        expectNode("test4", "b");
        expectNode("test5", "a");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");
    }

    @Test
    public void testAddWeightedNode() {
        hashRing = DistributedHash.newConsistentHash("a", "c");
        hashRing = hashRing.addWeightedNode("b", 0);
        hashRing = hashRing.addWeightedNode("b", 2);
        hashRing = hashRing.addWeightedNode("b", 2);

        expectNode("test", "b");
        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "b");
        expectNode("test5", "b");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

        expectNodes("test", new String[] {"b", "a"});
    }

    @Test
    public void TestUpdateWeightedNode() {
        hashRing = DistributedHash.newConsistentHash("a", "c");
        hashRing = hashRing.addWeightedNode("b", 1);
        hashRing = hashRing.updateWeightedNode("b", 2);
        hashRing = hashRing.updateWeightedNode("b", 2);
        hashRing = hashRing.updateWeightedNode("b", 0);
        hashRing = hashRing.updateWeightedNode("d", 2);

        expectNode("test", "b");
        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "b");
        expectNode("test5", "b");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

        expectNodes("test", "b", "a");
    }

    @Test
    public void TestRemoveAddNode() {
        hashRing = DistributedHash.newConsistentHash("a", "b", "c");

        expectNodesABC();
        expectNodeRangesABC();

        hashRing = hashRing.removeNode("b");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "c"); // Migrated to c from b
        expectNode("test2", "a"); // Migrated to a from b
        expectNode("test3", "c");
        expectNode("test4", "c");
        expectNode("test5", "a");
        expectNode("aaaa", "a"); // Migrated to a from b
        expectNode("bbbb", "a");

        expectNodes("test", "a", "c");
        expectNodes("test", "a", "c");
        expectNodes("test1", "c", "a");
        expectNodes("test2", "a", "c");
        expectNodes("test3", "c", "a");
        expectNodes("test4", "c", "a");
        expectNodes("test5", "a", "c");
        expectNodes("aaaa", "a", "c");
        expectNodes("bbbb", "a", "c");

        hashRing = hashRing.addNode("b");

        expectNodesABC();
        expectNodeRangesABC();
    }

    @Test
    public void TestRemoveAddWeightedNode() {
        Map<String,Integer> weights = new HashMap<>();
        weights.put("a", 1);
        weights.put("b", 2);
        weights.put("c", 1);

        hashRing = DistributedHash.newConsistentHash(weights);

        expectNode("test", "b");
        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "b");
        expectNode("test5", "b");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

        expectNodes("test", "b", "a");
        expectNodes("test", "b", "a");
        expectNodes("test1", "b", "c");
        expectNodes("test2", "b", "a");
        expectNodes("test3", "c", "b");
        expectNodes("test4", "b", "a");
        expectNodes("test5", "b", "a");
        expectNodes("aaaa", "b", "a");
        expectNodes("bbbb", "a", "b");

        hashRing = hashRing.removeNode("c");

        expectNode("test", "b");
        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "b"); // Migrated to b from c
        expectNode("test4", "b");
        expectNode("test5", "b");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

        expectNodes("test", "b", "a");
        expectNodes("test", "b", "a");
        expectNodes("test1", "b", "a");
        expectNodes("test2", "b", "a");
        expectNodes("test3", "b", "a");
        expectNodes("test4", "b", "a");
        expectNodes("test5", "b", "a");
        expectNodes("aaaa", "b", "a");
        expectNodes("bbbb", "a", "b");
    }

    @Test
    public void TestAddRemoveNode() {
        hashRing = DistributedHash.newConsistentHash("a", "b", "c");
        hashRing = hashRing.addNode("d");

        // Somehow adding d does not load balance these keys...
        expectNodesABCD();

        expectNodes("test", "a", "b");
        expectNodes("test1", "b", "d");
        expectNodes("test2", "b", "d");
        expectNodes("test3", "c", "d");
        expectNodes("test4", "c", "b");
        expectNodes("test5", "a", "d");
        expectNodes("aaaa", "b", "a");
        expectNodes("bbbb", "a", "b");

        hashRing = hashRing.addNode("e");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "b");
        expectNode("test3", "c");
        expectNode("test4", "c");
        expectNode("test5", "a");
        expectNode("aaaa", "b");
        expectNode("bbbb", "e"); // Migrated to e from a

        expectNodes("test", "a", "b");
        expectNodes("test", "a", "b");
        expectNodes("test1", "b", "d");
        expectNodes("test2", "b", "d");
        expectNodes("test3", "c", "e");
        expectNodes("test4", "c", "b");
        expectNodes("test5", "a", "e");
        expectNodes("aaaa", "b", "e");
        expectNodes("bbbb", "e", "a");

        hashRing = hashRing.addNode("f");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "f"); // Migrated to f from b
        expectNode("test3", "f"); // Migrated to f from c
        expectNode("test4", "c");
        expectNode("test5", "f"); // Migrated to f from a
        expectNode("aaaa", "b");
        expectNode("bbbb", "e");

        expectNodes("test", "a", "b");
        expectNodes("test", "a", "b");
        expectNodes("test1", "b", "d");
        expectNodes("test2", "f", "b");
        expectNodes("test3", "f", "c");
        expectNodes("test4", "c", "b");
        expectNodes("test5", "f", "a");
        expectNodes("aaaa", "b", "e");
        expectNodes("bbbb", "e", "f");

        hashRing = hashRing.removeNode("e");

        expectNode("test", "a");
        expectNode("test", "a");
        expectNode("test1", "b");
        expectNode("test2", "f");
        expectNode("test3", "f");
        expectNode("test4", "c");
        expectNode("test5", "f");
        expectNode("aaaa", "b");
        expectNode("bbbb", "f"); // Migrated to f from e

        expectNodes("test", "a", "b");
        expectNodes("test", "a", "b");
        expectNodes("test1", "b", "d");
        expectNodes("test2", "f", "b");
        expectNodes("test3", "f", "c");
        expectNodes("test4", "c", "b");
        expectNodes("test5", "f", "a");
        expectNodes("aaaa", "b", "a");
        expectNodes("bbbb", "f", "a");

        hashRing = hashRing.removeNode("f");

        expectNodesABCD();

        expectNodes("test", "a", "b");
        expectNodes("test", "a", "b");
        expectNodes("test1", "b", "d");
        expectNodes("test2", "b", "d");
        expectNodes("test3", "c", "d");
        expectNodes("test4", "c", "b");
        expectNodes("test5", "a", "d");
        expectNodes("aaaa", "b", "a");
        expectNodes("bbbb", "a", "b");

        hashRing = hashRing.removeNode("d");

        expectNodesABC();
        expectNodeRangesABC();
    }

}
