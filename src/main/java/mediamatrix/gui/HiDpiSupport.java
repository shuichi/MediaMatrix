package mediamatrix.gui;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;

public final class HiDpiSupport {

    private HiDpiSupport() {
    }

    public static ScaleFactor scaleFactor(Component component) {
        final GraphicsConfiguration configuration = resolveGraphicsConfiguration(component);
        if (configuration == null) {
            return new ScaleFactor(1.0, 1.0);
        }
        final AffineTransform transform = configuration.getDefaultTransform();
        final double scaleX = transform == null ? 1.0 : Math.max(1.0, transform.getScaleX());
        final double scaleY = transform == null ? 1.0 : Math.max(1.0, transform.getScaleY());
        return new ScaleFactor(scaleX, scaleY);
    }

    private static GraphicsConfiguration resolveGraphicsConfiguration(Component component) {
        if (component != null && component.getGraphicsConfiguration() != null) {
            return component.getGraphicsConfiguration();
        }
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
    }

    public record ScaleFactor(double scaleX, double scaleY) {
    }
}
