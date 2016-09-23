scala-ldap-demo
===============

This shows how the [scala-ldap](https://github.com/Normation/scala-ldap) project 
can be used for retrieving admin data from an LDAP store.

`scala-ldap` wraps the popular [Java LDAP SDK](https://www.ldap.com/unboundid-ldap-sdk-for-java) from UnboundID.

To get the demo working, clone this project, set up an LDAP server locally, then run the sample code.


Clone this project
==================

    git clone git://github.com/oranda/scala-ldap-demo

Alternatively download it from the GitHub page and unzip it.


Set up an LDAP server
=====================

One possibility is [ApacheDS](http://directory.apache.org/apacheds/). ApacheDS also
have a UI tool called Studio which can be used with any LDAP server.

However, I'll assume a barebones setup with just an in-memory store from UnboundID. 

1. Download the Standard Edition of the UnboundID SDK from this page: https://www.ldap.com/unboundid-ldap-sdk-for-java.
 Unzip it somewhere convenient. 
 
2. Go into the `tools` folder and run this. You will need to adjust the path of the LDIF file
 so that it points to `scala-ldap-demo/data/admins.ldif` according to where you have the `scala-ldap-demo` project.
 Take a look in this file. It is sample data in LDAP format that the server is initialized with.

        ./in-memory-directory-server --baseDN "dc=example,dc=com" --port 1234 --ldifFile ../../../dev/scala-ldap-demo/data/admins.ldif 


Run the sample code
===================

Assuming you have Scala and `sbt` installed, go into the `scala-ldap-demo` directory, run `sbt` and then
type `run`. This runs the code in [LDAPDemo.scala](src/main/scala/ldap/LDAPDemo.scala). You should see data retrieved from the LDAP server when some queries are run like this:


    All admins:
    
    Anne Carter
    Charlie Davis
    Emma Williams
    George Brown
    Jane Doe
    John Jones
    John Smith
    Jack Xi
    Oliver Miller
    Robert Johnson
    
    
    Level 4 admins:
    
    Anne Carter
    George Brown
    
    
    Level 3 and above admins:
    
    Anne Carter
    Emma Williams
    George Brown
    John Jones



TODO
====

Investigate the options for authentication.