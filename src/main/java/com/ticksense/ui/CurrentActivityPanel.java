package com.ticksense.ui;

import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.core.ActivitySession;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public final class CurrentActivityPanel extends JPanel
{
    private static final String NO_ACTIVITY = "None";
    private static final String NO_CANDIDATE = "No candidate";

    private final Supplier<Optional<ActivitySession>> activeSessionSupplier;
    private final Supplier<List<ActivityDiagnostic>> diagnosticsSupplier;
    private final JLabel activityValue = valueLabel(NO_ACTIVITY);
    private final JLabel startedValue = valueLabel("-");
    private final JLabel candidateValue = valueLabel(NO_CANDIDATE);

    public CurrentActivityPanel(
        Supplier<Optional<ActivitySession>> activeSessionSupplier,
        Supplier<List<ActivityDiagnostic>> diagnosticsSupplier)
    {
        this.activeSessionSupplier = activeSessionSupplier;
        this.diagnosticsSupplier = diagnosticsSupplier;

        setLayout(new BorderLayout(0, 8));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 8, 10, 8));

        final JLabel title = new JLabel("Current Activity");
        title.setForeground(ColorScheme.BRAND_ORANGE);
        add(title, BorderLayout.NORTH);

        final JPanel rows = new JPanel(new GridLayout(0, 2, 8, 4));
        rows.setOpaque(false);
        rows.add(label("Active"));
        rows.add(activityValue);
        rows.add(label("Started"));
        rows.add(startedValue);
        rows.add(label("Latest"));
        rows.add(candidateValue);
        add(rows, BorderLayout.CENTER);

        refresh();
    }

    public void refresh()
    {
        final Optional<ActivitySession> activeSession = activeSessionSupplier.get();
        if (activeSession.isPresent())
        {
            final ActivitySession session = activeSession.get();
            activityValue.setText(displayActivity(session));
            startedValue.setText("tick " + session.getStartTime().getGameTick());
        }
        else
        {
            activityValue.setText(NO_ACTIVITY);
            startedValue.setText("-");
        }

        candidateValue.setText(displayLatestDiagnostic(diagnosticsSupplier.get()));
    }

    String getActivityText()
    {
        return activityValue.getText();
    }

    String getCandidateText()
    {
        return candidateValue.getText();
    }

    private static String displayActivity(ActivitySession session)
    {
        final String displayName = session.getMetadata().get("displayName");
        if (displayName != null && !displayName.trim().isEmpty())
        {
            return displayName;
        }
        return session.getActivityType().name();
    }

    private static String displayLatestDiagnostic(List<ActivityDiagnostic> diagnostics)
    {
        if (diagnostics == null || diagnostics.isEmpty())
        {
            return NO_CANDIDATE;
        }
        final ActivityDiagnostic diagnostic = diagnostics.get(diagnostics.size() - 1);
        return diagnostic.getActivityType().name()
            + " "
            + String.format("%.2f", diagnostic.getConfidence())
            + " "
            + diagnostic.getDecision();
    }

    private static JLabel label(String text)
    {
        final JLabel label = new JLabel(text);
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return label;
    }

    private static JLabel valueLabel(String text)
    {
        final JLabel label = new JLabel(text);
        label.setForeground(ColorScheme.TEXT_COLOR);
        return label;
    }
}
