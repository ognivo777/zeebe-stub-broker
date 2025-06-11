# zeebe-stub-broker

**zeebe-stub-broker** is a lightweight emulator of a [Zeebe broker](https://docs.camunda.io/docs/components/zeebe/zeebe-overview/),
designed for testing and validating [Zeebe workers](https://docs.camunda.io/docs/components/concepts/job-workers/). 
It enables easy functional verification of new workers and supports load testing to evaluate performance under stress.

Built using [Quarkus](https://quarkus.io/performance/) for high performance and responsiveness.

# 🧪 Use Cases
* ⚙️ Quick validation of newly developed Zeebe workers.
* 🔄 Performance and high load testing of worker throughput.
* 🧰 Local development tool for simulating Zeebe job dispatching.

# 🔧 Features
* Accepts jobs via a simple REST API.
* Asynchronously delivers jobs to registered Zeebe workers.
* Queues jobs if the worker is busy (with configurable limits).
* Optional synchronous mode to get immediate worker responses.
* Provides real-time job submission status and queue info.

# 🛠 Build Instructions
To build the project, make sure you have **Java 21** installed. Then, clone the repository and build it using Gradle:
```shell
./gradlew qurkusBuild
```
The resulting uber JAR will be located at: 
```shell
./build/quarkus-build/gen
```

# 🚀 Usage

## Run broker stub
Pick `*.jar` from last release and run with java:
```shell
java -jar broker-stub-to-run-workers-1.0-SNAPSHOT-runner.jar
```

## Submit Job via REST
**Endpoint:**
`PUT http://localhost:8080/payload/{worker-name}`

**Body:**
JSON containing the Zeebe context variables.

**Example Request:**
```http
PUT /payload/my-worker HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "someVar": "some value"
}
```

*Example Response (Async mode, default):*
```json
{
  "jobRequest": {
    "number": 135574,
    "variablesJson": "{\"someVar\":\"some value\"}",
    "worker": "my-worker"
  },
  "queued": false,
  "spaceLeft": 500,
  "success": true
}
```

### Synchronous Response Mode
You can use `?async=false` to wait for the worker to complete and return its response immediately:
```http
PUT /payload/my-worker?async=false HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "someVar": "some value"
}
```

```json
{
  "jobKey": 135575,
  "variables": "{\"Message\":\"Fast worker response!\"}"
}
```
