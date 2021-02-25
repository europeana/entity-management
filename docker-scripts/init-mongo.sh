mongo -- "$MONGO_INITDB_DATABASE" <<EOF
    var rootUser = '$MONGO_INITDB_ROOT_USERNAME';
    var rootPassword = '$MONGO_INITDB_ROOT_PASSWORD';

    db.auth(rootUser, rootPassword);

    var user = '$EM_DB_USER';
    var passwd = '$EM_DB_PASSWORD';
    db.createUser(
    {
        user: 'api_user',
        pwd: 'password',
        roles: [
            { role: 'readWrite', db: 'EM_APP_DB' },
            { role: 'readWrite', db: 'EM_BATCH_DB' }
            ],
    },
);

db.getSiblingDB('entity-management').createCollection('temp');
db.getSiblingDB('job-repository').createCollection('temp');

EOF