package ldap

import com.normation.ldap.sdk.{RoLDAPConnection, ROAnonymousConnectionProvider}
import com.typesafe.config.ConfigFactory

import scala.util.{Success, Try}

case class LDAPConnectionWithProvider(
    provider: ROAnonymousConnectionProvider,
    conn: RoLDAPConnection)

/*
 * Provides a means of getting an LDAP connection.
 */
object LDAPConnectionWithProvider {

  val ldapServers: Set[LDAPServer] = getLdapServersFromConfig

  case class LDAPServer(host: String, port: Int) {
    override def toString = s"$host:$port"

    def connect: Try[LDAPConnectionWithProvider] = {
      val provider = new ROAnonymousConnectionProvider(host, port)
      Try(provider.newConnection).map(conn => LDAPConnectionWithProvider(provider, conn))
    }
  }

  /*
   * Get an LDAP connection. The returned object includes provider (host) information.
   */
  def get: Option[LDAPConnectionWithProvider] =
    (ldapServers.iterator.map(_.connect).collectFirst { case Success(conn) => conn }).orElse {
      val ldapServersTried = ldapServers.mkString(", ")
      // TODO: use logger
      System.err.println(s"Unable to connect to any LDAP servers, tried: $ldapServersTried")
      None
    }

  /*
   * Get the host and port for the main LDAP server, but also backup server(s).
   */
  def getLdapServersFromConfig: Set[LDAPServer] = {

    val config = ConfigFactory.load()
    val maxServersAllowed = 20 // but usually only about 2
    val numServersConfigured = (1 to maxServersAllowed).find(num =>
        !config.hasPath(s"ldapHost$num")).getOrElse(maxServersAllowed) - 1

    def getLdapServerFromConfig(num: Int): LDAPServer =
      LDAPServer(config.getString(s"ldapHost$num"), config.getInt(s"ldapPort$num"))

    var ldapServers = Set[LDAPServer]()
    (1 to numServersConfigured).foreach(ldapServers += getLdapServerFromConfig(_))

    ldapServers
  }

}

