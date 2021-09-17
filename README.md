# Cassandra - create keyspace external channel
CREATE KEYSPACE external_channel WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};
