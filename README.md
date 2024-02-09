# Account transactions API

### Requirements
- Java 21
- Docker

### Missing
- Business exceptions are not translated to view exception, which can further be mapped to appropriate http response codes.

### Design Considerations
- Amount fields support only till 2 decimal places at the moment.
- Added `getCustomers` & `getAuditLogs`
- 4 DB tables: tenant(contains tenant) -> customer_tenant(contains tenant+customers) -> transactions & audit_logs.
- Would have been better to create Account table between customer_tenant & transactions.
- Final account balance maintained in `account_balance` column of customer_tenant.
- Every transaction performed or rollbacked recalculates `account_balance`
- Rollback transaction internally has multiple steps:
  - Mark transaction as `rolling_back` if transaction is in `active` state.
  - Create new transaction which has negated `amount` of transaction to be rolled back.
  - Mark transaction as `rolled_back`.
- If a transaction is already `rolled_back`, it cannot be rolled back again.
- Used artemis spring jms library to simulate an embedded(Ideally, the queue should be out of the application) JMS queue
- No check present on lower bound of account_balance.
- E2EIntegrationTest.java is an integration test to verify transaction operations. It is performed over test database.
- Liquibase changelog contains test data sql as well(only gets executed for tests). Ideally it should have been in test directories(, but was causing some issues).
- Audit logs have data as json formatted string

### Steps to run(`The steps have been tried on windows powershell and may require changes if executed on unix`)
- Run command `docker-compose down`
- Run command `docker-compose up -d`
- Build application using command
  `./mvnw clean package`
- Go to target directory by running command `cd target`
- run `java --enable-preview -jar account-balance-service-0.0.1-SNAPSHOT.jar`. This should start the application
- Open browser of your choice and go to URL `http://localhost:8080/`
- The web page will have the supported operations.

<b>NOTE</b>: the web page is built to support only happy path. To verify other scenarios, use curls mentioned below.

### Steps to build docker image
- run `docker build -t account-balance-service:1.0 .` from within the project root directory

P.S. - Working on windows system so cannot test any build/shell script, hence provided individual steps


### REST API
NOTE: In case the application is running on a different host and port, replace `localhost:8080` with correct `<host>:<port>`
Swagger: `localhost:8080/swagger-ui/index.html` (only accessible once the project is running)

### Perform Transaction
#### Request
`POST /api/v1/transactions`

    curl --location 'localhost:8080/api/v1/transactions' \
      --header 'Content-Type: application/json' \
      --data '{
        "service": "sample-service",
        "amount": 85,
        "type": "PAY_IN",
        "customer": {
          "customerId": 1150,
          "tenant": "enterprise-all-inclusive.com"
        }
      }'

#### Response
    {
      "id": 4,
      "service": "sample-service",
      "amount": 85,
      "customer": {
        "id": 22002,
        "customerId": 1150,
        "tenant": {
          "id": 1,
          "name": "enterprise-all-inclusive.com"
        },
        "accountBalance": 85.00
      },
      "state": "ACTIVE",
      "createdAt": "2024-02-08T16:19:22.806Z"
    }

### Get transactions sorted desc by created at
#### Request
`GET /api/v1/transactions`

    curl --location 'localhost:8080/api/v1/transactions?customerId=1150&tenant=enterprise-all-inclusive.com&count=10'

#### Response
    [
      {
        "id": 5,
        "service": "sample-service",
        "amount": 85.00,
        "customer": {
          "id": 22002,
          "customerId": 1150,
          "tenant": {
            "id": 1,
            "name": "enterprise-all-inclusive.com"
          },
          "accountBalance": 170.00
        },
        "state": "ACTIVE",
        "createdAt": "2024-02-08T16:24:15.674Z"
      },
      {
        "id": 4,
        "service": "sample-service",
        "amount": 85.00,
        "customer": {
          "id": 22002,
          "customerId": 1150,
          "tenant": {
            "id": 1,
            "name": "enterprise-all-inclusive.com"
          },
          "accountBalance": 85.00
        },
        "state": "ACTIVE",
        "createdAt": "2024-02-08T16:19:22.806Z"
      }
    ]

### Perform Transaction rollback
#### Request
`POST /api/v1/transactions/{transactionId}/rollback`

    curl --location 'localhost:8080/api/v1/transactions/5/rollback' \
      --header 'Content-Type: application/json' \
      --data '{
        "customer": {
          "customerId": 1150,
          "tenant": "enterprise-all-inclusive.com"
        }
      }'

#### Response
    Http 204

### Get audit logs sorted desc by performed at
#### Request
`GET /api/v1/audit-logs`

    curl --location 'localhost:8080/api/v1/audit-logs?count=2'

#### Response
    [
      {
        "id": 2,
        "operationName": "POST_TRANSACTION",
        "data": "{\"id\":2,\"service\":\"demo-service\",\"amount\":-45}",
        "customer": {
          "id": 22003,
          "customerId": 1170,
          "tenant": {
            "id": 2,
            "name": "betrieb-alles-inklusive.de"
          },
          "accountBalance": 0.00
        },
        "performedAt": "2024-02-08T15:56:15.257Z"
      },
      {
        "id": 1,
        "operationName": "POST_TRANSACTION",
        "data": "{\"id\":1,\"service\":\"test-service\",\"amount\":23}",
        "customer": {
          "id": 22000,
          "customerId": 1100,
          "tenant": {
            "id": 1,
            "name": "enterprise-all-inclusive.com"
          },
          "accountBalance": 123.00
        },
        "performedAt": "2024-02-08T15:47:55.395Z"
      }
    ]

