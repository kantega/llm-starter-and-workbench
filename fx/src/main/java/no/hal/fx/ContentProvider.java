package no.hal.fx;

import java.util.List;
import java.util.function.Supplier;

import javafx.scene.Node;
import javafx.scene.Parent;

@FunctionalInterface
public interface ContentProvider<N> {
    N getContent();

    public static <N> ContentProvider<N> of(N node) {
        return () -> node;
    }

    public static <N> ContentProvider<N> of(Supplier<N> contentSupplier) {
        return () -> contentSupplier.get();
    }

    public interface Child extends ContentProvider<Node> {

        public static Child of(Node node) {
            return () -> node;
        }
    }
    
    public interface Container extends ContentProvider<Parent> {

        public static Container of(Parent parent) {
            return () -> parent;
        }
    }

    public interface Children extends ContentProvider<List<Node>> {

        public static Children of(List<Node> nodes) {
            return () -> nodes;
        }
    }
}
