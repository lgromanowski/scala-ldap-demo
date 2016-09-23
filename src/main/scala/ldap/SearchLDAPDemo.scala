package ldap

import com.normation.ldap.sdk._
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

object SearchLDAPDemo extends App {

  val config = ConfigFactory.load()

  val ldapHost = config.getString("ldapHost")
  val ldapPort = config.getInt("ldapPort")

  getLdapConnection(ldapHost, ldapPort) match {
    case Success(conn) => runSearches(conn)
    case Failure(conn) => println(s"Unable to connect to ldap host at $ldapHost:$ldapPort")
  }

  def getLdapConnection(ldapHost: String, ldapPort: Int): Try[RoLDAPConnection] = {
    val provider = new ROAnonymousConnectionProvider(host = ldapHost, port = ldapPort)
    Try(provider.newConnection)
    // TODO: try the backup LDAP server(s)
  }

  def runSearches(conn: RoLDAPConnection): Unit = {

    val search = new SearchLDAP(conn)

    val admins: Seq[User] = search.queryAllAdmins
    println(s"All admins:\n\n${admins.mkString("\n")}\n")

    val userJsmith = search.getLDAPUser("jsmith")
    println(s"user retrieved: $userJsmith\n")

    val groups = search.getLDAPGroupNamesForUser("jsmith")
    println("groups: " + groups.mkString)
  }
}

