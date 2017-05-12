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
package org.wasila.disthash.examples;

import org.wasila.disthash.hashring.DistributedHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Examples taken directly from original implementation
 */
public class OriginalImplementationExamples {

    public static void example1() {
        List<String> memcacheServers = new ArrayList<>();
        memcacheServers.add("192.168.0.246:11212");
        memcacheServers.add("192.168.0.247:11212");
        memcacheServers.add("192.168.0.249:11212");

        DistributedHash<String> hring = DistributedHash.newConsistentHash(memcacheServers);

        String node = hring.getNode("my_key").get();

        System.out.println("Selected node: " + node);
    }

    public static void example2() {
        String[] serversInRing = new String[] {
                "192.168.0.246:11212",
                "192.168.0.247:11212",
                "192.168.0.248:11212",
                "192.168.0.249:11212",
                "192.168.0.250:11212",
                "192.168.0.251:11212",
                "192.168.0.252:11212"
        };

        int replicaCount = 3;

        DistributedHash<String> ring = DistributedHash.newConsistentHash(Arrays.asList(serversInRing));

        Set<String> server = ring.getNodes("my_key", replicaCount);
        System.out.println("Selected nodes: " + server);
    }

    public static void example3() {
        Map<String,Integer> weights = new HashMap<>();
        weights.put("192.168.0.246:11212", 1);
        weights.put("192.168.0.247:11212", 2);
        weights.put("192.168.0.249:11212", 1);

        DistributedHash<String> hring = DistributedHash.newConsistentHash(weights);

        String node = hring.getNode("my_key").get();

        System.out.println("Selected node: " + node);
    }

    public static void example4() {
        List<String> memcacheServers = new ArrayList<>();
        memcacheServers.add("192.168.0.246:11212");
        memcacheServers.add("192.168.0.247:11212");
        memcacheServers.add("192.168.0.249:11212");

        DistributedHash<String> hring = DistributedHash.newConsistentHash(memcacheServers);

        hring = hring.removeNode("192.168.0.246:11212");
        hring = hring.addNode("192.168.0.250:11212");

        String node = hring.getNode("my_key").get();

        System.out.println("Selected node: " + node);
    }

    public static void main(String[] args) {
        example1();
        example2();
        example3();
        example4();
    }

}
