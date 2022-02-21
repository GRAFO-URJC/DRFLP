package es.urjc.etsii.grafo.drflp.model;

import es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.stream.Stream;

public class OptNeighborhood extends LazyNeighborhood<DRFLPBaseMove, DRFLPSolution, DRFLPInstance> {

    @Override
    public Stream<DRFLPBaseMove> stream(DRFLPSolution solution) {
        // Assume first row has at least two elements
        Stream<DRFLPBaseMove> stream = Stream.empty();
        for (int i = 0; i < solution.getNRows(); i++) {
            if (solution.getRowSize(i) > 1) {
                stream = Stream.concat(stream, buildStream(new OptMove(solution, i, 0, 1)));
            }
        }
        return stream;
    }

    protected static class OptMove extends DRFLPBaseMove {

        private final int row;

        public OptMove(DRFLPSolution s, int row, int index1, int index2) {
            super(s, index1, index2, twoOptCost(s, row, index1, index2));
            this.row = row;
        }

        // Neighborhoods cost calculation and move execution
        private static double twoOptCost(DRFLPSolution solution, int row, int index1, int index2) {
            // Antes de hacer el movimiento
            assert solution.equalsSolutionData(solution.recalculateCentersCopy());
            double before = solution.partialCost(row, index1, index2);

            // Do movement
            ArrayUtil.reverseFragment(solution.solutionData[row], index1, index2);

            // Despues de hacer el movimiento
            solution.recalculateCentersInPlace(row);
            double after = solution.partialCost(row, index1, index2);

            // Undo movement
            ArrayUtil.reverseFragment(solution.solutionData[row], index1, index2);

            // Al deshacer el coste deberia quedar igual
            solution.recalculateCentersInPlace(row);
            assert DoubleComparator.equals(before, solution.partialCost(row, index1, index2));

            return after - before;
        }

        @Override
        protected void _execute() {
            this.twoOpt(this.row, this.index1, this.index2, this.score);
        }

        @Override
        public OptMove next() {
            // Copy for new movement
            int _row = row, _index1 = index1, _index2 = index2;
            _index2++;
            if (_index2 >= s.getRowSize(_row)) {
                // Advance _index1 and reset _index2
                _index1++;
                _index2 = _index1 + 1;
                if (_index2 >= s.getRowSize(_row)) {
                    return null; // End of stream
                }
            }

            return new OptMove(this.s, _row, _index1, _index2);
        }

        public void twoOpt(int row, int index1, int index2, double cost) {
            var solution = getSolution();
            solution.cachedScore += cost;
            ArrayUtil.reverseFragment(solution.solutionData[row], index1, index2);
            solution.recalculateCentersInPlace(row);
            assert DoubleComparator.isPositiveOrZero(solution.cachedScore) : "Cannot have negative score in this problem: " + solution.cachedScore;
        }
    }
}

