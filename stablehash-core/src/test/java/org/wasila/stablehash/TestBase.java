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

import org.junit.Assert;

import java.util.Optional;

public class TestBase {

    protected StableHash<String> hash;

    protected void expectNode(String key, String expectedNode) {
        Optional<String> node = hash.getNode(key);
        Assert.assertEquals(expectedNode, node.get());
    }

    protected void expectNodes(String key, String... expectedNodes) {
        String[] nodes = hash.getNodes(key, expectedNodes.length).toArray(new String[0]);
        Assert.assertArrayEquals(expectedNodes, nodes);
    }

}
