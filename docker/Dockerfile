FROM azul/zulu-openjdk-alpine:17-jre-headless
# Any custom config property can be added!
COPY instances /instances
COPY sota.tsv /sota.tsv
COPY target/DRFP*.jar /app.jar

CMD [\
"/usr/bin/java",\
"--add-opens", "java.base/java.util=ALL-UNNAMED",\
"--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",\
"--add-opens", "java.base/java.lang=ALL-UNNAMED",\
# Set memory
"-Xmx1G", "-Xms1G",\
"-jar", "/app.jar"\
]
