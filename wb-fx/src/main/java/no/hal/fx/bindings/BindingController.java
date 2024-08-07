package no.hal.fx.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
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
    
    public void addBindingSources(BindingsSource bindingsSource) {
        var sources = bindingsSource.getBindingSources();
        this.bindingSources.addAll(sources);
        sources.forEach(this::updateBindingSourceTooltip);
    }
    public void removeBindingSources(BindingsSource bindingsSource) {
        var sources = bindingsSource.getBindingSources();
        this.bindingSources.removeAll(sources);
        sources.forEach(this::updateBindingSourceTooltip);
    }
    
    public void addBindingTargets(BindingsTarget bindingsTarget) {
        var targets = bindingsTarget.getBindingTargets();
        this.bindingTargets.addAll(targets);
        targets.forEach(this::updateBindingTargetTooltip);
    }
    public void removeBindingTargets(BindingsTarget bindingsTarget) {
        var targets = bindingsTarget.getBindingTargets();
        this.bindingTargets.removeAll(targets);
        targets.forEach(this::updateBindingTargetTooltip);
    }

    private Map<BindingSubscription, Path> bindingSubscriptions = new HashMap<>();

    private Stream<BindingSubscription> getBindingSubscriptions(Predicate<BindingSubscription> filter) {
        return bindingSubscriptions.keySet().stream().filter(filter);
    }
    private Stream<BindingSubscription> getBindingSubscriptions(BindingSource<?> source, BindingTarget<?> target) {
        return getBindingSubscriptions(subscription -> (source == null || source == subscription.source()) && (target == null || target == subscription.target()));
    }

    private boolean bindingSourceMatchesTarget(BindingSource<?> source, BindingTarget<?> target) {
        return source.sourceClass().equals(target.targetClass());
    }

    public Optional<BindingSource<?>> findSourceForTarget(BindingTarget<?> bindingTarget) {
        return bindingSources
            // reverse, so newly added sources are tried first
            .reversed().stream()
            .filter(bindingSource -> bindingSourceMatchesTarget(bindingSource, bindingTarget)).findFirst();
    }
    public Optional<BindingTarget<?>> findTargetForSource(BindingSource<?> bindingSource) {
        return bindingTargets
            // reverse, so newly added targets are tried first
            .reversed().stream()
            .filter(bindingTarget -> bindingSourceMatchesTarget(bindingSource, bindingTarget)).findFirst();
    }

    public boolean bindToTarget(BindingTarget<?> bindingTarget) {
        var source = findSourceForTarget(bindingTarget);
        source.ifPresent(bindingSource -> bindSourceToTarget(bindingSource, bindingTarget, null));
        return source.isPresent();
    }
    public boolean bindToSource(BindingSource<?> bindingSource) {
        var target = findTargetForSource(bindingSource);
        target.ifPresent(bindingTarget -> bindSourceToTarget(bindingSource, bindingTarget, null));
        return target.isPresent();
    }

    public void bindToTargets(BindingsTarget bindingsTarget) {
        bindingsTarget.getBindingTargets().forEach(this::bindToTarget);
    }
    public void bindToSources(BindingsSource bindingsSource) {
        bindingsSource.getBindingSources().forEach(this::bindToSource);
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
        updateBindingSourceTooltip(bindingSource);
        updateBindingTargetTooltip(bindingTarget);
    }

    private void updateBindingSourceTooltip(BindingSource<?> bindingSource) {
        updateBindingsTooltip(bindingSource.sourceNode());
    }
    private void updateBindingTargetTooltip(BindingTarget<?> bindingTarget) {
        updateBindingsTooltip(bindingTarget.targetNode());
    }

    private final static String SOURCE_TOOLTIP_FORMAT = "Source of %s, bound to %s target(s)";
    private final static String TARGET_TOOLTIP_FORMAT = "Target for %s, bound to %s source(s)";

    private void updateBindingsTooltip(Node node) {
        if (node instanceof Control control) {
            StringBuilder tooltip = new StringBuilder();
            bindingSources.stream().filter(bs -> bs.sourceNode() == node).forEach(bs -> appendTooltip(tooltip, SOURCE_TOOLTIP_FORMAT, bs.sourceClass().getSimpleName(), getBindingSubscriptions(bs, null).count()));
            bindingTargets.stream().filter(bt -> bt.targetNode() == node).forEach(bt -> appendTooltip(tooltip, TARGET_TOOLTIP_FORMAT, bt.targetClass().getSimpleName(), getBindingSubscriptions(null, bt).count()));
            if (tooltip.length() > 0) {
                control.setTooltip(new Tooltip(tooltip.toString()));
            }
        }
    }
    private void appendTooltip(StringBuilder tooltip, String format, Object... args) {
        if (tooltip.length() > 0) {
            tooltip.append("\n");
        }
        tooltip.append(format.formatted(args));
    }

    public void removeBinding(BindingSubscription bindingSubscription) {
        Path path  = bindingSubscriptions.get(bindingSubscription);
        bindingControllerRoot.getChildren().remove(path);
        bindingSubscription.subscription().unsubscribe();
        bindingSubscriptions.remove(bindingSubscription);
    }

    public void removeBindings(BindingSource<?> source, BindingTarget<?> target) {
        getBindingSubscriptions(source, target)
            .toList()
            .forEach(this::removeBinding);
    }
    public void removeBindings(BindingSource<?> source) {
        removeBindings(source, null);
        updateBindingSourceTooltip(source);
    }
    public void removeBindings(BindingTarget<?> target) {
        removeBindings(null, target);
        updateBindingTargetTooltip(target);
    }
    public void removeBindings(BindingsSource bindingsSource) {
        bindingsSource.getBindingSources().forEach(this::removeBindings);
    }
    public void removeBindings(BindingsTarget bindingsTarget) {
        bindingsTarget.getBindingTargets().forEach(this::removeBindings);
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
            if (parentClass.isInstance(childNode) && parentTest.test(parentClass.cast(childNode))) {
                return parentClass.cast(childNode);
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
                    path.setCursor(Cursor.DISAPPEAR);
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
