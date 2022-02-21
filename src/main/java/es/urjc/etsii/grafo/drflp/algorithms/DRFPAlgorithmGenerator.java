package es.urjc.etsii.grafo.drflp.algorithms;

import es.urjc.etsii.grafo.drflp.ParallelMultiStartAlgorithm;
import es.urjc.etsii.grafo.drflp.constructives.DRFPRandomConstructive;
import es.urjc.etsii.grafo.drflp.constructives.DRFPRandomFakeFacilitiesConstructive;
import es.urjc.etsii.grafo.drflp.constructives.grasp.DRFPFakeFacilitiesBySwapListManager;
import es.urjc.etsii.grafo.drflp.constructives.tetris.DRFPTetrisConstructive;
import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.model.MoveBySwapNeighborhood;
import es.urjc.etsii.grafo.drflp.shake.IRSDestructive;
import es.urjc.etsii.grafo.drflp.shake.RandomRemoveDestructive;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.IteratedGreedy;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.solver.create.grasp.RandomGreedyGRASPConstructive;
import es.urjc.etsii.grafo.solver.destructor.DestroyRebuild;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.solver.irace.IraceAlgorithmGenerator;
import es.urjc.etsii.grafo.solver.irace.IraceRuntimeConfiguration;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class DRFPAlgorithmGenerator extends IraceAlgorithmGenerator<DRFLPSolution, DRFLPInstance> {

    /**
     * Stop if not improved in the configured number of iterations
     */
    private static final int STOP_IF_NOT_IMPROVED_IN = 100;

    /**
     * Limit max execution time for experiments coming from irace to 1 minute.
     */
    private static final int timeLimit = 1;
    private static final TimeUnit timeUnit = TimeUnit.MINUTES;

    /**
     * Build an algorithm object from the given Irace configuration.
     * @param config Requested configuration from irace
     * @return Algorithm object
     */
    @Override
    public Algorithm<DRFLPSolution, DRFLPInstance> buildAlgorithm(IraceRuntimeConfiguration config) {

        // Always minimize cost
        boolean maximizing = false;

        // Local Search is not configurable
        var moveBySwapLS = new LocalSearchBestImprovement<>(maximizing, new MoveBySwapNeighborhood());

        // Choose stop implementation with their parameters
        var stop = switch (config.getValue("stop").orElseThrow()){
            case "fraction" -> new FIUtil.FractionOfWidthStop(parseDouble(config.getValue("fractionv").orElseThrow()));
            case "constant" -> new FIUtil.ConstantStop(parseInt(config.getValue("constantv").orElseThrow()));
            default -> throw new IllegalArgumentException("Unknown stopping criterion: " + config.getValue("stop").orElseThrow());
        };

        // Choose and configure fake facilities increment strategy
        var incrementer = switch (config.getValue("increment").orElseThrow()){
            case "linearinc" -> new FIUtil.LinearFakesForIteration(parseInt(config.getValue("linearratio").orElseThrow()));
            case "fiboinc" -> new FIUtil.FibonacciFakesForIteration();
            default -> throw new IllegalArgumentException("Unknown incrementer strategy: " + config.getValue("increment").orElseThrow());
        };

        // Retrieve alpha values, alpha1 is used in construction phase, alpha2 during reconstruction
        var alpha1 = config.getValue("alpha1");
        var alpha2 = config.getValue("alpha2");

        // Choose and configure constructive implementation
        Function<double[], Constructive<DRFLPSolution, DRFLPInstance>> constructive = switch (config.getValue("constructive").orElseThrow()){
            case "graspgr" -> (fakes) -> new GreedyRandomGRASPConstructive<>(new DRFPFakeFacilitiesBySwapListManager(fakes), parseDouble(alpha1.orElseThrow()), maximizing);
            case "grasprg" -> (fakes) -> new RandomGreedyGRASPConstructive<>(new DRFPFakeFacilitiesBySwapListManager(fakes), parseDouble(alpha1.orElseThrow()), maximizing);
            case "tetris" -> (fakes) -> new DRFPTetrisConstructive(fakes, parseDouble(alpha1.orElseThrow()));
            case "random" -> DRFPRandomFakeFacilitiesConstructive::new;
            default -> throw new IllegalArgumentException("Unknown constructive method: " + config.getValue("constructive").orElseThrow());
        };

        // Destruction ratio: % of facilities removed. Used when the reconstructive is graspgr, grasprg or random.
        // Tetris reconstruct always uses tetris destructive
        var destratio = config.getValue("destratio");

        // Choose and configure reconstructive implementation
        Function<double[], Shake<DRFLPSolution, DRFLPInstance>> reconstructive = switch (config.getValue("reconstructive").orElseThrow()){
            case "graspgr" -> (fakes) -> new DestroyRebuild<>(new GreedyRandomGRASPConstructive<>(new DRFPFakeFacilitiesBySwapListManager(fakes), parseDouble(alpha2.orElseThrow()), maximizing), new RandomRemoveDestructive(parseDouble(destratio.orElseThrow())));
            case "grasprg" -> (fakes) -> new DestroyRebuild<>(new RandomGreedyGRASPConstructive<>(new DRFPFakeFacilitiesBySwapListManager(fakes), parseDouble(alpha2.orElseThrow()), maximizing), new RandomRemoveDestructive(parseDouble(destratio.orElseThrow())));
            case "tetris" -> (fakes) -> new DestroyRebuild<>(new DRFPTetrisConstructive(fakes, parseDouble(alpha2.orElseThrow())), new IRSDestructive());
            case "random" -> (fakes) -> new DestroyRebuild<>(new DRFPRandomConstructive(), new RandomRemoveDestructive(parseDouble(destratio.orElseThrow())));
            default -> throw new IllegalArgumentException("Unknown reconstructive method: " + config.getValue("reconstructive").orElseThrow());
        };

        // Total iterations budget is 1_000_000. Distribute according to 'iterationsratio' parameter between multistart and iterated greedy.
        int totalIterations = 1000 * 1000;
        int multiStartIterations = parseInt(config.getValue("iterationsratio").orElseThrow());
        int iteratedGreedyIterations = totalIterations / multiStartIterations;
        assert iteratedGreedyIterations * multiStartIterations == totalIterations;

        // Build final algorithm with all the components
        return new FakeIncrementer(
                "IraceTuning",
                (fakes) -> new ParallelMultiStartAlgorithm(
                        multiStartIterations,
                        "MultiStart IG",
                        new IteratedGreedy<>(
                                iteratedGreedyIterations,
                                STOP_IF_NOT_IMPROVED_IN,
                                constructive.apply(fakes),
                                reconstructive.apply(fakes),
                                moveBySwapLS
                        )
                ),
                incrementer,
                stop,
                timeLimit,
                timeUnit
        );
    }
}
