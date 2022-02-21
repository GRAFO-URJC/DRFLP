#!/bin/bash

java -Xmx8G -Xms8G -cp DRFP-0.1-SNAPSHOT.jar -Dloader.path=gurobi912/linux64/lib org.springframework.boot.loader.PropertiesLauncher

