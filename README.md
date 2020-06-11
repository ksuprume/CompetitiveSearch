# Vehicle Relocation for Ride-Hailing

This project is based on the [COMSET](https://github.com/Chessnl/COMSET-GISCUP) simulator.

## Brief description

We employ non-negative matrix factorization (NMF) to predict future distribution of resources. Given the predicted resource distribution, idle agents are directed towards a random nearby location, weighted by this predicted resource distribution. To find a balance between exploration (searching for better locations) and exploitation (remaining in the current location), we use a parameter k that specifies the notion of “nearby”, and another parameter tau to weight nearby locations based on their distance. A third parameter, Delta, defines the time horizon to look for future resource distributions. Finally, a fourth parameter #features defines the number of latent features used in the matrix factorization to model the spatiotemporal resource prediction. All four parameters were optimized on the training data through experimentation.  

Through empirical analysis we found two matrices whose size is relatively small but big enough to yield good performance. We tackled the following challenges:
- Learning spatiotemporal resource distribution
- Predicting future resources
- Avoiding herding
- Avoiding unnecessary long distance trips

Main features of our approach are summarized as follows:
- To avoid herding, each agent has their own random number generator with unique seed number, i.e. agent id. This is to ensure that given the same data, agents  make diverse decision to avoid overfitting.
- To avoid long distance trips, we distribute agents considering distance from each resource.
- Through empirical analysis, we found satisfactory settings such as time horizon for prediction of future resources.


## Structure of project

Besides original COMSET, we added source code and resources as follows:
- src/Extension/
- model/


## How to compile and build a jar file

Since COMSET is a maven project, use maven to compile the project with the following command.
```
mvn org.apache.maven.plugins:maven-compiler-plugin:3.1:compile org.apache.maven.plugins:maven-assembly-plugin:3.1.0:single
```
It will generate `COMSET-1.0-jar-with-dependencies.jar` in directory `target`. It will include all dependencies. Note that a created jar file does not include any resources such as maps, datasets, and models. Therefore, when you run simulation, make sure that all resources are on the same path.


## How to run simulation

Prerequisite: 
1. Set `comset.agent_class` to `Extension.CSTS` in the configuration file (etc/config.properties).
```
comset.agent_class = Extension.CSTS
```
2. Make sure that two files `H6.txt` and `W6.txt` in directory `model` exist.

If the prerequisite is satisfied, simply run `COMSET-1.0-jar-with-dependencies.jar` with java command as follows:
```
java -jar COMSET-1.0-jar-with-dependencies.jar
```
For your convenience, executable files, i.e., `run.bat` and `run.sh`, are available. We assume that the jar file, `COMSET-1.0-jar-with-dependencies.jar`, exists in directory `target`.


## How to build a model


1. Download [New York TLC Trip Record](https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page). Note that building a model only works with the data before July of 2016.
2. Install Python. We employ Python libraries to build a model. Therefore, you will need a [Python](https://www.python.org/) interpreter and required libraries, including [numpy](https://numpy.org/) and [sklearn](https://scikit-learn.org/stable/).
3. Set a period. The default period is between `2016-01-01T00:00:00` and `2016-07-01T00:00:00`. A period should be at least one week. To determine a period, set `temporal.temporalModelStartDatetime` and `temporal.temporalModelEndDatetime` with [LocalDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html) in the configuration file (etc/config.properties). For instance,

```
temporal.temporalModelStartDatetime = 2015-07-01T00:00:00
temporal.temporalModelStartDatetime = 2016-07-01T00:00:00
```


Learning consists of two steps: 1) generating raw matrix (`raw_matrix.txt`), and 2) non-negative matrix factorization (e.g., `H6.txt` and `W6.txt`).
In order to build a model, you need to add data paths that will be used for learning in the configuration file by setting property `mf.learningData` as follows:

```
mf.learningData = NewYorkTLC/yellow_tripdata_2016-01.csv
mf.learningData = NewYorkTLC/yellow_tripdata_2016-02.csv
```

`ModelBuilder` has a main function that takes arguments as follows:

```
<arguments> ::= <argument> | <arguments>
<argument> ::= " -c " <command> | " -config " <configuration-file-path> | " -python " <python-path>
<command> ::= "generate_test_data" | "generate_matrix" | "factorization" 
```

Examples:

```
java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c generate_matrix -config etc/config.properties
java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c factorization -config etc/config.properties
java -cp COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c factorization -config etc/config.properties -python C:/Python37/python
```

For your convenience, executable files, i.e., `traning.bat` and `traning.sh`, are available, which runs `generate_matrix` followed by `factorization`. If Python path is not set in your environment (i.e., you cannot run `python` in the command-line interface), you should set Python path manually using argument `-python` (see the above example). We assume that the jar file, `COMSET-1.0-jar-with-dependencies.jar`, exists in directory `target`.

By default, the program will generate 10 pairs of non-negative matrices that varies the number of components in directory `model`. If you want to change the setting, you may modify `model/NMF.py`.


## Resources


Project Website: [https://sites.google.com/view/dsaa-2020](https://sites.google.com/view/dsaa-2020)




