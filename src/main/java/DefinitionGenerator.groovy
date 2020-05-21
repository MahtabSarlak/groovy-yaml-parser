import groovy.sql.Sql

class DefinitionGenerator {
    public parse(Sql sql , def models){
        dbInitialization(sql)
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
    private void dbInitialization(Sql sql) {
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

    }
}
