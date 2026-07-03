package com.ticksense.ui;

import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import net.runelite.client.ui.ColorScheme;

final class PanelLists
{
    private PanelLists()
    {
    }

    static <T> JList<T> readOnlyList(DefaultListModel<T> model, Color foreground)
    {
        final JList<T> list = new JList<>(model);
        list.setBackground(ColorScheme.DARK_GRAY_COLOR);
        list.setForeground(foreground);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFocusable(false);
        return list;
    }
}
