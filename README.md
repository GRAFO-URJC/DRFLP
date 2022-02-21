# A practical methodology for reproducible experimentation: an application to the Double-row Facility Layout Problem

[![DOI](https://zenodo.org/badge/461803962.svg)](https://zenodo.org/badge/latestdoi/461803962)


## Abstract
TODO

## Authors
- Raúl Martín-Santamaría<br/>
email: `raul.martin at urjc.es`<br/>
Department of Computer Science and Statistics, Universidad Rey Juan Carlos,
Móstoles, 28933, Spain

- Sergio Cavero<br/>
email: `sergio.cavero at urjc.es`<br/>
Department of Computer Science and Statistics, Universidad Rey Juan Carlos,
Móstoles, 28933, Spain

- Alberto Herrán<br/>
email: `alberto.herran at urjc.es`<br/>
Department of Computer Science and Statistics, Universidad Rey Juan Carlos,
Móstoles, 28933, Spain

- Abraham Duarte<br/>
email: `abraham.duarte at urjc.es`<br/>
Department of Computer Science and Statistics, Universidad Rey Juan Carlos,
Móstoles, 28933, Spain

- J. Manuel Colmenar <br/>
email: `josemanuel.colmenar at urjc.es`<br/>
Department of Computer Science and Statistics, Universidad Rey Juan Carlos,
Móstoles, 28933, Spain

## Datasets

Instances are categorized inside the `instances` folder. 
- `all` contains all instances used in any experiment
- `small` contains only the instances used previously in Amaral's paper. https://doi.org/10.1007/s10479-020-03617-5
- `big` contains the new instances proposed for the DRFLP, already used in other FLPs.
- `benchmark` contains the instances used to calibrate the algorithm using irace.


## Instance analysis
There is a Jupyter notebook inside the `instance_selector` folder which automates the instance analysis
and automatically chooses the benchmark instances. Before executing the notebook,
install all dependencies using:
```
pip install -r requirements.txt
```

The recommended way to use and execute the notebook is using an IDE like PyCharm Professional, Datalore or similar.
Another way is manually starting Jupyter using a terminal with `jupyter notebook`
and then visit `http://localhost:8888` using any browser.

## Generating artifacts

### Recommended: using Docker
Docker containers can be easily built and executed with the scripts provided inside the Docker folder. 
The only prerequisite is having Docker installed and running. See [this page](https://docs.docker.com/engine/install/) for more information.

Building container:
```bash
cd docker
./build.sh yourname/drfp
```

Running container:
```bash
cd docker
./run.sh yourname/drfp
```

### Manual: Dependencies
The algorithms are implemented using Java 17. The dependency management system used is Maven. Both must be installed before proceeding.
Example for an Ubuntu/Debian like system:

```text
sudo apt update && sudo apt install openjdk-17-jdk maven
```

Windows users may use `choco` to install both Java and Maven.
Mac OS users may use `brew` to install both Java and Maven.

Manually installing dependencies is discouraged but possible.

### Manual: Compile and build artifact
Building the executable artifacts from source is as easy as executing `mvn clean package`.

### Manual: Executing
Launch the JAR from the root project directory using the following command.

```text
java -jar DRFP-0.1-SNAPSHOT.jar
```

Any custom property can be passed as command line parameters. See [Mork configuration](https://mork-optimization.readthedocs.io/en/latest/features/config/) for more information.

### Optional: Executing with Gurobi

If you are going to execute any algorithm that requires Gurobi (ie, Amaral heuristics), you must use the following form in order to correctly set up the classpath for Gurobi:
```text
java -cp DRFP-0.1-SNAPSHOT -Dloader.path=gurobi912/linux64/lib org.springframework.boot.loader.PropertiesLauncher
```
Remember that Gurobi must be locally installed and properly licensed before attempting to use it. Replace the path in `loader.path` with the location where Gurobi libraries are available.
Gurobi native libraries are not included in the project as their license probably does not allow it.

### Solving a new set of instances
To solve a new set of instances add them to a new folder inside the `instances` folder and launch the app either
with Docker or manually with the parameter `instances.path.default` pointing to the new folder. Example:

```
java -jar target/DRFP-0.1-SNAPSHOT.jar --instances.path.default=instances/new
```

After the program finishes, the `solutions` folder will contain best solution found by each instance/algorithm pair and the `results` folder will have all results processed in a Microsoft Excel file.
There are two experiments already defined, more can be added to the experiments folder in `src/main/java/es/urjc/etsii/grafo/drflp`:

- FinalExperiment: Our MultiStart IteratedGreedy implementation, as defined in the paper (MS-IG).
- PreviousExperiment: Our implementation of Amaral Heuristics.

The executed experiment defaults to `FinalExperiment`, but can be overriden with the `solver.experiments` parameter, example:

```
java -jar target/DRFP-0.1-SNAPSHOT.jar --solver.experiments=PreviousExperiment
```

Note that the experiment `PreviousExperiment`, ie, our implementation of Amaral heuristics, require a working installation of Gurobi.


## Cite

Consider citing our paper if used in your own work:

### DOI
Pending

### Bibtex
```
Pending
```

