import groovy.yaml.YamlSlurper
import groovy.json.JsonOutput;
import groovy.sql.Sql


class GroovyTest {

    static def yamlFile = new File("example.yml")
    static def dbUrl = "jdbc:postgresql://localhost/dbtest"
    static def dbUser = "testuser"
    static def dbPassword = "test"
    static def dbDriver = "org.postgresql.Driver"
    static def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)

    static void main(args) {
        //sql configuration
        dbInitialization()
        // yaml reader
        yamlFile.withReader { reader ->
            def yaml = new YamlSlurper().parse(reader)
            def json = JsonOutput.toJson(yaml)
            println JsonOutput.prettyPrint(json)
            def models = yaml.definitions
            generateModel(models)
            def paths = yaml.paths
            generatePath(paths)
            sql.close()
        }
    }
//  sql configuration
    static void dbInitialization() {
        sql.execute("DROP TABLE IF EXISTS model cascade;")
        sql.execute("CREATE TABLE model (model_id serial NOT NULL,name varchar(100) NOT NULL," +
                "type varchar(100) NOT NULL,xml_name varchar(100)  NULL," +
                "CONSTRAINT PK_model PRIMARY KEY ( model_id));")
        sql.execute("DROP TABLE IF EXISTS propertiestb ;")
        sql.execute("CREATE TABLE propertiestb(propertiestb_id serial NOT NULL," +
                "name varchar(100) NOT NULL,type varchar(100) NULL,format varchar(100) NULL," +
                "description varchar(100) NULL,isDefault varchar(100) NULL,example varchar(100) NULL," +
                "ref varchar(100) NULL,enum varchar(100) NULL,xml_name varchar(100) NULL, xml_wrapped varchar(100) NULL,items varchar(100) NULL ,model_id integer NOT NULL," +
                "CONSTRAINT PK_propertiestb PRIMARY KEY ( propertiestb_id)," +
                "CONSTRAINT FK_21 FOREIGN KEY ( model_id) REFERENCES model ( model_id));")
        sql.execute("DROP TABLE IF EXISTS path cascade;")
        sql.execute(
                "CREATE TABLE path(" +
                        " path_id  serial NOT NULL," +
                        " url     varchar(50) NOT NULL," +
                        " CONSTRAINT PK_path PRIMARY KEY (path_id));")
        sql.execute("DROP TABLE IF EXISTS api ;")
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
                        " format     varchar(50) NULL," +
                        " api_id    integer   NOT NULL," +
                        " CONSTRAINT PK_parameters PRIMARY KEY (parameters_id)," +
                        " CONSTRAINT FK_34 FOREIGN KEY (api_id) REFERENCES api (api_id));")

    }
// parse path data
    static void generatePath(paths) {
        for (path in paths) {
            sql.executeInsert(" INSERT INTO path ( URL) VALUES (?) ", [path.key]);
            for (item in path.getValue()) {
                def first = sql.firstRow("SELECT path_id FROM path where path.url=" + "'" + path.key + "'")
                int path_id = first.path_id

                sql.executeInsert(" INSERT INTO api ( type ,tags , summary ,description , operation_id ,consumes , produces , path_id) VALUES (?,? , ?,?,?,? ,? , ?) ",
                        [item.getKey(), item.getValue().tags[0], item.getValue().summary, item.getValue().description, item.getValue().operationId, item.getValue().consumes.toString(), item.getValue().produces.toString(), path_id]);
            }

        }
    }
// parse model data
    static def generateModel(models) {
        for (model in models) {
            if (model.getValue().xml != null) {
                sql.executeInsert(" INSERT INTO MODEL ( name , type , xml_name) VALUES (?,?,?) ", [model.key, model.getValue().type, model.getValue().xml.name]);
            } else {
                sql.executeInsert(" INSERT INTO MODEL ( name , type ) VALUES (?,?) ", [model.key, model.getValue().type]);
            }
            for (property in model.value.properties) {
                def first = sql.firstRow("SELECT model_id FROM model where model.name=" + "'" + model.key + "'")
                int model_id = first.model_id
                if (property.getValue().xml == null) {
                    sql.executeInsert(" INSERT INTO propertiestb (name , type  , format , description , isDefault , example , ref , enum  ,items, model_id) VALUES (? ,?, ?,? ,?,?,?,?,?,?) ",
                            [property.key, property.getValue().type, property.getValue().format, property.getValue().description, property.getValue().default, property.getValue().example,
                             property.getValue().$ref, property.getValue().enum.toString(), property.getValue().items.toString(), model_id])
                } else {
                    sql.executeInsert(" INSERT INTO propertiestb (name , type  , format , description , isDefault , example , ref , xml_name , xml_wrapped, enum ,items , model_id) VALUES (? ,?,?, ?,? ,?,? ,?,?,?,?,?) ",
                            [property.key, property.getValue().type, property.getValue().format, property.getValue().description, property.getValue().default, property.getValue().example,
                             property.getValue().$ref, property.getValue().xml.name, property.getValue().xml.wrapped, property.getValue().enum.toString(), property.getValue().items.toString(), model_id])
                }
            }
        }
    }
}


