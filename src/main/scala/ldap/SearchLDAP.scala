package ldap

import com.normation.ldap.sdk.BuildFilter._
import com.normation.ldap.sdk.{BuildFilter, LDAPEntry, RoLDAPConnection}
import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk.DN

/*
 * Functionality for searching an LDAP directory for users and groups.
 */
class SearchLDAP(conn: RoLDAPConnection) {

  val config = ConfigFactory.load()

  val rootDN = new DN(config.getString("rootDNString"))
  val peopleDN = new DN(s"ou=People,$rootDN")

  def queryAllAdmins: Seq[User] = {
    val userEntries: Seq[LDAPEntry] = conn.searchOne(peopleDN, ALL)
    userEntries.flatMap(ldapEntryToUser)
  }

  sealed trait LookupFailure
  case object UserNotFound extends LookupFailure
  case object MoreThanOneUserFound extends LookupFailure

  /*
   * Get an LDAP user.
   * Logic translated from the PHP _getLDAPUser method, some of which looks hacky.
   */
  def getLDAPUser(username: String, ou: String = "People"): Either[LookupFailure, User] = {

    val dn = new DN(s"ou=$ou,$rootDN")
    val userEntries: Seq[LDAPEntry] =
      if (ou == "People" || ou == "locked")
        // TODO: why does the old PHP code use rootDN here instead of dn?
        conn.searchOne(dn, BuildFilter.EQ("uid", username))
      else
        conn.searchOne(dn, BuildFilter.EQ("cn", username))

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

  /*
   * Get the groups a user belongs to. Returns group names.
   * Logic translated from legacy PHP method
   */
  def getLDAPGroupNamesForUser(username: String): Seq[String] =
    getLDAPGroupsForUser(username).map(_.name).sorted

  /*
   * Get the groups a user belongs to. Returns group entities.
   * Logic translated from legacy PHP method
   */
  def getLDAPGroupsForUser(username: String): Seq[Group] = {
    val isAGroupFilter = BuildFilter.EQ("objectclass", "posixGroup")
    val groupHasUserFilter = BuildFilter.EQ("memberuid", username)
    val combinedFilter = BuildFilter.AND(isAGroupFilter, groupHasUserFilter)
    val groupEntries: Seq[LDAPEntry] = conn.searchOne(rootDN, combinedFilter)
    groupEntries.flatMap(ldapEntryToGroup)
  }

  // TODO: write this based on what fields are actually mandatory/optional
  private[this] def ldapEntryToUser(e: LDAPEntry): Option[User] =
    e("uid").map { uid =>
      User(
        uid = uid,
        firstName = e("givenname").getOrElse(""),
        surname = e("sn").getOrElse(""),
        location = e("l").getOrElse(""),
        email = e("mail").getOrElse(""),
        phoneNumber = e("telephonenumber").getOrElse(""),
        roomNumber = e("roomnumber").getOrElse(""),
        password = e("userpassword").getOrElse("")
      )
    }

  case class GroupKeyFields(name: String, gidNumber: Int)
  // TODO: write this based on what fields are actually mandatory/optional
  private[this] def ldapEntryToGroup(e: LDAPEntry): Option[Group] = {
    (for {
      name <- e("cn")
      gidNumber <- e("gidNumber").map(_.toInt)
    } yield GroupKeyFields(name, gidNumber)).map { groupKeyFields =>
      val memberUids: Option[Seq[String]] = e.attribute("memberUid").map(_.getValues)
      Group(
        name = groupKeyFields.name,
        gidNumber = groupKeyFields.gidNumber,
        memberUids = memberUids.getOrElse(Nil),
        description = e("description").getOrElse("")
      )
    }
  }
}
