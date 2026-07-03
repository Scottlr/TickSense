package com.ticksense.ui;

import com.ticksense.analytics.ReportSummary;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import net.runelite.client.ui.ColorScheme;

public class ReportListPanel extends JPanel
{
    private static final String EMPTY_CARD = "empty";
    private static final String REPORTS_CARD = "reports";

    private final JLabel emptyState = new JLabel("No reports yet.", SwingConstants.CENTER);
    private final DefaultListModel<ReportSummary> reportModel = new DefaultListModel<>();
    private final JList<ReportSummary> reportList = PanelLists.readOnlyList(reportModel, ColorScheme.TEXT_COLOR);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel content = new JPanel(contentLayout);
    private final JScrollPane reportScrollPane = PanelScrollPanes.borderless(reportList);

    public ReportListPanel()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(12, 8, 12, 8));

        content.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        emptyState.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        reportList.setCellRenderer(new ReportSummaryRenderer());

        content.add(emptyState, EMPTY_CARD);
        content.add(reportScrollPane, REPORTS_CARD);
        add(content, BorderLayout.CENTER);
        showEmptyState();
    }

    public void setReports(List<ReportSummary> reports)
    {
        final List<ReportSummary> normalized = reports == null ? Collections.emptyList() : reports;
        reportModel.clear();
        for (ReportSummary report : normalized)
        {
            reportModel.addElement(report);
        }

        if (normalized.isEmpty())
        {
            reportList.clearSelection();
            showEmptyState();
        }
        else
        {
            showReports();
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

    private void showEmptyState()
    {
        contentLayout.show(content, EMPTY_CARD);
    }

    private void showReports()
    {
        contentLayout.show(content, REPORTS_CARD);
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
