package com.ticksense.ui;

import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

final class PanelTextAreas
{
    private PanelTextAreas()
    {
    }

    static JTextArea readOnlyWrapped()
    {
        final JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(true);
        textArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        textArea.setForeground(ColorScheme.TEXT_COLOR);
        textArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        return textArea;
    }
}
