# dslink-scala-sink
Simple message sink for performance testing on IOT-DSA.
Use with dslink-scala-generator.

# Usage

```shell-session
$ sbt assembly
$ java -jar target/scala-2.12/dslink-scala-sink-assembly-0.1.0-SNAPSHOT.jar <generator dslink base name> <first generator number> <last generator number> <number of nodes> <output directory> --broker https://broker-host:port/conn
```

## Example Usage

Start dslink-scala-sink.

```shell-session
$ java -jar target/scala-2.12/dslink-scala-sink-assembly-0.1.0-SNAPSHOT.jar Generator 1 1 1 ./ --broker https://localhost:8443/conn
```

Start dslink-scala-generator in another terminal.

```shell-session
$ java -jar target/scala-2.12/dslink-scala-generator-assembly-0.1.0-SNAPSHOT.jar 1 2 60 --broker https://localhost:8443/conn
```
