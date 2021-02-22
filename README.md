# Demo Customer Insights API

This API has one endpoint to search for customers customer insights by customer id. 
Current functionality, provides insights on 
- Spend by Category and 
- Bill Tracking, to determine Whether Bills and or Outgoings were higher or lower this month, 
compared with previous months.

### Running, Building, Testing (CLI)
This is a Java 11 project compiled in gradle.
To run the following commands use Gradle or Gradlew equivalent in the CLI

to clean & build the project:
    
    ./gradlew clean build

to run API:
    
    ./gradlew bootRun

to run all tests:s
    
    ./gradlew test --tests 'com.nationwide*'

the server will run on localhost default port 8080


### Assumptions

As inferred from the specification provided, there is no functionality to create a `Customer` or `Transactions` from the API,
As such, there are two SQL scripts which run will run on application launch to create these objects. Not intended as
production ready but for test/demonstration purposes only.

`resources/db/schema.sql`
`resources/db/data.sql`

These scripts initialise and populate the H2 in-memory database with dummy data. 
Running the application will produce log messages of responses to the two GET query

[Search Customer Insights by customer id](#search-by-id)



# search-by-id
Search Customer Insights by customer id


    GET /api/v1/customer/insights/1

| Path variable |  Data type    | Description    |
| :-----------: | :-----------: | :------------: |
| ID            |   Long        |   customer id  |

example:

    http://localhost:8080/api/v1/customer/insights/1
    
### Responses

Responses to queries are in JSON format.
They are also logged, in part at INFO level.
