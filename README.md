# Run Cassandra Docker container
```bash
$ docker run --name some-cassandra -d cassandra:3.3
$ docker exec -it some-cassandra ip add
```

# Akka & Docker

For a detailed description read [this blog entry](http://mukis.de/pages/akka-cluster-with-docker-containers/).

```bash
$ sbt docker:publishLocal
$ docker run --name seed-1 messenger:0.0.1 --seed -c <cassandra-ip>:9042
$ docker run --name seed-2 messenger:0.0.1 --seed -c <cassandra-ip>:9042 <ip-of-your-seed-1>:2551
$ docker run --name node-1 messenger:0.0.1 -c <cassandra-ip>:9042 <ip-of-your-seed-1>:2551 <ip-of-your-seed-2>:2551
$ docker run --name node-2 messenger:0.0.1 -c <cassandra-ip>:9042 <ip-of-your-seed-1>:2551 <ip-of-your-seed-2>:2551
```

# SBT - none docker

Of course you can run your cluster within sbt for test purposes.

```
sbt runSeed
sbt runNode
```

# Links and References

* [Akka Docker Cluster Example Blog](http://blog.michaelhamrah.com/2014/03/running-an-akka-cluster-with-docker-containers/)
* [Akka Docker Cluster Example Github](https://github.com/mhamrah/akka-docker-cluster-example)
* [Docker Networking](https://docs.docker.com/articles/networking/)
* [Docker Cheat Sheet](https://github.com/wsargent/docker-cheat-sheet)
* [Docker Env Variables](http://mike-clarke.com/2013/11/docker-links-and-runtime-env-vars/)
* [Docker Ambassador Pattern Linking](http://docs.docker.com/articles/ambassador_pattern_linking/)
