package ldap

/*
 * Group entity taken from an LDAP entry.
 */
case class Group(
    name: String,
    gidNumber: Int,
    memberUids: Seq[String],
    description: String)
