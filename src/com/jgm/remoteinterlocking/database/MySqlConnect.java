package com.jgm.remoteinterlocking.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * A Singleton Database Access Class for a MySQL Database.
 * @author Jonathan Moss
 * @version v1.0 August 2016
 */
public final class MySqlConnect {
    private Connection conn; // The connection object.
    private Statement statement; // The statement object.
    private static MySqlConnect db; // Holds this instance object.
    private static String conString; // A string used to hold the connection string for accessing the database.
    private static String dbHost; // The IP address of the database server.
    private static String dbPort; // The port of the DB Server.
    private static String dbUserName; // The username of the DB user.
    private static String dbName; // The name of the database.
    private static String dbPassword; // The password of the DB user.
    private static final ArrayList <String> DB_CONNECTION_CREDENTIALS  = new ArrayList<>(); // An array to hold the connection credentials. 
    
    /**
     * This method returns the single instance of the initialised class object.
     * @return MysqlConnect Database connection object
     */
    public static synchronized MySqlConnect getDbCon() {
        if ( db == null ) {
            db = new MySqlConnect();
        }
        return db;
    }
    
    /**
     *
     * @param query String The query to be executed
     * @return a ResultSet object containing the results or null if not available
     * @throws SQLException
     */
    public ResultSet query(String query) throws SQLException {
        statement = db.conn.createStatement();
        ResultSet res = statement.executeQuery(query);
        return res;
    }
    
    /**
     * @desc Method to insert data to a table
     * @param insertQuery String The Insert query
     * @return boolean
     * @throws SQLException
     */
    public int insert(String insertQuery) throws SQLException {
        statement = db.conn.createStatement();
        int result = statement.executeUpdate(insertQuery);
        return result;
    }
    
    /**
     * This is the constructor method. It is private to ensure that it is not instantiated from elsewhere.
     */
    private MySqlConnect() {
        if (dbHost == null) { // Check if dbHost has been initialised.
            getDatabaseCredentials(); // If not, get and fill the class variables concerning the database credentials.
        }
        conString = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbName); // The db connection String.
        if (this.conn == null) {
            System.out.print("Attempting to establish a connection with the remote DB...");
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                this.conn = (Connection)DriverManager.getConnection(conString, dbUserName, dbPassword);
                System.out.println(String.format ("%s%s%s", 
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException sqle) {
                System.out.println(String.format ("%s%s [Connection Error]%s", 
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()));
                System.exit(0);
            }
        }
    }
    
    /**
     * This Method reads the DataBase connection details and credentials from dbAccess.txt
     */
    private synchronized static void getDatabaseCredentials() {
        System.out.print("Attempting to get Database Credentials from dbAccess.txt...");
        try {
            // Setup the input stream.
            InputStream dbSetup  = LineSideModule.class.getResourceAsStream("dbAccess.txt");
            BufferedReader reader = new BufferedReader (new InputStreamReader(dbSetup));
            // Declare method variables.
            String line;
            int linesRead = 0;
            // Read the contents of the file.
            while ((line = reader.readLine())!= null) {
                if (linesRead >= 5) {
                    break; // We have reached the limit needed (5 lines).
                } else {
                    DB_CONNECTION_CREDENTIALS.add(line.trim()); // Add the contents of the line into the ArrayList
                    linesRead ++; // Increment the Linesread method variable.
                }
            }
            // Check again that we have the correct number of lines/credentials from the file - apply to class variables.
            if (DB_CONNECTION_CREDENTIALS.size() == 5) {
                dbHost = DB_CONNECTION_CREDENTIALS.get(0);
                dbPort = DB_CONNECTION_CREDENTIALS.get(1);
                dbName = DB_CONNECTION_CREDENTIALS.get(2);
                dbUserName = DB_CONNECTION_CREDENTIALS.get(3);
                dbPassword = DB_CONNECTION_CREDENTIALS.get(4);
                System.out.println(String.format ("%s%s%s", 
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()));
            } else {
                // Alert the user - close the programme (no point continuing).
                System.out.println(String.format ("%s%s%s - %sdbAccess.txt contains invalid database credentials.%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour(), Colour.BLUE.getColour(), Colour.RESET.getColour(), LineSideModule.NEW_LINE));
                System.exit(0);
            }
        } catch (NullPointerException | IOException e) {
            // Alert the user - close the programme (no point continuing).
            System.out.println(String.format ("%s%s%s - %sCannot read from dbAccess.txt%s%s", 
                Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour(), Colour.BLUE.getColour(), Colour.RESET.getColour(), LineSideModule.NEW_LINE));
            System.exit(0);
        }
    }
}
