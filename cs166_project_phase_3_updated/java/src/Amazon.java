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
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            System.out.println("Please make your choice: ");
            switch (readChoice()){
            // System.out.println("Please make your choice: 2");
            // switch (2){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }
            if (authorisedUser != null) {
              boolean usermenu = true;
              loadNearbyStores(esql);
              loadManagers(esql);
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
               System.out.println("10. View Product Supply Requests");

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
                  case 10: viewProductSupplyRequests(esql); break;

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
   /***/ private ArrayList<Integer> managerList = new ArrayList<Integer>(); /***/ //list of all manager ids

   //Check log in credentials for an existing user @return User login or null is the user does not exist
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         // System.out.println("\tEnter name: Amy");
         // System.out.println("\tEnter password: xyz");
         // String name = "Amy";
         // String password = "xyz";

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

   public static void loadManagers(Amazon esql) {
      esql.managerList.clear();
      String query;
      List<List<String>> res;
      try {
         query = String.format("SELECT DISTINCT managerID FROM Store");
         res = esql.executeQueryAndReturnResult(query);
         for (int i = 0; i < res.size(); i++) {
            esql.managerList.add(Integer.parseInt(res.get(i).get(0)));
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
//sub-function1 of placeOrder()
   public static int selectStore(Amazon esql) {
      String ID;
      int IDint;
      
      while(true) {
         try {
            System.out.print("\nEnter Store ID or enter 0 to go back to Main Menu: ");
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
   
//William
//sub-function2 of placeOrder()
   public static String selectProduct(Amazon esql, int storeID) {
      String product, query; 
      List<List<String>> res;
      while(true) {
         try {
            System.out.print("\nEnter Product name or enter 0 to go back: ");
            product = in.readLine();
            product = product.trim();
            if(esql.isNumeric(product)) {
               if(Integer.parseInt(product) == 0) {
                  return "0";
               }  
            }
            query = String.format("SELECT productName FROM Product WHERE storeID = %d AND productName = '%s'", storeID, product);
            res = esql.executeQueryAndReturnResult(query);
            if(res.size() > 0) {
               return res.get(0).get(0);
            }
            else {
               System.out.println("Could not find product with name: " + product);
            }
         }
         catch (Exception e) {
            System.err.println (e.getMessage ());
            return "";
         }
      }
   }

//William
//sub-function3 of placeOrder()
   public static int selectCount(Amazon esql, int storeID, String productName) {
      String count, query;
      List<List<String>> res;
      int countNum, available;
      
      while(true) {
         try {
            System.out.print("\nEnter quantity or enter 0 to go back: ");
            count = in.readLine();
            count = count.replaceAll("\\s","");
            if(!esql.isNumeric(count)){
               System.out.println("Please enter only digits for the quantity");
               continue;
            }
            countNum = Integer.parseInt(count);
            if(countNum == 0) {
               return 0;
            }
            query = String.format("SELECT numberOfUnits FROM Product WHERE storeID = %d AND productName = '%s'", storeID, productName);
            res = esql.executeQueryAndReturnResult(query);
            if(res.size() > 0) {
               available = Integer.parseInt(res.get(0).get(0));
               if(countNum <= available) {
                  return countNum;
               }
               else {
                  System.out.println("Error: count is too high");
                  System.out.println("There are only " + available + " of this item available in this store");
                  continue;
               }
            }
            else {
               System.out.println("ERROR: Response size is 0");
               return -1;
            }
         }
         catch (Exception e) {
            System.err.println (e.getMessage ());
            return -1;
         }           
      }
   }

//William
//sub-funciton4 of placeOrder()
   public static void insertOrder(Amazon esql, int storeID, String pname, int count) {
      String query;
      try {
         query = String.format("INSERT INTO Orders Values(DEFAULT, %d, %d, '%s', %d, CURRENT_TIMESTAMP)", esql.userID, storeID, pname, count);
         esql.executeUpdate(query);
         query = String.format("UPDATE Product SET numberOfUnits = numberOfUnits - %d WHERE storeID = %d AND productName = '%s'", count, storeID, pname);
         esql.executeUpdate(query);
      }
      catch (Exception e) {
         System.err.println (e.getMessage ());
         return;
      } 
   }

   public static void placeOrder(Amazon esql) {
      int cur = 1;
      String choice = "";

      int storeID = 0; //prevent initialization warning
      int count = 0;
      String pName = "";
   
      while(true) {
         switch(cur) {
            case 1:
               storeID = esql.selectStore(esql);
               if(storeID == 0) {
                  return;
               }
               cur = 2;
               break;
            case 2:
               pName = esql.selectProduct(esql, storeID);
               pName = pName.trim();
               if(esql.isNumeric(pName)) {
                  if(Integer.parseInt(pName) == 0) {
                     cur = 1;
                     break;
                  }  
               }
               cur = 3;
               break;
            case 3:
               count = esql.selectCount(esql, storeID, pName);
               if(count == 0) {
                  cur = 2;
                  break;
               }
               cur = 4;
               break;
            case 4:
               System.out.println("\nStore ID: " + storeID + "   Product: " + pName + "     Quantity: " + count);
               System.out.println("Enter y to confirm order\nEnter b to go back\nEnter n to cancel order");
               try {
                  choice = in.readLine();
                  choice = choice.trim();
                  switch(choice) {
                     case "y":
                        cur = 5;
                        break;
                     case "b":
                        cur = 3;
                        break;
                     case "n":
                        return;
                     default:
                        System.out.println("Error at order confirmation");
                  }
               }
               catch (Exception e) {
                  System.err.println (e.getMessage ());
                  return;
               }      
               break;
            case 5:
               esql.insertOrder(esql, storeID, pName, count);
               System.out.println("Order Placed!");
               return;
            default: break;
         }
      }
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
      int isManager = 0;
      String query, str;
      List<List<String>> res;
      int resInt;
      isManager = esql.checkIfManager(esql);
      if(isManager == -1) { //if user
         try {
            query = String.format("SELECT * FROM Orders WHERE CustomerID = %d ORDER BY orderTime DESC LIMIT 5", esql.userID);
            res = esql.executeQueryAndReturnResult(query); 
            System.out.println(String.format("\n%-15s%-12s%-30s%-15s    %s", "Order Number", "Store ID", "Product Name", "Units Ordered", "Order Time" ));
            System.out.println("------------------------------------------------------------------------------------------------------");
            for(int i = 0; i < res.size(); i++) {
               str = String.format("%-15s%-12s%-30s%-15s    %s", res.get(i).get(0), res.get(i).get(2), res.get(i).get(3).trim(), res.get(i).get(4), res.get(i).get(5));
               System.out.println(str);
            }
            System.out.println();
         }
         catch (Exception e) {
            System.err.println (e.getMessage ());
            return;
}           
      }
      else { // if manager
         try {
            query = "SELECT O.orderNumber, U.name, O.storeID, O.productName, O.orderTime " +
            "FROM Orders O, Users U " +
            "WHERE O.customerID = U.userID " +
            "AND O.storeID IN (SELECT storeID FROM Store WHERE managerID = " + isManager + ") " +
            "ORDER BY O.orderTime DESC";
            resInt = esql.executeQueryAndPrintResult(query);
         }
         catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
         }           
      }
      return;
   }

public static int checkIfManager(Amazon esql) {
      try {
          int userID = esql.userID;
          String query = "SELECT userID FROM Users WHERE userID = " + userID + " AND type = 'manager'";
          List<List<String>> result = esql.executeQueryAndReturnResult(query);
          return result.isEmpty() ? -1 : Integer.parseInt(result.get(0).get(0));
      } catch (Exception e) {
          System.err.println("Error: " + e.getMessage());
          return -1;
      }
  }

//Rest are Jeffrey
   public static void updateProduct(Amazon esql) {
      try {
         int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized update Product.");
            return;
         }
 
         // Prompt the user for store ID
         int storeID;
         do {
             System.out.print("Enter the store ID: ");
             try {
                 storeID = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid store ID.");
             }
         } while (true);
 
         // Check if the manager manages the given store
         String checkManagerQuery = "SELECT * FROM Store WHERE managerID = " + managerID + " AND storeID = " + storeID;
         if (esql.executeQueryAndReturnResult(checkManagerQuery).isEmpty()) {
             System.out.println("You don't manage the store with ID " + storeID);
             return;
         }

         // Prompt the user for product information updates
         String productName;
         do {
             System.out.print("Enter the product name: ");
             productName = in.readLine().trim();
             if (productName.isEmpty()) {
                 System.out.println("Product name cannot be empty.");
             }
         } while (productName.isEmpty());
 
         int newNumberOfUnits;
         do {
             System.out.print("Enter the new number of units: ");
             try {
                 newNumberOfUnits = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid number.");
             }
         } while (true);
 
         float newPricePerUnit;
         do {
             System.out.print("Enter the new price per unit: ");
             try {
                 newPricePerUnit = Float.parseFloat(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid price.");
             }
         } while (true);
 
         // Update product information
         String updateProductQuery = "UPDATE Product SET numberOfUnits = " + newNumberOfUnits + ", pricePerUnit = " + newPricePerUnit +
                 " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
         esql.executeUpdate(updateProductQuery);
 
         // Update ProductUpdates table
         String insertProductUpdateQuery = "INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) VALUES (" +
                 managerID + ", " + storeID + ", '" + productName + "', CURRENT_TIMESTAMP)";
         esql.executeUpdate(insertProductUpdateQuery);
 
         System.out.println("Product information updated successfully!");
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }

   public static void viewRecentUpdates(Amazon esql) {
      try {
         int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized to view recent updates.");
            return;
         }
 
         // Retrieve the store IDs managed by the manager
         String getManagedStoresQuery = "SELECT storeID FROM Store WHERE managerID = " + managerID;
         List<List<String>> managedStores = esql.executeQueryAndReturnResult(getManagedStoresQuery);
         if (managedStores.isEmpty()) {
             System.out.println("You don't manage any stores.");
             return;
         }
 
         // Fetch the last 5 recent updates for all managed stores
         String viewRecentUpdatesQuery = "SELECT updateNumber, managerID, storeID, productName, updatedOn " +
                 "FROM ProductUpdates WHERE storeID IN (SELECT storeID FROM Store WHERE managerID = " + managerID +
                 ") ORDER BY updatedOn DESC LIMIT 5";
         System.out.println("Recent updates for your managed stores:");
         esql.executeQueryAndPrintResult(viewRecentUpdatesQuery);
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }
   
   public static void viewPopularProducts(Amazon esql) {
      try {
         int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized to view popular products.");
            return;
         }
         String query = "SELECT p.productName, SUM(o.unitsOrdered) AS totalOrdered " +
                        "FROM Orders o " +
                        "JOIN Product p ON o.storeID = p.storeID AND o.productName = p.productName " +
                        "JOIN Store s ON o.storeID = s.storeID " +
                        "WHERE s.managerID = " + managerID + " " +
                        "GROUP BY p.productName " +
                        "ORDER BY totalOrdered DESC " +
                        "LIMIT 5";
         
         esql.executeQueryAndPrintResult(query);
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
 }

   public static void viewPopularCustomers(Amazon esql) {
      try {
         int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized to view popular customers.");
            return;
         }
 
         String query = "SELECT U.name, COUNT(*) AS orderCount " +
                        "FROM Orders O, Users U, Store S " +
                        "WHERE O.customerID = U.userID AND O.storeID = S.storeID AND S.managerID = " + managerID + " " +
                        "GROUP BY U.name " +
                        "ORDER BY orderCount DESC " +
                        "LIMIT 5";
 
         esql.executeQueryAndPrintResult(query);
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }

   public static void placeProductSupplyRequests(Amazon esql) {
      try {
         int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized to place product supply requests.");
            return;
         }
 
         // Retrieve the store IDs managed by the manager
         String getManagedStoresQuery = "SELECT storeID FROM Store WHERE managerID = " + managerID;
         List<List<String>> managedStores = esql.executeQueryAndReturnResult(getManagedStoresQuery);
         if (managedStores.isEmpty()) {
             System.out.println("You don't manage any stores.");
             return;
         }
 
         // Prompt the user for store ID
         int storeID;
         do {
             System.out.print("Enter the store ID: ");
             try {
                 storeID = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid store ID.");
             }
         } while (true);

         // Check if the manager manages the given store
         String checkManagerQuery = "SELECT * FROM Store WHERE managerID = " + managerID + " AND storeID = " + storeID;
         if (esql.executeQueryAndReturnResult(checkManagerQuery).isEmpty()) {
            System.out.println("You don't manage the store with ID " + storeID);
            return;
         }
 
         // Prompt the user for product name
         System.out.print("Enter product name: ");
         String productName = in.readLine();
 
         // Prompt the user for number of units needed
         int numberOfUnits;
         do {
             System.out.print("Enter number of units needed: ");
             try {
                 numberOfUnits = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid number.");
             }
         } while (true);
 
         // Prompt the user for warehouse ID
         int warehouseID;
         do {
             System.out.print("Enter warehouse ID: ");
             try {
                 warehouseID = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid warehouse ID.");
             }
         } while (true);
 
         // Update the Product table
         String updateProductQuery = "UPDATE Product SET numberOfUnits = numberOfUnits + " + numberOfUnits +
                                     " WHERE storeID = " + storeID + " AND productName = '" + productName + "'";
         esql.executeUpdate(updateProductQuery);
 
         // Insert the supply request into the ProductSupplyRequests table
         String insertSupplyRequestQuery = "INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) " +
                 "VALUES (" + managerID + ", " + warehouseID + ", " + storeID + ", '" + productName + "', " + numberOfUnits + ")";
         esql.executeUpdate(insertSupplyRequestQuery);
 
         System.out.println("Supply request placed successfully.");
     } catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
     }
   }

   public static void viewProductSupplyRequests(Amazon esql) {
      try {
          int managerID = checkIfManager(esql);
         if (managerID == -1) {
            System.out.println("You are not authorized to view product supply requests.");
            return;
         }
  
          // Retrieve the store IDs managed by the manager
          String getManagedStoresQuery = "SELECT storeID FROM Store WHERE managerID = " + managerID;
          List<List<String>> managedStores = esql.executeQueryAndReturnResult(getManagedStoresQuery);
          if (managedStores.isEmpty()) {
              System.out.println("You don't manage any stores.");
              return;
          }
  
         // Prompt the user for store ID
         int storeID;
         do {
             System.out.print("Enter the store ID: ");
             try {
                 storeID = Integer.parseInt(in.readLine());
                 break;
             } catch (NumberFormatException e) {
                 System.out.println("Invalid input! Please enter a valid store ID.");
             }
         } while (true);

         // Check if the manager manages the given store
         String checkManagerQuery = "SELECT * FROM Store WHERE managerID = " + managerID + " AND storeID = " + storeID;
         if (esql.executeQueryAndReturnResult(checkManagerQuery).isEmpty()) {
            System.out.println("You don't manage the store with ID " + storeID);
            return;
         }
  
          // Query to select recent product supply requests for the manager's store
          String viewProductSupplyRequestsQuery = "SELECT * FROM ProductSupplyRequests WHERE storeID = " + storeID +
                  " ORDER BY requestNumber DESC LIMIT 5";
  
          // Execute the query and print the results
          esql.executeQueryAndPrintResult(viewProductSupplyRequestsQuery);
      } catch (Exception e) {
          System.err.println("Error: " + e.getMessage());
      }
  }

}//end Amazon

