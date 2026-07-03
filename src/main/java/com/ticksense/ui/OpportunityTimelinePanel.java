package com.ticksense.ui;

import com.ticksense.analytics.OpportunityTimelineEntry;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class OpportunityTimelinePanel extends JPanel
{
    private final DefaultListModel<String> entries = new DefaultListModel<>();
    private final JList<String> timelineList = PanelLists.readOnlyList(entries, ColorScheme.LIGHT_GRAY_COLOR);

    public OpportunityTimelinePanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(0, 0, 8, 0));

        final JScrollPane timelineScrollPane = new JScrollPane(timelineList);
        timelineScrollPane.setBorder(null);
        add(timelineScrollPane, BorderLayout.CENTER);
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
