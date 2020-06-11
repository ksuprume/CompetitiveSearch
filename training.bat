@echo off
java -cp target/COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c generate_matrix -config etc/config.properties
java -cp target/COMSET-1.0-jar-with-dependencies.jar Extension.ModelBuilder -c factorization -config etc/config.properties
@pause
