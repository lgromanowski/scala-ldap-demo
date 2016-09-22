import com.normation.ldap.sdk._
import com.unboundid.ldap.sdk.DN
import com.unboundid.ldap.sdk.Filter
import com.normation.ldap.sdk.BuildFilter._

object LdapDemo extends App {

  val ldapServer = "localhost"
  val ldapServerPort = 1234

  case class User(
      uid: String,
      firstName: String,
      surname: String,
      location: String,
      email: String,
      phoneNumber: String,
      roomNumber: String,
      password: String) {
    override def toString = s"$firstName $surname"
  }

  val provider = new ROAnonymousConnectionProvider(host = ldapServer, port = ldapServerPort)
  val con = provider.newConnection

  queryAllAdmins()
  queryLevel4Admins()
  queryLevel3AndAboveAdmins()

  // Map the LDAPEntries to business objects by accessing their attributes
  private[this] def ldapEntriesToUsers(ldapEntries: Seq[LDAPEntry]): Seq[User] =
    ldapEntries.flatMap(e => for {
      uid <- e("uid")
      givenname <- e("givenname")
      sn <- e("sn")
      location <- e("l")
      email <- e("mail")
      phoneNumber <- e("telephonenumber")
      roomNumber <- e("roomnumber")
      password <- e("userpassword")
    } yield User(uid, givenname, sn, location, email, phoneNumber, roomNumber, password))

  private[this] def filterForRole(role: String): Filter =
    BuildFilter.SUB(
      attributeName = "nsRoleDN",
      subInitial = null,
      subAny = Array(role),
      subFinal = null)

  def queryAllAdmins(): Unit = {
    val users = new DN("ou=Admins,dc=example,dc=com")
    val userEntries: Seq[LDAPEntry] = con.searchOne(users, ALL)
    val admins: Seq[User] = ldapEntriesToUsers(userEntries)

    println(s"All admins:\n\n${admins.mkString("\n")}\n\n")
  }

  def queryLevel4Admins(): Unit = {
    val users = new DN("ou=Admins,dc=example,dc=com")
    val searchFilter = filterForRole("Level 4 Admins")
    val userEntries: Seq[LDAPEntry] = con.searchOne(users, searchFilter)
    val admins: Seq[User] = ldapEntriesToUsers(userEntries)

    println(s"Level 4 admins:\n\n${admins.mkString("\n")}\n\n")
  }

  def queryLevel3AndAboveAdmins(): Unit = {
    val users = new DN("ou=Admins,dc=example,dc=com")
    val searchFilter =
      BuildFilter.OR(filterForRole("Level 3 Admins"), filterForRole("Level 4 Admins"))
    val userEntries: Seq[LDAPEntry] = con.searchOne(users, searchFilter)
    val admins: Seq[User] = ldapEntriesToUsers(userEntries)

    println(s"Level 3 and above admins:\n\n${admins.mkString("\n")}\n\n")
  }
}
