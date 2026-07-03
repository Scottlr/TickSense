package com.ticksense.analytics;

import com.ticksense.common.TextValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ScoreBreakdown
{
    private final double baseScore;
    private final List<Component> components;
    private final ExecutionScore executionScore;

    public ScoreBreakdown(double baseScore, List<Component> components)
    {
        this.baseScore = requireFinite(baseScore, "baseScore");
        this.components = immutableComponents(components);
        this.executionScore = new ExecutionScore(baseScore + totalAdjustment(this.components));
    }

    public static Component bonus(String key, String label, double amount, String reason)
    {
        return new Component(key, label, Math.abs(requireFinite(amount, "amount")), reason);
    }

    public static Component penalty(String key, String label, double amount, String reason)
    {
        return new Component(key, label, -Math.abs(requireFinite(amount, "amount")), reason);
    }

    public double getBaseScore()
    {
        return baseScore;
    }

    public List<Component> getComponents()
    {
        return components;
    }

    public ExecutionScore getExecutionScore()
    {
        return executionScore;
    }

    public double totalAdjustment()
    {
        return totalAdjustment(components);
    }

    private static double totalAdjustment(List<Component> components)
    {
        double total = 0.0D;
        for (Component component : components)
        {
            total += component.getDelta();
        }
        return total;
    }

    private static List<Component> immutableComponents(List<Component> components)
    {
        if (components == null || components.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<Component> copied = new ArrayList<>(components.size());
        for (Component component : components)
        {
            copied.add(Objects.requireNonNull(component, "component"));
        }
        return Collections.unmodifiableList(copied);
    }

    private static double requireFinite(double value, String fieldName)
    {
        if (Double.isNaN(value) || Double.isInfinite(value))
        {
            throw new IllegalArgumentException(fieldName + " must be finite");
        }
        return value;
    }

    public static final class Component
    {
        private final String key;
        private final String label;
        private final double delta;
        private final String reason;

        private Component(String key, String label, double delta, String reason)
        {
            this.key = TextValues.requireText(key, "key");
            this.label = TextValues.requireText(label, "label");
            this.delta = requireFinite(delta, "delta");
            this.reason = TextValues.trimmedOrEmpty(reason);
        }

        public String getKey()
        {
            return key;
        }

        public String getLabel()
        {
            return label;
        }

        public double getDelta()
        {
            return delta;
        }

        public String getReason()
        {
            return reason;
        }

        public boolean isBonus()
        {
            return delta >= 0.0D;
        }

        public boolean isPenalty()
        {
            return delta < 0.0D;
        }
    }

}
