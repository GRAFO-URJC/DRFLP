package es.urjc.etsii.grafo.drflp.model;

import java.util.Objects;

public class Facility {
    public final int id;
    public final double width;
    public final boolean fake;

    public Facility(int id, double width) {
        this.id = id;
        this.width = width;
        this.fake = false;
    }

    public Facility(int id, double width, boolean fake) {
        this.id = id;
        this.width = width;
        this.fake = fake;
    }

    @Override
    public String toString() {
        return "F{" +
                "i=" + id +
                ",w=" + width +
                ",f=" + fake +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facility facility = (Facility) o;
        return id == facility.id && width == facility.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, width);
    }
}
