package org.wasila.stablehash.internal;

public class InputValidator<N> {

    public void validateGetNode(String stringKey) {
        validateKey(stringKey);
    }

    public void validateGetNodes(String key, int size, int nodesSetSize) {
        validateKey(key);
        validateSize(size, nodesSetSize);
    }

    public void validateAddNode(N nodeName) {
        validateNode(nodeName);
    }

    public void validateAddWeightedNode(N nodeName, int weight) {
        validateNode(nodeName);
        validateWeight(weight);
    }

    public void validateUpdateWeightedNode(N nodeName, int weight) {
        validateNode(nodeName);
        validateWeight(weight);
    }

    public void validateRemoveNode(N nodeName) {
        validateNode(nodeName);
    }

    private void validateNode(N node) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
    }

    private void validateKey(String key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
    }

    private void validateSize(int size, int nodesSetSize) {
        if (size < 1 || size > nodesSetSize) {
            throw new IllegalArgumentException("size outside of expected range (0," + nodesSetSize +"): " + size);
        }
    }

    private void validateWeight(int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Invalid weight value: " + weight + "; should be > 0.");
        }
    }
}
