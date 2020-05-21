import groovy.sql.Sql

class PathGenerator {
    public parse(Sql sql , def paths) {
        dbInitialization(sql)

        for (path in paths) {
            sql.executeInsert(" INSERT INTO path ( URL) VALUES (?) ", [path.key]);
            for (item in path.getValue()) {
                def first = sql.firstRow("SELECT path_id FROM path where path.url=" + "'" + path.key + "'")
                int path_id = first.path_id

                sql.executeInsert(" INSERT INTO api ( type ,tags , summary ,description , operation_id ,consumes , produces ,security, path_id) VALUES (?,?,? , ?,?,?,? ,? , ?) ",
                        [item.getKey(), item.getValue().tags[0], item.getValue().summary, item.getValue().description, item.getValue().operationId, item.getValue().consumes.toString(), item.getValue().produces.toString(), item.getValue().security.toString(), path_id]);

                def second = sql.firstRow("SELECT api_id FROM api where api.path_id=" + " '" + path_id + "' " + "and api.type=" + "'" + item.getKey() + "'");
                int api_id = second.api_id
                for (res in item.getValue().responses) {
                    if (res != null) {
                        sql.executeInsert(" INSERT INTO response (status , description , api_id) VALUES (? , ? ,?)",
                                [res.getKey(), item.getValue().description, api_id]);
                    } else {
                        sql.executeInsert(" INSERT INTO parameters (api_id) VALUES (?)",
                                [api_id]);
                    }
                }
                for (param in item.getValue().parameters) {
                    if (param != null) {
                        sql.executeInsert(" INSERT INTO parameters ( name  , in_value , description , required , type , format ,items , schema , collectionFormat , api_id) VALUES (?,?,?,?,?,?,?,?,?,?)",
                                [param.name, param.in, param.description, param.required, param.type, param.format, param.items.toString(), param.schema.toString(), param.collectionFormat, api_id]);
                    } else {
                        sql.executeInsert(" INSERT INTO parameters (api_id) VALUES (?)",
                                [api_id]);
                    }
                }

            }
        }
    }
    //  sql configuration
    private void dbInitialization(Sql sql) {
        sql.execute("DROP TABLE IF EXISTS path cascade;")
        sql.execute(
                "CREATE TABLE path(" +
                        " path_id  serial NOT NULL," +
                        " url     varchar(50) NOT NULL," +
                        " CONSTRAINT PK_path PRIMARY KEY (path_id));")
        sql.execute("DROP TABLE IF EXISTS api cascade;")
        sql.execute(
                "CREATE TABLE api (" +
                        " api_id serial NOT NULL," +
                        " type   varchar(50) NULL," +
                        " tags        varchar(100) NULL," +
                        " summary      varchar(200) NULL," +
                        " description  varchar(200) NULL," +
                        " operation_id varchar(100) NULL," +
                        " consumes     varchar(100) NULL," +
                        " produces     varchar(100) NULL," +
                        " security     varchar(100) NULL," +
                        " path_id    integer   NOT NULL," +
                        " CONSTRAINT PK_api PRIMARY KEY (api_id)," +
                        " CONSTRAINT FK_44 FOREIGN KEY (path_id) REFERENCES path (path_id));")
        sql.execute("DROP TABLE IF EXISTS parameters ;")
        sql.execute(
                "CREATE TABLE parameters (" +
                        " parameters_id serial NOT NULL," +
                        " name   varchar(50) NULL," +
                        " in_value        varchar(100) NULL," +
                        " description  varchar(200) NULL," +
                        " required varchar(50) NULL," +
                        " type     varchar(50) NULL," +
                        " items     varchar(200) NULL," +
                        " schema     varchar(200) NULL," +
                        " collectionFormat     varchar(100) NULL," +
                        " format     varchar(50) NULL," +
                        " api_id    integer   NOT NULL," +
                        " CONSTRAINT PK_parameters PRIMARY KEY (parameters_id)," +
                        " CONSTRAINT FK_34 FOREIGN KEY (api_id) REFERENCES api (api_id));")
        sql.execute("DROP TABLE IF EXISTS response ;")
        sql.execute(
                "CREATE TABLE response (" +
                        " response_id serial NOT NULL," +
                        " status   varchar(50) NULL," +
                        " description  varchar(200) NULL," +
                        " api_id    integer   NOT NULL," +
                        " CONSTRAINT PK_response PRIMARY KEY (response_id)," +
                        " CONSTRAINT FK_34 FOREIGN KEY (api_id) REFERENCES api (api_id));")

    }

}
