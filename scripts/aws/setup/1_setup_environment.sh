bash create_buckets.sh

bash create_queues.sh

curl --location --request POST 'http://localhost:3000/execute' \
--header 'Content-Type: application/json' \
--data-raw '{
    "statement": "CREATE KEYSPACE external_channel WITH replication = {'\''class'\'': '\''SimpleStrategy'\'', '\''replication_factor'\'': '\''1'\''} AND durable_writes = true;",
    "options": {
        "trace": false,
        "consistency": "one"
    }
}'

read -n 1 -s -r -p "Press any key to continue"