package no.hal.fx.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import no.hal.fx.util.Modifiers;

public class BindingController {
    
    private final Pane bindingControllerRoot;

    public BindingController(Pane bindingControllerRoot) {
        this.bindingControllerRoot = bindingControllerRoot;
    }

    private List<BindingSource<?>> bindingSources = new ArrayList<>();
    private List<BindingTarget<?>> bindingTargets = new ArrayList<>();
    
    public void addBindingSources(BindableView bindableView) {
        this.bindingSources.addAll(bindableView.getBindingSources());
    }
    public void removeBindingSources(BindableView bindableView) {
        this.bindingSources.removeAll(bindableView.getBindingSources());
    }
    
    public void addBindingTargets(BindableView bindableView) {
        this.bindingTargets.addAll(bindableView.getBindingTargets());
    }
    public void removeBindingTargets(BindableView bindableView) {
        this.bindingTargets.removeAll(bindableView.getBindingTargets());
    }

    private Map<BindingSubscription, Path> bindingSubscriptions = new HashMap<>();

    public Optional<BindingSource<?>> findSourceForTarget(BindingTarget<?> bindingTarget) {
        return bindingSources
            // reverse, so newly added sources are tried first
            .reversed().stream()
            .filter(bindingSource -> bindingSource.sourceClass() == bindingTarget.targetClass()).findFirst();
    }

    public boolean bindToTarget(BindingTarget<?> bindingTarget) {
        var source = findSourceForTarget(bindingTarget);
        source.ifPresent(bindingSource -> bindSourceToTarget(bindingSource, bindingTarget, null));
        return source.isPresent();
    }

    public void bindToTargets(BindableView bindableView) {
        bindableView.getBindingTargets().forEach(this::bindToTarget);
    }

    public boolean bindingSourceSupportsTarget(BindingSource<?> bindingSource, BindingTarget<?> bindingTarget) {
        return bindingSource.sourceClass().isAssignableFrom(bindingTarget.targetClass());
    }

    public void bindSourceToTarget(BindingSource<?> bindingSource, BindingTarget<?> bindingTarget, Path path) {
        if (! bindingSourceSupportsTarget(bindingSource, bindingTarget)) {
            throw new IllegalArgumentException(bindingTarget.targetClass() + " is not assignable to " + bindingSource.sourceClass());
        }
        var subscription = bindingSource.sourceValue().subscribe(value -> {
            var targetProperty = ((BindingTarget<Object>) bindingTarget).targetProperty();
            targetProperty.setValue(value);
        });
        bindingSubscriptions.put(new BindingSubscription(bindingSource, bindingTarget, subscription), path);
    }

    public void removeBinding(BindingSubscription bindingSubscription) {
        Path path  = bindingSubscriptions.get(bindingSubscription);
        bindingControllerRoot.getChildren().remove(path);
        bindingSubscription.subscription().unsubscribe();
        bindingSubscriptions.remove(bindingSubscription);
    }

    public void removeBindings(Predicate<BindingSubscription> filter) {
        var subscriptions = bindingSubscriptions.keySet().stream().filter(filter).toList();
        subscriptions.forEach(this::removeBinding);
    }
    public void removeBindings(BindingSource<?> source, BindingTarget<?> target) {
        removeBindings(subscription -> (source == null || source == subscription.source()) && (target == null || target == subscription.target()));
    }
    public void removeBindings(BindingSource<?> source) {
        removeBindings(source, null);
    }
    public void removeBindings(BindingTarget<?> target) {
        removeBindings(null, target);
    }
    public void removeBindings(BindableView bindableView) {
        bindableView.getBindingSources().forEach(this::removeBindings);
        bindableView.getBindingTargets().forEach(this::removeBindings);
    }

    private Modifiers bindingGestureModifiers = Modifiers.of("ALT+META");
    
    public void setBindingGestureModifiers(Modifiers modifiers) {
        this.bindingGestureModifiers = modifiers;
    }
    public void setBindingGestureModifiers(String modifiers) {
        setBindingGestureModifiers(Modifiers.of(modifiers));
    }

    private boolean isBindingPath(Path path) {
        return bindingSubscriptions.values().contains(path);
    }

    private Optional<BindingSubscription> getBindingSubscription(Path path) {
        for (var entry : bindingSubscriptions.entrySet()) {
            if (entry.getValue() == path) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private NodeHighlighter sourceHighlighter = new NodeHighlighter("-fx-border-color: BLUE; -fx-border-width: 2");
    private NodeHighlighter targetHighlighter = new NodeHighlighter("-fx-border-color: GREEN; -fx-border-width: 2");
    private NodeHighlighter pathHighlighter = new NodeHighlighter("-fx-fill: ORANGE");

    private <T> T findParent(Node childNode, Class<T> parentClass, Predicate<T> parentTest) {
        while (childNode != null) {
            if (parentClass.isInstance(childNode) && parentTest.test((T) childNode)) {
                return (T) childNode;
            }
            childNode = childNode.getParent();
        }
        return null;
    }

    private boolean isChildOf(Node childNode, Node parentNode) {
        return findParent(childNode, Node.class, parent -> parent == parentNode) != null;
    }

    private Point2D centerOf(Bounds bounds) {
        return new Point2D((bounds.getMinX() + bounds.getMaxX()) / 2, (bounds.getMinY() + bounds.getMaxY()) / 2);
    }

    private Point2D toParent(Point2D point, Node childNode, Node parentNode) {
        while (childNode != null && childNode != parentNode) {
            point = new Point2D(point.getX() + childNode.getLayoutX(), point.getY() + childNode.getLayoutY());
            childNode = childNode.getParent();
        }
        return point;
    }

    private Path createBindingPath() {
        var path = new Path();
        path.setStrokeWidth(2);
        path.setStroke(Color.GREEN);
        path.setManaged(false);
        return path;
    }

    private void updateBindingPath(Path path, BindingSubscription bindingSubscription) {
        updateBindingPathStart(path, bindingSubscription.source().sourceNode());
        Node target = bindingSubscription.target().targetNode();
        updateBindingPathEnd(path, centerOf(target.getBoundsInLocal()), target);
    }

    private void updateBindingPathStart(Path path, Node source) {
        Point2D start = toParent(centerOf(source.getBoundsInLocal()), source, bindingControllerRoot);
        path.getElements().setAll(new MoveTo(start.getX(), start.getY()));
    }

    private double tipSize = 14;

    private void updateBindingPathEnd(Path path, Point2D point, Node target) {
        Point2D start = switch (path.getElements().getLast()) {
            case MoveTo moveTo -> new Point2D(moveTo.getX(), moveTo.getY());
            case LineTo lineTo -> new Point2D(lineTo.getX(), lineTo.getY());
            default -> null;
        };
        Point2D end = toParent(point, target, bindingControllerRoot);
        path.getElements().add(new LineTo(end.getX(), end.getY()));
        if (start != null) {
            double dx = end.getX() - start.getX(), dy = end.getY() - start.getY(), len = Math.sqrt(dx * dx  + dy * dy);
            double tipDx = dx * tipSize / len, tipDy = dy * tipSize / len;
            path.getElements().addAll(List.of(
                new LineTo(end.getX() - tipDx + tipDy / 2, end.getY() - tipDy - tipDx / 2),
                new LineTo(end.getX() - tipDx - tipDy / 2, end.getY() - tipDy + tipDx / 2),
                new LineTo(end.getX(), end.getY())
            ));
        }
    }

    public class BindingKeyHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED && bindingGestureModifiers.match(event)) {
                bindingSources.forEach(bindingSource -> sourceHighlighter.highlight(bindingSource.sourceNode()));
                for (var bs : bindingSubscriptions.keySet()) {
                    var path = bindingSubscriptions.computeIfAbsent(bs, bs2 -> createBindingPath());
                    if (path.getParent() == null) {
                        bindingControllerRoot.getChildren().add(path);
                    }
                    updateBindingPath(path, bs);
                    path.setVisible(true);
                }
            } else {
                sourceHighlighter.clear();
                for (var path : bindingSubscriptions.values()) {
                    if (path != null) {
                        path.setVisible(false);
                    }
                }
            }
        }
    }

    public class BindingMouseHandler implements EventHandler<MouseEvent> {

        private BindingSource<?> bindingSource = null;
        private Path bindingPath = null;

        @Override
        public void handle(MouseEvent event) {
            if (! bindingGestureModifiers.match(event)) {
                this.bindingSource = null;
                sourceHighlighter.clear();
                targetHighlighter.clear();
                if (bindingPath != null && bindingPath.getParent() == bindingControllerRoot) {
                    bindingControllerRoot.getChildren().remove(bindingPath);
                }
                return;
            }
            var pick = event.getPickResult();
            if (! isChildOf(pick.getIntersectedNode(), bindingControllerRoot)) {
                return;
            }
            
            Optional<Path> pathMaybe = Optional.empty();
            Optional<BindingSource<?>> sourceMaybe = Optional.empty();
            Optional<BindingTarget<?>> targetMaybe = Optional.empty();
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED && pick.getIntersectedNode() instanceof Path path && isBindingPath(path)) {
                pathMaybe = Optional.of(path);
            } else if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                sourceMaybe = bindingSources.stream()
                    .filter(bindingSource -> isChildOf(pick.getIntersectedNode(), bindingSource.sourceNode()))
                    .findAny();
            } else if (this.bindingSource != null && (event.getEventType() == MouseEvent.MOUSE_DRAGGED || event.getEventType() == MouseEvent.MOUSE_RELEASED)) {
                targetMaybe = bindingTargets.stream()
                    .filter(bindingTarget -> isChildOf(pick.getIntersectedNode(), bindingTarget.targetNode()))
                    .filter(bindingTarget -> bindingSource != null && bindingSourceSupportsTarget(bindingSource, bindingTarget))
                    .findFirst();
            }
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                if (pathMaybe.isPresent()) {
                    pathHighlighter.highlight(pathMaybe.get());
                } else {
                    pathHighlighter.clear();
                }
                sourceHighlighter.clear();
                if (sourceMaybe.isPresent()) {
                    sourceHighlighter.highlight(sourceMaybe.get().sourceNode());
                }
            } else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                if (pathMaybe.isPresent()) {
                    getBindingSubscription(pathMaybe.get()).ifPresent(BindingController.this::removeBinding);
                } else {
                    this.bindingSource = sourceMaybe.orElse(null);
                    if (this.bindingSource != null) {
                        bindingPath = createBindingPath();
                        bindingPath.setMouseTransparent(true);
                    }
                }
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (bindingPath != null) {
                    if (bindingPath.getParent() == null) {
                        bindingControllerRoot.getChildren().add(bindingPath);
                    }
                    updateBindingPathStart(bindingPath, bindingSource.sourceNode());
                    Point3D pickPoint = pick.getIntersectedPoint();
                    updateBindingPathEnd(bindingPath, new Point2D(pickPoint.getX(), pickPoint.getY()), pick.getIntersectedNode());
                }
                bindingTargets.forEach(bindingTarget -> targetHighlighter.highlight(bindingTarget.targetNode()));
            } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                if (this.bindingSource != null && targetMaybe.isPresent()) {
                    bindingPath.setMouseTransparent(false);
                    bindSourceToTarget(this.bindingSource, targetMaybe.get(), bindingPath);
                    bindingPath = null;
                }
                this.bindingSource = null;
                sourceHighlighter.clear();
                targetHighlighter.clear();
                if (bindingPath != null && bindingPath.getParent() == bindingControllerRoot) {
                    bindingControllerRoot.getChildren().remove(bindingPath);
                }
            }
        }
    }
}
