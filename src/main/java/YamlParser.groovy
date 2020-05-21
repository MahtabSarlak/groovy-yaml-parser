import groovy.json.JsonOutput
import groovy.sql.Sql
import groovy.yaml.YamlSlurper

class YamlParser {
    private PathGenerator pathGenerator
    private DefinitionGenerator definitionGenerator
    private Sql sql
    private File yamlFile

    public YamlParser(Sql sql, String address) {
        this.pathGenerator = new PathGenerator()
        this.definitionGenerator = new DefinitionGenerator()
        this.sql = sql
        this.yamlFile = new File(address)
    }

    public void parse() {
        def yaml = readYamlFile()
        def models = yaml.definitions
        def paths = yaml.paths
        pathGenerator.parse(sql, paths)
        definitionGenerator.parse(sql, models)
        sql.close()
    }

    private readYamlFile() {
        yamlFile.withReader { reader ->
            def yaml = new YamlSlurper().parse(reader)
            return yaml
        }
    }
}
