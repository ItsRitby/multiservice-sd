# multiservice-sd

Distributed web service built with Spring Boot 4.1.1 and Java 21, deployed across three Ubuntu virtual machines. It exposes three domains — a calculator, an NFS CSV reader, and a MySQL CRUD — behind an NGINX load balancer, with three replicated application containers.

## Architecture

```
                    ┌──────────────────────┐
                    │   Cliente / Postman  │
                    └──────────┬───────────┘
                               │ HTTP :80
                               ▼
                    ┌──────────────────────┐
                    │  mvBalancer (NGINX)  │
                    │  Contenedor Docker   │
                    └──────────┬───────────┘
                               │ least_conn
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
        :8081            :8082            :8083
     ┌─────────┐      ┌─────────┐      ┌─────────┐
     │ web-1   │      │ web-2   │      │ web-3   │  mvServer
     └────┬────┘      └────┬────┘      └────┬────┘
          │                │                │
          └────────────────┼────────────────┘
                           │
                ┌──────────┴──────────┐
                ▼                     ▼
          NFS :2049            MySQL :3306
          people.csv           appdb.person
                └──────────┬──────────┘
                           ▼
                  ┌──────────────────────┐
                  │  mvDataBase (Ubuntu) │
                  └──────────────────────┘
```

## Virtual Machines

| VM | Role | Contents |
|---|---|---|
| **mvDataBase** | Data server | NFS export of `people.csv` + MySQL container |
| **mvServer** | Application node | 3 Docker containers running the JAR |
| **mvBalancer** | Load balancer | NGINX container on port 80 |

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA (Hibernate)
- MySQL 8.4 (containerized)
- HikariCP
- Lombok
- Docker + Docker Compose
- NGINX 1.27 Alpine
- NFS (nfs-kernel-server)

## Endpoints

### Calculator — `/api/calculator`

| Method | Path | Params | Description |
|---|---|---|---|
| `GET` | `/api/calculator` | `num1`, `num2`, `op` | Performs an arithmetic operation. `op` ∈ `add`, `sub`, `mult`, `div`. |

Example:
```
GET /api/calculator?num1=5&num2=3&op=add
```

### NFS CSV — `/api/nfs`

| Method | Path | Params | Description |
|---|---|---|---|
| `GET` | `/api/nfs` | `page` | Paginated list of persons read from the CSV. Fixed page size of 100. |
| `GET` | `/api/nfs/{id}` | — | Finds a single person by id using binary search over the index. |

### MySQL CRUD — `/api/db`

| Method | Path | Params | Description |
|---|---|---|---|
| `GET` | `/api/db` | `page` | Paginated list of persons from MySQL. Fixed page size of 100. |
| `GET` | `/api/db/{id}` | — | Finds a single person by id. |
| `POST` | `/api/db` | — | Updates a person. Body must be a JSON with `id`, `firstName`, `middleName`, `lastName1`, `lastName2`. |

## Response format

Every response is JSON and carries the node identity that answered the request:

```json
{
  "vmHostname": "mvServer1",
  "containerName": "webservice-1",
  "pageNumber": 0,
  "pageSize": 100,
  "totalPages": 100000,
  "totalRecords": 10000000,
  "hasNext": true,
  "persons": [ ... ],
  "message": "Hola, este es un valor quemado"
}
```

Paginated responses (`/api/nfs`, `/api/db`) and the calculator response include a hardcoded `message` field. It is defined in `src/main/java/co/edu/uptc/sd/multiservice/util/Messages.java` and can only be changed from code.

## Error codes

| Range | Domain | HTTP status |
|---|---|---|
| `0XX` | Calculator | `400 Bad Request` |
| `1XX` | NFS CSV | `400`, `404`, `500` depending on the case |
| `2XX` | Database | `400`, `404`, `500` depending on the case |

Example error response:

```json
{
  "errorCode": "001",
  "message": "Division by zero is not allowed.",
  "vmName": "mvServer1",
  "containerName": "webservice-1"
}
```

## Project structure

```
src/main/java/co/edu/uptc/sd/multiservice/
├── MultiserviceApplication.java
├── controller/
│   ├── CalculatorController.java
│   ├── NfsController.java
│   └── DbController.java
├── service/
│   ├── CalculatorService.java
│   ├── NfsCsvService.java
│   ├── DbPersonService.java
│   └── index/
│       └── CsvIndex.java
├── repository/
│   └── PersonRepository.java
├── entity/
│   └── PersonEntity.java
├── dto/
│   ├── CalculatorResponseDTO.java
│   ├── ErrorResponseDTO.java
│   ├── PersonDTO.java
│   ├── PersonPageResponseDTO.java
│   ├── PersonResponseDTO.java
│   └── PersonUpdateRequestDTO.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── custom/
│       ├── calculator/
│       ├── file/
│       └── db/
└── util/
    ├── NodeIdentifier.java
    └── Messages.java
```

## Configuration

All tunables are environment variables with sensible defaults in `application.properties`.

| Variable | Default | Purpose |
|---|---|---|
| `VM_HOSTNAME` | `unknown-vm` | VM name reported in every response |
| `CONTAINER_NAME` | `unknown-container` | Container name reported in every response |
| `CSV_PATH` | `/data/people.csv` | NFS-mounted CSV path inside the container |
| `PAGE_SIZE` | `100` | Page size for NFS pagination |
| `DB_PAGE_SIZE` | `100` | Page size for DB pagination |
| `DB_URL` | `jdbc:mysql://mvDataBase:3306/appdb` | MySQL JDBC URL |
| `DB_USER` | `appuser` | MySQL user |
| `DB_PASSWORD` | `apppass` | MySQL password |

## CSV layout

`people.csv` is expected to be sorted by `id` and contain one record per line:

```
id,first_name,middle_name,last_name1,last_name2
1,Diego,Andres,Soto,Fuya
2,Ana,Maria,Perez,Gomez
...
```

The NFS service builds an in-memory index of byte offsets at startup (one entry every 100 lines) to answer any page in O(1) and to support binary search by id.

## How to run

### Requirements

- Docker Engine 24+
- Docker Compose v2
- NFS client (`nfs-common`) on the machine that runs the containers
- An NFS server exposing the CSV
- A MySQL instance with the `appdb` schema and the `person` table

### First deployment

```bash
git clone https://github.com/ItsRitby/multiservice-sd.git /home/ritby/webservices
cd /home/ritby/webservices
nano .env    # set VM_HOSTNAME, DB_HOST, DB_NAME, DB_USER, DB_PASSWORD
chmod 600 .env
docker compose up -d --build
```

### Normal deployment after a code change

```bash
cd /home/ritby/webservices
git pull
docker compose up -d --build
```

### Restart only (no code change)

```bash
cd /home/ritby/webservices
docker compose up -d
```

### Check status

```bash
docker compose ps
docker compose logs -f webservice-1
```

## Database schema

```sql
CREATE TABLE person (
    id           INT          NOT NULL PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    middle_name  VARCHAR(100),
    last_name1   VARCHAR(100) NOT NULL,
    last_name2   VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

The table is populated with 10,000,000 rows via `LOAD DATA INFILE`. The IDs are dense (1 to 10,000,000), which lets the service use `MAX(id)` as the total row count in O(1) instead of `COUNT(*)`, and page by `id BETWEEN ? AND ?` instead of `LIMIT/OFFSET`.

## Design rules

- One single Spring Boot project, three controllers, one per domain.
- All data crossing the API boundary goes through DTOs.
- Every response includes the `vmHostname` and `containerName` that answered.
- All list endpoints are paginated. No unbounded lists.
- Private fields with getters/setters (Clean Code).
- No overengineering. No speculative abstractions.
- Errors are modeled by a custom exception hierarchy per domain and translated by a global `@RestControllerAdvice`.

## License

Academic project for the Distributed Systems course at UPTC.
