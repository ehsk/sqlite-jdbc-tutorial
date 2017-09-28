# SQLite JDBC Tutorial
The goal of this tutorial is to learn how to write a program in Java to work with SQLite database.
This tutorial covers the basics of JDBC and SQLite-JDBC.
An example code is also provided to practice on JDBC.
Here is the outline of this tutorial:

+ [JDBC: At a Glance](#jdbc-at-a-glance)
+ [SQLite-JDBC](#sqlite-jdbc)
+ [Example](#example)
   

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

The goal here is to write a program that is able to perform two tasks:

First, the program must support enrolling a student to a course.
In order to do that, it prompts user for student id and course id.
Then, after performing preliminary validations, the program must insert a row to **take** table 
and update _seats_available_ in **course** table.

The second task is defined to work with `ResultSet` class.
In this task, the program retrieves information of students, but not all in once.
The program must load information page by page, called pagination, because the number of students may be so large
that it does'nt fit into memory. The details of pagination in SQLite is provided in [3].
     
The code can be found in [SQLiteJDBCExample.java](src/SQLiteJDBCExample.java). 

### Running the Example
Before running the code, you need to compile the code. The code is located in [src](src)
and depends on SQLite-JDBC library, located in [lib](lib).

You can compile the code using the following command:

    mkdir target
    javac -cp "lib/sqlite-jdbc-3.20.0.jar" -d target src/SQLiteJDBCExample.java

The class file is generated in _target_ folder.
The following command can be used to run the code:

    java -cp "lib/sqlite-jdbc-3.20.0.jar:target" SQLiteJDBCExample <arg>
    
The argument can be either _paginate_ or _enroll_.   


## References
[1] https://docs.oracle.com/javase/tutorial/jdbc/overview/index.html

[2] https://github.com/xerial/sqlite-jdbc

[3] https://www.sqlite.org/cvstrac/wiki?p=ScrollingCursor