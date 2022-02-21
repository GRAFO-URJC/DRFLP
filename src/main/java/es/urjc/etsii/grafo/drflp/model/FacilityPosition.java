package es.urjc.etsii.grafo.drflp.model;

import java.util.Objects;

public class FacilityPosition {
    private static final double UNDEFINED = -1;

    public final Facility facility;
    public double lastCenter = -1;

    public FacilityPosition(Facility facility) {
        this.facility = facility;
    }

    public FacilityPosition(Facility facility, double lastCenter) {
        this(facility);
        this.lastCenter = lastCenter;
    }

    public FacilityPosition(FacilityPosition facility) {
        this.facility = facility.facility;
        this.lastCenter = facility.lastCenter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacilityPosition that = (FacilityPosition) o;
        return Double.compare(that.lastCenter, lastCenter) == 0 && Objects.equals(facility, that.facility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facility, lastCenter);
    }

    @Override
    public String toString() {
        return "FacilityPosition{" +
                "lastCenter=" + lastCenter +
                ", facility=" + facility +
                '}';
    }

    public boolean isUndefined(){
        return this.lastCenter == UNDEFINED;
    }

    public double left() {
        if (this.isUndefined()){
            return UNDEFINED;
        }
        return this.lastCenter - this.facility.width / (double) 2;
    }

    public double right() {
        if (this.isUndefined()){
            return UNDEFINED;
        }
        return this.lastCenter + this.facility.width / (double) 2;
    }

    /**
     * Check if a facility is over or under another facility.
     * @return True if the given facility would collide with the current one if they were on the same row.
     */
    public boolean stacks(FacilityPosition position){
        return this.left() < position.right() && position.left() < this.right();
    }
}
