open module mediamatrix {
    requires java.desktop;
    requires java.logging;
    requires java.prefs;

    requires com.formdev.flatlaf;
    requires org.apache.commons.jexl3;
    requires org.jfree.jfreechart;
    requires AbsoluteLayout.RELEASE280;
    requires swingx.all;

    exports mediamatrix;
}
