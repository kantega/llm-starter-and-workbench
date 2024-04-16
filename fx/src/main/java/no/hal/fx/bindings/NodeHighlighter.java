package no.hal.fx.bindings;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

public class NodeHighlighter {

    private String highlightStyle;

    public NodeHighlighter(String highightStyle) {
        this.highlightStyle = highightStyle;
    }

    private Map<Node, String> highlightedNodes = new HashMap<>();

    public void clear() {
        highlightedNodes.entrySet().forEach(entry -> entry.getKey().setStyle(entry.getValue()));
        highlightedNodes.clear();
    }

    public void clearHighlight(Node node) {
        if (isHighlighted(node)) {
            node.setStyle(highlightedNodes.get(node));
        }
    }

    public void highlight(Node node) {
        if (! isHighlighted(node)) {
            highlightedNodes.put(node, node.getStyle());
            node.setStyle(highlightStyle);
        }
    }

    public boolean isHighlighted(Node node) {
        return highlightedNodes.containsKey(node);
    }

    public void toggleHighlight(Node node) {
        if (isHighlighted(node)) {
            node.setStyle(highlightedNodes.get(node));
        } else {
            highlightedNodes.put(node, node.getStyle());
            node.setStyle(highlightStyle);
        }
    }
}
