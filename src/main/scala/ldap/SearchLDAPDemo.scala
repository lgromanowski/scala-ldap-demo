package ldap

import com.normation.ldap.sdk._
import net.liftweb.common.Full

import scala.util.Try

object SearchLDAPDemo extends App {

  LDAPConnectionWithProvider.get.foreach { connWithProvider =>
      println(s"Connected to ${connWithProvider.provider.host}:${connWithProvider.provider.port}")
      runSearches(connWithProvider.conn)
      val bindDN = "uid=jsmith,ou=People,dc=example,dc=com"
      val password = "tobehashed"
      val loginResult = login(connWithProvider.provider, bindDN, password)
      println("loginResult: " + loginResult)
  }

  /*
   * Return true or false depending on whether the login was successful with the supplied
   * credentials.
   * The password is in plaintext on the assumption we are operating in a VPN.
   */
  def login(provider: LDAPConnectionProvider[RoLDAPConnection], bindDN: String, password: String):
      Boolean =
    provider.map { p =>
      val bindResult = Try(p.backed.bind(bindDN, password))
      bindResult.map(_.getResultCode.isConnectionUsable).isSuccess
    } == Full(true)

  def runSearches(conn: RoLDAPConnection): Unit = {

    val search = new SearchLDAP(conn)

    val admins: Seq[User] = search.queryAllAdmins
    println(s"All admins:\n\n${admins.mkString("\n")}\n")

    val userJsmith = search.getLDAPUser("jsmith")
    println(s"user retrieved: $userJsmith\n")

    val groups = search.getLDAPGroupNamesForUser("jsmith")
    println(s"groups: ${groups.mkString}\n")
  }
}

