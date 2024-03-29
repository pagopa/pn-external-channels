# MOCK CONSOLIDATORE

## REQUIREMENTS

### SQS
pn-mockconsolidatore-dummyqueue

### Parameter Store
MapExternalChannelMockSequence

```
[
  {
    "sequenceName":"OK_RS",
    "sequence":"@sequence.5s-CON080.5s-RECRS001C"
  },
  {
    "sequenceName":"FAIL_RS",
    "sequence":"@sequence.5s-CON080.5s-RECRS002A.5s-RECRS002B[DOC:Plico].5s-RECRS002C"
  },
  {
    "sequenceName":"OK-Retry_RS",
    "sequence":"@sequence.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS001C"
  },
  {
    "sequenceName":"OK_AR",
    "sequence":"@sequence.5s-CON080.5s-RECRN001A.5s-RECRN001B[DOC:AR;DELAY:1s].5s-RECRN001C"
  },
  {
    "sequenceName":"FAIL_AR",
    "sequence":"@sequence.5s-CON080.5s-RECRN002A.5s-RECRN002B[DOC:Plico].5s-RECRN002C"
  },
  {
    "sequenceName":"FAIL-Discovery_AR",
    "sequence":"@sequence.5s-CON080.5s-RECRN002D[DISCOVERY].5s-RECRN002E[DOC:Plico;DOC:Indagine].5s-RECRN002F@discovered.5s-CON080.5s-RECRN001A.5s-RECRN001B[DOC:AR].5s-RECRN001C"
  },
  {
    "sequenceName":"OK_890",
    "sequence":"@sequence.5s-CON080.5s-RECAG001A.5s-RECAG001B[DOC:23L].5s-RECAG001C"
  },
  {
    "sequenceName":"FAIL_890",
    "sequence":"@sequence.5s-CON080.5s-RECAG003A.5s-RECAG003B[DOC:23L;DOC:CAN].5s-RECAG003C"
  },
  {
    "sequenceName":"FAIL-Discovery_890",
    "sequence":"@sequence.5s-CON080.5s-RECAG003D[DISCOVERY].5s-RECAG003E[DOC:Plico;DOC:Indagine].5s-RECAG003F@discovered.5s-CON080.5s-RECAG001A.5s-RECAG001B[DOC:23L].5s-RECAG001C"
  },
  {
    "sequenceName":"OK_RIS",
    "sequence":"@sequence.5s-CON080.5s-RECRSI001.5s-RECRSI002.5s-RECRSI003C"
  },
  {
    "sequenceName":"FAIL_RIS",
    "sequence":"@sequence.5s-CON080.5s-RECRSI001.5s-RECRSI002.5s-RECRSI004A.5s-RECRSI004B[DOC:Plico].5s-RECRSI004C"
  },
  {
    "sequenceName":"OK_RIR",
    "sequence":"@sequence.5s-CON080.5s-RECRI001.5s-RECRI002.5s-RECRI003A.5s-RECRI003B[DOC:AR].5s-RECRI003C"
  },
  {
    "sequenceName":"FAIL_RIR",
    "sequence":"@sequence.5s-CON080.5s-RECRI001.5s-RECRI002.5s-RECRI004A.5s-RECRI004B[DOC:Plico].5s-RECRI004C"
  }
]
```

MapExternalChannelMockServiceIdEndpoint

```
[
    {
            "serviceId":"pn-cons-000",
            "endpoint":"http://localhost:8080",
            "endpointServiceId":"pn-extchannel-000"
    },
    {
            "serviceId":"pn-cons-001",
            "endpoint":"http://localhost:8081",
            "endpointServiceId":"pn-extchannel-001"
    }
]
```

### Secrets

pn-ExternalChannels-Secrets

```
ExternalChannelApiKey;[]
```

## INSTALLATION

Use the `release-<env>.sh` script to release to the target enviroment