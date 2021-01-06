### Hazelcast IMDG cluster with Dynamic discovery and Persistence


### Notes

Tried to run IT processes with gradle, no joy.
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