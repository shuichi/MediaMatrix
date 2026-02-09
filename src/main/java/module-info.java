open module mediamatrix {
    requires java.desktop;
    requires java.logging;
    requires java.prefs;

    requires com.formdev.flatlaf;
    requires com.formdev.flatlaf.extras;
    requires com.github.weisj.jsvg;
    requires org.apache.commons.jexl3;
    requires org.jfree.jfreechart;
    requires AbsoluteLayout.RELEASE280;

    exports mediamatrix;
}
