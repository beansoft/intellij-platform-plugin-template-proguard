package beansoft.intellij.util.ui;

import com.intellij.BundleBase;
import com.intellij.diagnostic.LoadingState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.TextWithMnemonic;
import com.intellij.ui.*;
import com.intellij.ui.mac.foundation.Foundation;
import com.intellij.ui.paint.LinePainter2D;
import com.intellij.ui.paint.PaintUtil.RoundingMode;
import com.intellij.ui.render.RenderingUtil;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.*;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.JBTreeTraverser;
import org.intellij.lang.annotations.JdkConstants;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;
import java.awt.print.PrinterGraphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

import com.intellij.openapi.editor.colors.EditorColors;

/**
 * Port some features from 212 and fix some internal method usage.
 * @author beansoft
 * @see https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/util/ui/UIUtil.java
 */
public class UIUtilEx {
    private static final Key<Boolean> IS_SHOWING = Key.create("Component.isShowing");
    private static final Key<Boolean> HAS_FOCUS = Key.create("Component.hasFocus");

    /**
     * Checks if a component is showing in a more general sense than UI visibility,
     * sometimes it's useful to limit various activities by checking the visibility of the real UI,
     * but it could be unneeded in headless mode or in other scenarios.
     * @see UIUtil#hasFocus(Component)
     */
    @ApiStatus.Experimental
    public static boolean isShowing(@NotNull Component component) {
        if (Boolean.getBoolean("java.awt.headless") || component.isShowing()) {
            return true;
        }

        ColorKey colorKey = EditorColors.SEPARATOR_ABOVE_COLOR;

        while (component != null) {
            JComponent jComponent = component instanceof JComponent ? (JComponent)component : null;
            if (jComponent != null && Boolean.TRUE.equals(jComponent.getClientProperty(IS_SHOWING))) {
                return true;
            }
            component = component.getParent();
        }

        return false;
    }

    /**
     * Checks if a component is focused in a more general sense than UI focuses,
     * sometimes useful to limit various activities by checking the focus of real UI,
     * but it could be unneeded in headless mode or in other scenarios.
     * @see UIUtil#isShowing(Component)
     */
//    @ApiStatus.Experimental
    public static boolean hasFocus(@NotNull Component component) {
        if (GraphicsEnvironment.isHeadless() || component.hasFocus()) {
            return true;
        }

        JComponent jComponent = component instanceof JComponent ? (JComponent)component : null;
        return jComponent != null && Boolean.TRUE.equals(jComponent.getClientProperty(HAS_FOCUS));
    }

    public static @Nullable Color getDeprecatedBackground() {
        return Registry.getColor("ui.deprecated.components.color", null);
    }
}
