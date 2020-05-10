import groovy.yaml.YamlSlurper
import groovy.json.JsonOutput;
import groovy.sql.Sql



class GroovyTest{

    static def dbUrl      = "jdbc:postgresql://localhost/dbtest"
    static def dbUser     = "testuser"
    static def dbPassword = "test"
    static def dbDriver   = "org.postgresql.Driver"

    static void main(args){
// -------------------------------- sql configuration -----------------------------------------
        def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
        sql.execute("DROP TABLE IF EXISTS PATHS;")
        sql.execute "create table PATHS (ID serial PRIMARY KEY, URL VARCHAR (100))"
        sql.execute("DROP TABLE IF EXISTS MODEL;")
        sql.execute "create table MODEL (ID serial PRIMARY KEY, name VARCHAR (100))"

//------------------------------ yaml reader---------------------------------------------------

        yamlFile.withReader { reader ->
            def yaml = new YamlSlurper().parse(reader)
            def json = JsonOutput.toJson(yaml)
            println("yaml to json")
                println(json)
            def models = yaml.definitions
            println("models to json")
            for (model in models)
            {
               sql.executeInsert (" INSERT INTO MODEL ( name) VALUES (?) " , [model.key]);

                def model_json = JsonOutput.toJson(model)
                println JsonOutput.prettyPrint(model_json)
            }
            println("paths to json")
            def paths = yaml.paths
            for (path in paths)
            {
                println(path.key)

               sql.executeInsert (" INSERT INTO PATHS ( URL) VALUES (?) " , [path.key]);

                def path_json = JsonOutput.toJson(path)
                println JsonOutput.prettyPrint(path_json)
            }
            sql.close()
        }
    }
    static def yamlFile = new File("example.yml")

}


