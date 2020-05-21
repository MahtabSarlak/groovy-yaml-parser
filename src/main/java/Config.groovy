import groovy.sql.Sql

class Config {

    private static def dbUrl = "jdbc:postgresql://localhost/dbtest"
    private static def dbUser = "testuser"
    private static def dbPassword = "test"
    private static def dbDriver = "org.postgresql.Driver"
    private static def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)

    public static getSql() {
        return sql
    }
}
