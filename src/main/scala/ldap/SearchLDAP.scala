package ldap

import com.normation.ldap.sdk.BuildFilter._
import com.normation.ldap.sdk.{BuildFilter, LDAPEntry, RoLDAPConnection}
import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk.DN

class SearchLDAP(conn: RoLDAPConnection) {

  val config = ConfigFactory.load()

  val rootDN = new DN(config.getString("rootDNString"))
  val peopleDN = new DN(s"ou=People,$rootDN")

  def queryAllAdmins: Seq[User] = {
    val userEntries: Seq[LDAPEntry] = conn.searchOne(rootDN/*peopleDN*/, ALL)
    userEntries.flatMap(ldapEntryToUser)
  }

  sealed trait LookupFailure
  case object UserNotFound extends LookupFailure
  case object MoreThanOneUserFound extends LookupFailure

  // logic translated from the PHP _getLDAPUser method, some of which looks hacky
  def getLDAPUser(username: String, ou: String = "People"): Either[LookupFailure, User] = {

    val userEntries: Seq[LDAPEntry] =
      if (ou == "People" || ou == "locked")
        conn.searchOne(rootDN, BuildFilter.EQ("uid", username))
      else
        conn.searchOne(new DN(s"ou=$ou,$rootDN"), BuildFilter.EQ("cn", username))

    if (userEntries.size > 1) Left(MoreThanOneUserFound)
    else {
      (for {
        entry <- userEntries.headOption
        user <- ldapEntryToUser(entry)
      } yield user) match {
        case Some(user) => Right(user)
        case None => Left(UserNotFound)
      }
    }
  }

  def getLDAPGroupNamesForUser(username: String): Seq[String] =
    getLDAPGroupsForUser(username).map(_.name).sorted

  // logic translated from legacy PHP method
  def getLDAPGroupsForUser(username: String): Seq[Group] = {
    val isAGroupFilter = BuildFilter.EQ("objectclass", "posixGroup")
    val groupHasUserFilter = BuildFilter.EQ("memberuid", username)
    val combinedFilter = BuildFilter.AND(isAGroupFilter, groupHasUserFilter)
    val groupEntries: Seq[LDAPEntry] = conn.searchOne(rootDN, combinedFilter)
    groupEntries.flatMap(ldapEntryToGroup)
  }

  private[this] def ldapEntryToUser(e: LDAPEntry): Option[User] =
    for {
      uid <- e("uid")
      givenname <- e("givenname")
      sn <- e("sn")
      location <- e("l")
      email <- e("mail")
      phoneNumber <- e("telephonenumber")
      roomNumber <- e("roomnumber")
      password <- e("userpassword")
    } yield User(uid, givenname, sn, location, email, phoneNumber, roomNumber, password)

  private[this] def ldapEntryToGroup(e: LDAPEntry): Option[Group] =
    for {
      name <- e("cn")
      gidNumber <- e("gidNumber")
      memberUids <- e.attribute("memberUid")
      description <- e("description")
    } yield Group(name, gidNumber.toInt, memberUids.getValues, description)
}
