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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RendezvousHashTest {

    DistributedHash<String> hashRing;

    @Test
    public void testSingleNodeABC() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c"});
        hashRing = new RendezvousHash<>(nodes);

        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

    }

    @Test
    public void testAddNode() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c"});
        hashRing = new RendezvousHash<>(nodes);
        hashRing = hashRing.addNode("d");

        expectNode("test", "b");
        expectNode("test1", "d"); // *
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d"); // *
        expectNode("bbbb", "d"); // *

        hashRing = hashRing.removeNode("d");

        expectNode("test", "b");
        expectNode("test1", "b");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "b");
        expectNode("bbbb", "a");

    }

    @Test
    public void testRemoveNode() {
        List<String> nodes = Arrays.asList(new String[] {"a", "b", "c", "d"});
        hashRing = new RendezvousHash<>(nodes);

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("test2", "a");
        expectNode("test3", "a");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hashRing = hashRing.removeNode("a");

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("test4", "c");
        expectNode("test5", "c");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hashRing = hashRing.removeNode("c");

        expectNode("test", "b");
        expectNode("test1", "d");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

        hashRing = hashRing.removeNode("b");

        expectNode("test1", "d");
        expectNode("aaaa", "d");
        expectNode("bbbb", "d");

    }

    private void expectNode(String key, String expectedNode) {
        Optional<String> node = hashRing.getNode(key);
        Assert.assertEquals(expectedNode, node.get());
    }

}
