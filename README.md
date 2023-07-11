## Project description
REST Api prototype model to handle **large scale** medical insurance plans

## Architecture  
![**Architecture**](https://github.com/SaiChandGhanta/medical-insurance-plans/blob/main/ArchitectureDiagram.jpg)


## Overview
In this project, we will develop a REST Api to parse a JSON schema model
1. Java Spring Boot rest API application validates incoming payloads with JSON schema
2. Performs conditional writes(using etags) to redis key value store
3. Secured APIs using RS256 algorithm
4. Search functionality using Elasticsearch
5. Queuing mechanism using RabbitMQ
    
## Pre-requisites
1. Java
2. Maven 
3. Redis Server
4. Elasticsearch and Kibana(Local or cloud based)

## Build and Run 
Run as Spring Boot Application in any IDE.

## Querying Elasticsearch
1. Run both the application i.e FinalProject and Consumer Message Queue(CMQ). CMQ application will create the indexes.
2. Run POST query from Postman
3. Run custom search queries as per your use case(Few are present in DemoQueries)

(Optional) For testing purpose - Inorder to test the indexes separately, Run the PUT query in Testing-ElasticSearchQueries on Kibana. This will create an index in elasticsearch
