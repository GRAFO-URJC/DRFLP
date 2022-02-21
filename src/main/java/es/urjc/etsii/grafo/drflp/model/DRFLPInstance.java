package es.urjc.etsii.grafo.drflp.model;

import es.urjc.etsii.grafo.io.Instance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DRFLPInstance extends Instance {

    // DRLP always has 2 rows
    public static final int NROWS = 2;
    private static Comparator<DRFLPInstance> comparator = Comparator.comparing(DRFLPInstance::getNRealFacilities).thenComparing(DRFLPInstance::getName);

    private final Facility[] facilities;
    private final int[][] weigths;

    public DRFLPInstance(String name, Facility[] facilities, int[][] weigths){
        super(name);
        this.facilities = new Facility[facilities.length];
        this.weigths = weigths;

        for (int i = 0; i < facilities.length; i++) {
            this.facilities[i] = facilities[i];
        }
    }

    public int getNRealFacilities() {
        return facilities.length;
    }

    public int getNRows(){
        return NROWS;
    }

    public int getWeight(Facility f1, Facility f2){
        if(f1.fake || f2.fake){
            return 0;
        }
        return this.weigths[f1.id][f2.id];
    }

    public int getWeight(int f1, int f2){
        if(f1 < 0 || f2 < 0){
            return 0;
        }

        return this.weigths[f1][f2];
    }

    // todo: improve implementation
    public Facility byId(int id){
        return id < 0? new Facility(id, 0.5D, true): this.facilities[id];
    }

    @Override
    public int compareTo(Instance o) {
        return comparator.compare(this, (DRFLPInstance) o);
    }

    public List<Facility> getFacilities(){
        return Arrays.asList(this.facilities);
    }

    /**
     * Returns total width as if we put all facilities in a single row without spaces
     * @return total width as a double
     */
    public double getTotalWidth(){
        double totalWidth = 0;
        for (var f : this.getFacilities()) {
            if (f.fake) continue;
            totalWidth += f.width;
        }
        return totalWidth;
    }
}
