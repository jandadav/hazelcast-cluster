## Hazelcast IMDG cluster with Dynamic discovery and Persistence

### Purpose

To provide a POC for implementing caching service under [Zowe/Api Mediation Layer](https://github.com/zowe/api-layer)

### Requirements 

Brief summary of caching service requirements and how Hazelcast fits into each one

#### Embeddability / Number of processes to manage

Can be embedded to springboot app, reducing number of processes to just the caching api.

#### Runs on zOS

Pure java impl, should run without problem. Not tested

#### Data Persistence

Data not persisted, opensource does not provide mechanisms for persistent data.

Handlers can be made and hooked on internal evenr model, that can serialize the data on events (inserted, deleted, .....) and custom serialization logic can be implemented.

#### Discovery

Hazelcast provides native integration with Eureka discovery. The integration works very well and is easy to configure.

#### Native Client

Yes, but with caveat that secure transport is not available. Can be implemented but effort is significant.

#### Security

Opensource solution does not offer TLS security on transport. Alternative solution usin AT-TLS or NGINX/TOMCAT as reverse proxy providing TLS should be possible (not tested)

Custom implementation could be made.

Customizing the nio networking:
Effort is significant. Experimented with customization of Hazelcast instance and the mechanism is done in the repo.

Replacing the networking with blocking secure socket:
Effort unknown

#### Performance

Profiling of memmory vs data should be done to understand capacity.

#### Capacity

As above.

Multiple clients combine heap and extend capacity.

#### Replication

Very fast replication in cluster. Backups are kept so when instance goes down data does not get lost.

#### Spring Integration

Nice integration wiht SpringBoot. Autoconfigure or custom creation can be done with beans. Very Spring like!

#### License

Apache License, Version 2.0

#### Commercial support

Available

## Summary

While some aspects still remain to be tested, Hazelcast seems like solid option that fits on almost all requirements.

The only challenge is securing the transport. Some options are availabe and need to be evaluated.

### Notes

* Run integration tests manually from ide.. test task is disabled as it was running during build. 

* Tried to run IT processes with gradle, no joy.
Exec task and depending on it is synchronous, so no use.
Starting NPM through gradle runs detached and needs a way to kill, plus no logs.
So keeping starting services with `npm run everything` manual for the time being.
```
task startHazelcastNode1(type: Exec) {
    dependsOn getTasksByName('build', true)
    group = "Execution"
    description = "Run Hazelcast node on port 9090"
    commandLine "java ", "-Dserver.port=9090", "-jar", 'hazelcast-node/build/libs/hazelcast-node.jar'
}

task startHazelcastNode2(type: Exec) {
    dependsOn getTasksByName('build', true)
    group = "Execution"
    description = "Run Hazelcast node on port 9091"
    commandLine "java ", "-Dserver.port=9091", "-jar", 'hazelcast-node/build/libs/hazelcast-node.jar'
}

task startHazelcastNode3() {
    dependsOn getTasksByName('build', true)
    doFirst {
        if (System.properties['os.name'].toLowerCase().contains('windows')) {
            ext.process = new ProcessBuilder()
                    .directory(projectDir)
                    .command("npm.cmd", "run", "everything")
                    .start()
        } else {
            ext.process = new ProcessBuilder()
                    .directory(projectDir)
                    .command("npm", "run", "everything")
                    .start()
        }
    }
}
```