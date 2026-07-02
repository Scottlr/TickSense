package com.ticksense.ui;

import com.ticksense.analytics.OpportunityTimelineEntry;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class OpportunityTimelinePanel extends JPanel
{
    private final DefaultListModel<String> entries = new DefaultListModel<>();
    private final JList<String> timelineList = new JList<>(entries);

    public OpportunityTimelinePanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(0, 0, 8, 0));

        timelineList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        timelineList.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        timelineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timelineList.setFocusable(false);

        final JScrollPane scrollPane = new JScrollPane(timelineList);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setEntries(List<OpportunityTimelineEntry> timelineEntries)
    {
        entries.clear();
        if (timelineEntries == null || timelineEntries.isEmpty())
        {
            entries.addElement("No opportunity timeline yet.");
            return;
        }

        for (OpportunityTimelineEntry entry : timelineEntries)
        {
            entries.addElement(ReportTextFormatter.timelineEntry(entry));
        }
        timelineList.setSelectedIndex(0);
    }
}
