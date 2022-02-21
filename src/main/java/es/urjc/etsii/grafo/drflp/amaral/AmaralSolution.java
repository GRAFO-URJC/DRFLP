package es.urjc.etsii.grafo.drflp.amaral;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.model.Facility;

import java.util.Arrays;
import java.util.Objects;

public class AmaralSolution {
    public final DRFLPInstance instance;
    public int[] pi;
    public int t;
    public double r;
    public double s;
    public double v;

    protected AmaralSolution(AmaralSolution solution){
        this.pi = solution.pi.clone();
        this.t = solution.t;
        this.r = solution.r;
        this.s = solution.s;
        this.v = solution.v;
        this.instance = solution.instance;
    }

    protected AmaralSolution copy(){
        return new AmaralSolution(this);
    }

    protected AmaralSolution(int[] pi, DRFLPInstance instance){
        this(pi, 0, 0, 0, instance);
    }

    protected AmaralSolution(int[] pi, int t, double r, double s, DRFLPInstance instance) {
        this.pi = pi;
        this.t = t;
        this.r = r;
        this.s = s;
        this.instance = instance;
        updateScore();
    }

    public int[] pi() {
        return pi;
    }

    public int t() {
        return t;
    }

    public double r() {
        return r;
    }

    public double s() {
        return s;
    }

    public DRFLPInstance instance(){
        return this.instance;
    }

    public void updateScore(){
        int[][] matrix = this.toMatrix();

        // Evaluate with same objective function as my implementation
        this.v = DRFLPSolution.evaluate(this.instance, matrix);
    }

    /**
     *     transform array in matrix with fast Array copy
     */
    public int[][] toMatrix(){
        return new int[][]{
                Arrays.copyOfRange(this.pi, 0, t),         // First row are elements  [0, t - 1]
                Arrays.copyOfRange(this.pi, t, this.pi.length)  // Second row are elements [t, length - 1]
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AmaralSolution) obj;
        return Objects.equals(this.pi, that.pi) &&
                this.t == that.t &&
                Double.doubleToLongBits(this.r) == Double.doubleToLongBits(that.r) &&
                Double.doubleToLongBits(this.s) == Double.doubleToLongBits(that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pi, t, r, s);
    }

    @Override
    public String toString() {
        return "AmaralSolution[" +
                "pi=" + pi + ", " +
                "t=" + t + ", " +
                "r=" + r + ", " +
                "s=" + s + ']';
    }

    public DRFLPSolution transform() {
        DRFLPSolution s = new DRFLPSolution(instance);
        // First row
        for (int i = 0; i < t; i++) {
            Facility f = instance.byId(pi[i]);
            s.insertLast(0, f);
        }
        // Second row
        for (int i = t; i < this.pi.length; i++) {
            Facility f = instance.byId(pi[i]);
            s.insertLast(1, f);
        }
        s.rebuildCaches();
        s.updateLastModifiedTime();
        return s;
    }


}
