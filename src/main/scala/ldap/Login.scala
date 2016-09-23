package ldap

import com.normation.ldap.sdk.{RoLDAPConnection, LDAPConnectionProvider}
import net.liftweb.common.Full

import scala.util.Try

/*
 * Utilities for authentication.
 */
object Login {

  /*
   * Return true or false depending on whether the login was successful with the supplied
   * credentials.
   * The password is in plaintext on the assumption we are operating in a VPN.
   */
  def login(
      provider: LDAPConnectionProvider[RoLDAPConnection],
      username: String,
      password: String): Boolean = {
    val bindDN = s"uid=$username,ou=People,dc=example,dc=com"
    provider.map { p =>
      val bindResult = Try(p.backed.bind(bindDN, password))
      bindResult.map(_.getResultCode.isConnectionUsable).isSuccess
    } == Full(true)
  }
}
