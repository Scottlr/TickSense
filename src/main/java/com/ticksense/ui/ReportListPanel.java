package com.ticksense.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class ReportListPanel extends JPanel
{
    public ReportListPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(12, 8, 12, 8));

        final JLabel emptyState = new JLabel("No reports yet.", SwingConstants.CENTER);
        emptyState.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        add(emptyState, BorderLayout.CENTER);
    }
}
