# Cassandra - create keyspace external channel
CREATE KEYSPACE external_channel WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;
