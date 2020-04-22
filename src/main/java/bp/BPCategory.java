/*
 * BP categories using an enum
 */
package bp;
/**
 *
 * @author kanolan
 */
public enum BPCategory {
    Low("Low Blood Pressure"),
    Normal("Normal Blood Pressure"),
    PreHigh("Pre-High Blood Pressure"),
    High("High Blood Pressure")
    ;

    private final String category;

    BPCategory(String category) {
        this.category = category;
    }
    
    public String getCategory() {
        return this.category;
    }

}
