# SQLite JDBC Tutorial
In this tutorial, I explain how to write a program in Java to work with SQLite database.

## JDBC: At a Glance
The Java Database Connectivity, or JDBC in short, is a standard for dealing with a relational database in Java.
JDBC APIs enable developers to interact with a database regradless of the vendor.
These functionalities entail [1]:

+ Creating and managing data source connections
+ Integrating SQL with Java programs, i.e. sending SQL queries to data source
+ Processing retrieved results from data source 

Now, let's see how to use JDBC APIs in programs. In the following, the key classes in JDBC are introduced:
1. [**`DriverManager`**](https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html):
Applications can establish a connection to a data source via this class.
`DriverManager` requires a Driver class typically implemented by third parties.
These drivers can be determined using Java dynamic loading mechanism `Class.forName("a.b.Class")`.
We also need to specify a data source URL. The Driver class and the URL are provided by database vendors. 
Depending on the database, you may need to pass other information such as credentials and configuration properties.
   
```java
  Connection con = DriverManager.getConnection(
                     "jdbc:myDriver:myDatabase",
                     username, password);
```

   The following Table lists required JDBC information for some well-known open-source databases.  
   
|Database|JDBC Driver|JDBC URL|
|:---|:---|:---|
|MySQL|`com.mysql.jdbc.Driver`|jdbc:mysql://*HOST*:*PORT*/*DATABASE_NAME*|
|PostgreSQL|`org.postgresql.Driver`|jdbc:postgresql://*HOST*/*DATABASE_NAME*|
|**SQLite**|**`org.sqlite.JDBC`**|**jdbc:sqlite:_DATABASE_FILE_**|

2. [**`Connection`**](https://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html) class contains information about the underlying connection to the data source.
For sending SQL statements to the database, we need to create a `Statement` object. 
```java
  Statement stmt = con.createStatement();
```
3. [**`Statement`**](https://docs.oracle.com/javase/8/docs/api/java/sql/Statement.html) class is used to execute SQL statements.
```java
  ResultSet rs = stmt.executeQuery("SELECT column1 FROM Table1");
  stmt.executeUpdate("INSERT INTO Table1 VALUES (value1,value2)");
```
4. [**`ResultSet`**](https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html) class represents results returned by the data source.
`ResultSet` operates like an iterator, i.e. it points to the current row of data and its pointer should be moved forward to read the data.   
```java
  while (rs.next()) {
    System.out.println(rs.getString("column1"));
  }
```

All JDBC APIs are provided in `java.sql` and `javax.sql` packages.

## SQLite-JDBC
SQLite-JDBC [2] is the JDBC Driver we're using for SQLite in this tutorial.

SQLite supports in-memory data management and use SQLite without any files, JDBC URL should be defined as **`jdbc:sqlite::memory:`**.
Also, for storing data in a file, JDBC URL must be **`jdbc:sqlite:/path/myfile.db`** (UNIX-style) or **`jdbc:sqlite:C:/path/myfile.db`** (Windows-style).

Here is an example code to acquire an in-memory SQLite connection:
```java
Class.forName("org.sqlite.JDBC");
      
try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {
  Statement stmt = con.createStatement();
} catch (SQLException e) {
  System.err.println(e.getMessage());
}
```

SQLite-JDBC library provides `SQLiteConfig` object to configure connections.
`SQLiteConfig` offers a wide of range configurations, most of which requires detailed knowledge on SQLite.
Here, we leverage it to enforce foreign key constraints (which is not enabled by default):
```java
SQLiteConfig config = new SQLiteConfig();
config.enforceForeignKeys(true);      
Connection con = DriverManager.getConnection("jdbc:sqlite::memory:", config.toProperties());
```  

## Example
Let's put what we discussed into practice. Consider the following schema:

- **course** (*course_id*, title, seats_available)  
- **student** (*student_id*, name)  
- **take** (*student_id*, *course_id*, enroll_date)  

In [SQLiteEnrollExample.java](src/SQLiteEnrollExample.java) class, this schema is created and initialized with sample data.
The task here is to write a program that enrolls a student to a course.
The program must prompt the user for student id and course id.
Then, after performing preliminary validations, it must insert a row to **take** table and update _seats_available_ in **course** table.
The code can be found in [SQLiteEnrollExample.java](src/SQLiteEnrollExample.java). 

### Running the Example
 


## References
[1] https://docs.oracle.com/javase/tutorial/jdbc/overview/index.html

[2] https://github.com/xerial/sqlite-jdbc