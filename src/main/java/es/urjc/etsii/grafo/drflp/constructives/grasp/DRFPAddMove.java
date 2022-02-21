package es.urjc.etsii.grafo.drflp.constructives.grasp;


import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.model.Facility;
import es.urjc.etsii.grafo.drflp.model.FacilityPosition;
import es.urjc.etsii.grafo.solution.EagerMove;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Objects;

public class DRFPAddMove extends EagerMove<DRFLPSolution, DRFLPInstance> {
    private final double cost;
    private final int position;
    private final int row;
    private final FacilityPosition facilityPosition;

    public DRFPAddMove(DRFLPSolution solution, int row, int position, Facility f, double cost) {
        super(solution);
        this.row = row;
        this.position = position;
        this.facilityPosition = new FacilityPosition(f);
        this.cost = cost;
    }

    public DRFPAddMove(DRFLPSolution solution, int row, int position, Facility f) {
        super(solution);
        this.row = row;
        this.position = position;
        this.facilityPosition = new FacilityPosition(f);
        this.cost = solution.insertCost(row, position, facilityPosition);
    }

    @Override
    protected void _execute() {
        this.s.insert(row, position, cost, facilityPosition);
    }

    @Override
    public double getValue() {
        return this.cost;
    }

    @Override
    public boolean improves() {
        return DoubleComparator.isNegative(this.getValue());
    }

    @Override
    public String toString() {
        return "DRFPAddMove{" +
                "c=" + cost +
                ", f=" + facilityPosition.facility.id +
                ", row=" + row +
                ", pos=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DRFPAddMove that = (DRFPAddMove) o;
        return Double.compare(that.cost, cost) == 0 && position == that.position && row == that.row && Objects.equals(facilityPosition, that.facilityPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, position, row, facilityPosition);
    }

    @Override
    public boolean isValid() {
        // If the movement is not valid, it should not be in the candidate list.
        // Moves in candidate list are always valid
        return true;
    }
}
