package com.ticksense.ui;

import com.ticksense.analytics.TickLossBreakdown;
import java.awt.BorderLayout;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class TickLossBreakdownPanel extends JPanel
{
    private final DefaultListModel<String> items = new DefaultListModel<>();
    private final JList<String> breakdownList = new JList<>(items);

    public TickLossBreakdownPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(0, 0, 8, 0));

        breakdownList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        breakdownList.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        breakdownList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        breakdownList.setFocusable(false);

        final JScrollPane scrollPane = new JScrollPane(breakdownList);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setBreakdown(TickLossBreakdown breakdown)
    {
        items.clear();
        if (breakdown == null)
        {
            items.addElement("No tick loss breakdown yet.");
            return;
        }

        items.addElement("Total tick loss: " + breakdown.getTotalTickLoss() + " ticks");
        for (Map.Entry<String, Integer> entry : breakdown.getCategories().entrySet())
        {
            items.addElement(entry.getKey() + ": " + entry.getValue() + " ticks");
        }
        breakdownList.setSelectedIndex(0);
    }
}
