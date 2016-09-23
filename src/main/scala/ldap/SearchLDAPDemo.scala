package ldap

import com.normation.ldap.sdk._

object SearchLDAPDemo extends App {

  LDAPConnectionWithProvider.get.foreach { connWithProvider =>
      println(s"Connected to ${connWithProvider.provider.host}:${connWithProvider.provider.port}")
      runSearches(connWithProvider.conn)
      val username = "jsmith"
      val password = "tobehashed"
      val loginResult = Login.login(connWithProvider.provider, username, password)
      println("loginResult: " + loginResult)
  }

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

