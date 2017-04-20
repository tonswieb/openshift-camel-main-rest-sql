package nl.finalist.quickstarts;

import static java.lang.System.getenv;

import java.sql.Connection;

import org.apache.camel.main.Main;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class CamelMain {

	private Main main;
	
	public static void main(String[] args) throws Exception {
		CamelMain camelMain = new CamelMain();
		camelMain.boot();
	}

    public void boot() throws Exception {
    	
    	MysqlDataSource datasource = new MysqlDataSource();
    	datasource.setServerName(getenv("MYSQL_SERVICE_NAME"));
    	datasource.setDatabaseName(getenv("MYSQL_SERVICE_DATABASE"));
    	datasource.setUser(getenv("MYSQL_SERVICE_USERNAME"));
    	datasource.setPassword(getenv("MYSQL_SERVICE_PASSWORD"));
    	
    	//Create Database
    	Connection conn = datasource.getConnection();
    	conn.prepareStatement("drop table if exists orders").execute();
    	conn.prepareStatement("create table orders (id integer primary key, item varchar(10), amount integer, description varchar(30), processed boolean);").execute();
    	conn.close();
    	
    	// create a Main instance
        main = new Main();
        // bind MyBean into the registry
        main.bind("orderService", new OrderService());
        main.bind("dataSource", datasource);
        // add routes
        main.addRouteBuilder(new Application());
        // add event listener
//        main.addMainListener(new Events());
        // set the properties from a file
//        main.setPropertyPlaceholderLocations("example.properties");
        // run until you terminate the JVM
        main.run();
    }
}
