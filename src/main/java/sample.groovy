/*@GrabConfig(systemClassLoader=true)
@Grab(group='org.postgresql', module='postgresql', version='9.4-1205-jdbc42')*/

import groovy.yaml.YamlSlurper
import org.yaml.snakeyaml.Yaml
import groovy.json.JsonOutput;
import groovy.sql.Sql



class GroovyTest{
    static void main(args){

        def dbUrl      = "jdbc:postgresql://localhost/dbtest"
        def dbUser     = "testuser"
        def dbPassword = "test"
        def dbDriver   = "org.postgresql.Driver"

        def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
        sql.execute("DROP TABLE IF EXISTS PROJECT;")
        sql.execute "create table PROJECT (ID serial PRIMARY KEY, URL VARCHAR (100))"
        sql.execute("DROP TABLE IF EXISTS MODEL;")
        sql.execute "create table MODEL (ID serial PRIMARY KEY, name VARCHAR (100))"
       /* def ids = sql.executeInsert """
  INSERT INTO PROJECT (NAME, URL) VALUES ('tutorials', 'github.com/eugenp/tutorials')
"""*/

        //println(ids)

        yamlFile.withReader { reader ->
            def yaml = new YamlSlurper().parse(reader)
            def json = JsonOutput.toJson(yaml)
            println("yaml to json")
                println(json)
            def models = yaml.definitions
            println("models to json")
            for (model in models)
            {
                def t = sql.executeInsert (" INSERT INTO MODEL ( name) VALUES (?) " , [model.key]);

                def model_json = JsonOutput.toJson(model)
                println JsonOutput.prettyPrint(model_json)
            }
            println("paths to json")
            def paths = yaml.paths
            for (path in paths)
            {
                println(path.key)

               def t = sql.executeInsert (" INSERT INTO PROJECT ( URL) VALUES (?) " , [path.key]);

                def path_json = JsonOutput.toJson(path)
                println JsonOutput.prettyPrint(path_json)
            }
            sql.close()
        }
    }
    static def yamlFile = new File("example.yaml")

}


