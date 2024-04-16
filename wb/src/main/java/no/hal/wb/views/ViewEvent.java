package no.hal.wb.views;

public sealed interface ViewEvent {

    ViewInfo viewInfo();

    public record Added(ViewInfo viewInfo) implements ViewEvent {
    }    
    public record Removed(ViewInfo viewInfo) implements ViewEvent {
    }
}
