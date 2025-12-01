package project.app.humanelogistics.model;

public enum DamageCategory {
    AFFECTED_PEOPLE("Affected People"),
    ECONOMIC_IMPACT("Economic Production Disruption"),
    HOUSING_DAMAGE("Houses or Buildings Damaged"),
    LOSS_OF_BELONGINGS("Loss of Personal Belongings"),
    INFRASTRUCTURE_DAMAGE("Damaged Infrastructure"),
    OTHER("Other"),
    UNKNOWN("Unknown");

    private final String displayName;

    DamageCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static DamageCategory fromString(String text) {
        if (text == null) return UNKNOWN;
        String normalized = text.trim().toUpperCase().replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try partial matching if strict match fails
            for (DamageCategory c : values()) {
                if (normalized.contains(c.name())) return c;
            }
            return OTHER;
        }
    }
}