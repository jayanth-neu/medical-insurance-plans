## Project description
REST API prototype model to handle **large scale** medical insurance plans

## Architecture  
![**Architecture**](https://github.com/jayanth-neu/medical-insurance-plans/blob/main/ArchitectureDiagram.jpg)


## Overview
In this project, we will develop a REST API to parse a JSON schema model
1. Java Spring Boot rest API application validates incoming payloads with JSON schema
2. Performs conditional writes(using etags) to redis key-value store
3. Secured APIs using the RS256 algorithm
4. Search functionality using Elasticsearch
5. Queuing mechanism using RabbitMQ
    
## Pre-requisites
1. Java
2. Maven 
3. Redis Server
4. Elasticsearch and Kibana(Local or cloud-based)

## Build and Run 
Run as Spring Boot Application in any IDE.

## Querying Elasticsearch
1. Run both the application i.e. FinalProject and Consumer Message Queue(CMQ). CMQ application will create the indexes.
2. Run POST query from Postman
3. Run custom search queries as per your use case(Few are present in DemoQueries)

(Optional) For testing purposes - In order to test the indexes separately, Run the PUT query in Testing-ElasticSearchQueries on Kibana. This will create an index in Elasticsearch

## Milestone 1
1. Rest API that can handle any structured data in JSON
Specify URIs, status codes, headers, data model, version
2. Rest API with support for CRD operations
    Post, Get, Delete
3. Rest API with support for validation
    JSON Schema describing the data model for the use case
4. Controller validates incoming payloads against JSON schema
5. The semantics with ReST API operations such as update if not changed/read if changed
    Update not required
    Conditional read is required
6. Storage of data in key/value store
    Must implement the use case provided

## Milestone 2
1. Rest API that can handle any structured data in JSON
2. Rest API with support for crud operations, including merge/Patch support,  delete
3. Rest API with support for validation
4. JSON Schema describing the data model for the use case
5. Advanced semantics with rest API operations such as update if not changed; conditional read and conditional write
6. Storage of data in key/value store
7. Security mechanism must use RS 256

## Milestone 3
1. Rest API that can handle any structured data in JSON
2. Rest API with support for crud operations, including merge support, cascaded delete
3. Rest API with support for validation
4. JSON Schema describing the data model for the use case
5. Advanced semantics with rest API operations such as update if not changed
6. Storage of data in key/value store
7. Search with join using Elastic
8. Parent-Child indexing
    (Patch changes are shown from the Search API)
9. Queueing
10. Security
