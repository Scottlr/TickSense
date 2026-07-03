package com.ticksense.ui;

import java.awt.BorderLayout;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import net.runelite.client.ui.ColorScheme;

public final class DeveloperDiagnosticsPanel extends JPanel
{
    private final Supplier<String> diagnosticsTextSupplier;
    private final Runnable exportAction;
    private final JTextArea diagnosticsArea;
    private final JButton exportButton;

    public DeveloperDiagnosticsPanel(Supplier<String> diagnosticsTextSupplier, Runnable exportAction)
    {
        this.diagnosticsTextSupplier = Objects.requireNonNull(diagnosticsTextSupplier, "diagnosticsTextSupplier");
        this.exportAction = Objects.requireNonNull(exportAction, "exportAction");

        setLayout(new BorderLayout(0, 8));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        diagnosticsArea = PanelTextAreas.readOnlyWrapped();

        final JButton refreshButton = new JButton("Refresh diagnostics");
        refreshButton.addActionListener(event -> refresh());

        exportButton = new JButton("Export debug bundle");
        exportButton.addActionListener(event -> exportAction.run());

        final JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.add(refreshButton);
        controls.add(exportButton);

        add(controls, BorderLayout.NORTH);
        add(PanelScrollPanes.borderless(diagnosticsArea), BorderLayout.CENTER);
        refresh();
    }

    public void refresh()
    {
        diagnosticsArea.setText(diagnosticsTextSupplier.get());
    }

    public void setExportEnabled(boolean enabled)
    {
        exportButton.setEnabled(enabled);
    }
}
