package com.ticksense.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.ticksense.analytics.ReportSummary;
import net.runelite.client.ui.ColorScheme;

public class ReportListPanel extends JPanel
{
    private final JLabel emptyState = new JLabel("No reports yet.", SwingConstants.CENTER);
    private final DefaultListModel<ReportSummary> reportModel = new DefaultListModel<>();
    private final JList<ReportSummary> reportList = new JList<>(reportModel);

    public ReportListPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(12, 8, 12, 8));

        emptyState.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        reportList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        reportList.setForeground(ColorScheme.TEXT_COLOR);
        reportList.setCellRenderer(new ReportSummaryRenderer());
        reportList.setFocusable(false);

        add(emptyState, BorderLayout.CENTER);
    }

    public void setReports(List<ReportSummary> reports)
    {
        final List<ReportSummary> normalized = reports == null ? Collections.emptyList() : reports;
        reportModel.clear();
        for (ReportSummary report : normalized)
        {
            reportModel.addElement(report);
        }

        removeAll();
        if (normalized.isEmpty())
        {
            add(emptyState, BorderLayout.CENTER);
        }
        else
        {
            add(new JScrollPane(reportList), BorderLayout.CENTER);
            reportList.setSelectedIndex(0);
        }
        revalidate();
        repaint();
    }

    public void setEmptyState(String text)
    {
        emptyState.setText(text);
    }

    public void onSelectionChanged(Consumer<ReportSummary> listener)
    {
        reportList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting())
            {
                listener.accept(reportList.getSelectedValue());
            }
        });
    }

    public int getReportCount()
    {
        return reportModel.getSize();
    }

    private static final class ReportSummaryRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ReportSummary)
            {
                label.setText(ReportTextFormatter.formatSummaryLine((ReportSummary) value));
                label.setToolTipText(((ReportSummary) value).getEvidenceSummaryText());
                label.setBorder(new EmptyBorder(6, 6, 6, 6));
            }
            return label;
        }
    }
}
