/* Template JAVA User Interface
 
   Database Management Systems
   Department of Computer Science &amp; Engineering
   University of California - Riverside
 
 Target DBMS: 'Postgres' */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

//This class defines a simple embedded SQL utility class that is designed to work with PostgreSQL JDBC drivers.
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /*
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /*
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }

   /*
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }

   /*
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }

   /*
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /*
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /*
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }

    //The main execution method
    //@param args the command line arguments this inclues the <mysql|pgsql> <login file>
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical connection
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            System.out.println("#### DEBUG MODE ###");
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            //System.out.println("Please make your choice: ");
            //switch (readChoice()){
            System.out.println("Please make your choice: 2");
            switch (2){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }
            if (authorisedUser != null) {
              boolean usermenu = true;
              loadNearbyStores(esql);
              while(usermenu) {
               System.out.println("MAIN MENU");
               System.out.println("---------");
               System.out.println("1. View Stores within 30 miles");
               System.out.println("2. View Product List");
               System.out.println("3. Place a Order");
               System.out.println("4. View 5 recent orders");

               //the following functionalities basically used by managers
               System.out.println("5. Update Product");
               System.out.println("6. View 5 recent Product Updates Info");
               System.out.println("7. View 5 Popular Items");
               System.out.println("8. View 5 Popular Customers");
               System.out.println("9. Place Product Supply Request to Warehouse");

               System.out.println(".........................");
               System.out.println("20. Log out");

               switch (readChoice()) {
                  case 1: viewStores(esql); break;
                  case 2: viewProducts(esql); break;
                  case 3: placeOrder(esql); break;
                  case 4: viewRecentOrders(esql); break;
                  case 5: updateProduct(esql); break;
                  case 6: viewRecentUpdates(esql); break;
                  case 7: viewPopularProducts(esql); break;
                  case 8: viewPopularCustomers(esql); break;
                  case 9: placeProductSupplyRequests(esql); break;

                  case 20: usermenu = false; break;
                  default : System.out.println("Unrecognized choice!"); break;
               }
              }
            }
         }
      }
      catch (Exception e) {
         System.err.println (e.getMessage());
      }
      finally {
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }
         }
         catch (Exception e) {
            // ignored.
         }
      }
   }

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }

   //Reads the users choice given from the keyboard @int
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }
         catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }
      }
      while (true);
      return input;
   }

   // create new user
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   /***/ private int userID; /***/ //the id of the current user according to the database
   /***/ private ArrayList<Integer> nearbyStores = new ArrayList<Integer>(); /***/ //list of the store ids of all stores within 30 miles of the user's lat and long

   //Check log in credentials for an existing user @return User login or null is the user does not exist
   public static String LogIn(Amazon esql){
      try{
         // System.out.print("\tEnter name: ");
         // String name = in.readLine();
         // System.out.print("\tEnter password: ");
         // String password = in.readLine();

         System.out.println("\tEnter name: Amy");
         System.out.println("\tEnter password: xyz");
         String name = "Amy";
         String password = "xyz";

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);

         query = String.format("SELECT userID FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         esql.userID = Integer.parseInt(res.get(0).get(0));
	 if (userNum > 0)
		return name;
         return null;
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }

   // Rest of the functions definition go in here

//William
   /*
    * Saves all stores within 30 miles of user into class member
    * I moved this out of the viewStores method because other
    * methods require a check for stores within 30 miles
    * like placeOrder()
    */
   public static void loadNearbyStores(Amazon esql){
      esql.nearbyStores.clear();
      String query;
      List<List<String>> res;
      double lat1, lat2, long1, long2;
      try {
         query = String.format("SELECT latitude, longitude FROM Users WHERE userID = '%s'", esql.userID);
         res = esql.executeQueryAndReturnResult(query);
         lat1 = Double.parseDouble(res.get(0).get(0));
         long1 = Double.parseDouble(res.get(0).get(1));
         
         query = String.format("SELECT storeID, latitude, longitude FROM Store");
         res = esql.executeQueryAndReturnResult(query);
         for (int i = 0; i < res.size(); i++) {
            lat2 = Double.parseDouble(res.get(i).get(1));
            long2 = Double.parseDouble(res.get(i).get(2));
            if(esql.calculateDistance(lat1, long1, lat2, long2) < 30) {
               esql.nearbyStores.add(Integer.parseInt(res.get(i).get(0)));
            }
         }
         System.out.println();
         return;
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

//William
   public static void viewStores(Amazon esql) {
      System.out.println("\nStores(shown as store ID's) within 30 miles");
      System.out.println("-------------------------------------------");
      for(int i = 0; i < esql.nearbyStores.size(); i++) {
         System.out.println(esql.nearbyStores.get(i));
      }
      System.out.println();
   }

//William
   public static void viewProducts(Amazon esql) {
      String query, ID, str;
      int IDint;
      try {
         System.out.println("\nEnter a store ID to show that store's products");
         System.out.print("Store ID: ");
         ID = in.readLine();
         IDint = Integer.parseInt(ID);
         query = String.format("SELECT DISTINCT productName, pricePerUnit, numberOfUnits FROM Product, Store WHERE Product.storeID = %d ORDER BY productName ASC", IDint);
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         System.out.println(String.format("\n%-25s%-13s%-20s", "Product", "Price/Unit", "Units" ));
         System.out.println("-----------------------------------------------------------");
         for(int i = 0; i < res.size(); i++) {
            str = String.format("%-25s%-13s%-20s", res.get(i).get(0).trim(), res.get(i).get(1),res.get(i).get(2));
            System.out.println(str);
         }
         System.out.println();
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      }
   }

//William
//check to see if a string contains only numbers
   public static boolean isNumeric(String str) {
      try {
         Integer.parseInt(str);
         return true;
      }
      catch(NumberFormatException e) {
         return false;
      }
   }

//William
//sub-function of placeOrder()
   public static int selectStore(Amazon esql) {
      String ID;
      int IDint;
      boolean next = false;
      
      while(true) {
         try {
            System.out.print("\nEnter Store ID or enter 0 to go back: ");
            ID = in.readLine();
            ID = ID.replaceAll("\\s","");
            if(!esql.isNumeric(ID)){
               System.out.println("Please enter only digits for the ID");
               continue;
            }
            IDint = Integer.parseInt(ID);
            if(IDint == 0) {
               return 0;
            }
            
            for(int i = 0; i < esql.nearbyStores.size(); i++) {
               System.out.println(i);
               if(IDint == esql.nearbyStores.get(i)) {
                  return IDint;
               }
            }
            System.out.println("Cannot find a store with (ID: " + IDint + ") within 30 miles\n");
         }
         catch (Exception e) {
            System.err.println (e.getMessage ());
            return -1;
         }           
      }
   }
   
   public static void placeOrder(Amazon esql) {
      int StoreID;

      StoreID = esql.selectStore(esql);
   }

//William And Jeffrey
//I think this function will show 5 recent orders if account is non manager and all if manager
//I will do the user side and you can do the manager side
//You will need to edit the lines in LogIn() to ask for the account stuff
//Currently I have it set to log in automatically to Line 3 in users.csv
   public static void viewRecentOrders(Amazon esql) {
      //pseudocode
      //if user then view own most recent 5
      //if manager then all order info of stores they manage
   }

//Rest are Jeffrey
   public static void updateProduct(Amazon esql) {}

   public static void viewRecentUpdates(Amazon esql) {}

   public static void viewPopularProducts(Amazon esql) {}

   public static void viewPopularCustomers(Amazon esql) {}

   public static void placeProductSupplyRequests(Amazon esql) {}

}//end Amazon

