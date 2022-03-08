module com.GenSci.tools.GrahpViewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
	requires javafx.swing;
	//requires java.desktop;
	//requires javafx.base;
    opens com.GenSci.tools.GrahpViewer to javafx.fxml;
    exports com.GenSci.tools.GrahpViewer;
}
