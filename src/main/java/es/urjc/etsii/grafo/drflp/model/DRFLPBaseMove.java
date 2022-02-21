package es.urjc.etsii.grafo.drflp.model;

import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Objects;

public abstract class DRFLPBaseMove extends LazyMove<DRFLPSolution, DRFLPInstance> {

     final int index1;
     final int index2;
     final double score;

    public DRFLPBaseMove(DRFLPSolution s, int index1, int index2, double score) {
        super(s);
        this.index1 = index1;
        this.index2 = index2;
        this.score = score;
    }

    @Override
    public boolean improves() {
        // Improves solution if score is strictly negative
        return DoubleComparator.isNegative(score);
    }

    @Override
    public boolean isValid() {
        return true; // Swap always leaves the solution in a valid state
    }

    @Override
    public double getValue() {
        return this.score;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{" +
                "i1=" + index1 +
                ", i2=" + index2 +
                ", sc=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DRFLPBaseMove that = (DRFLPBaseMove) o;
        return index1 == that.index1 && index2 == that.index2 && Double.compare(that.score, score) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index1, index2, score);
    }

    @Override
    public DRFLPBaseMove next() {
        throw new UnsupportedOperationException();
    }
}
