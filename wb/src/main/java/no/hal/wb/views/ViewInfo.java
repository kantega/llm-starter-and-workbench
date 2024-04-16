package no.hal.wb.views;

public record ViewInfo(ViewProvider.Info info, String viewId, ViewProvider.Instance instance, Runnable disposer) {
}
