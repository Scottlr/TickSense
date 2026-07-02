package com.ticksense.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class TickSensePanel extends PluginPanel
{
    private final ReportListPanel reportListPanel;

    public TickSensePanel()
    {
        super(false);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        final JLabel title = new JLabel("TickSense");
        title.setForeground(ColorScheme.BRAND_ORANGE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        reportListPanel = new ReportListPanel();
        add(reportListPanel, BorderLayout.CENTER);
    }

    public ReportListPanel getReportListPanel()
    {
        return reportListPanel;
    }
}
