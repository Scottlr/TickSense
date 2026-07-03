package com.ticksense.ui;

import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

final class PanelScrollPanes
{
    private PanelScrollPanes()
    {
    }

    static JScrollPane borderless(JComponent component)
    {
        final JScrollPane scrollPane = new JScrollPane(Objects.requireNonNull(component, "component"));
        scrollPane.setBorder(null);
        return scrollPane;
    }
}
