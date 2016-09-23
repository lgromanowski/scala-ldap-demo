package ldap

case class Group(
    name: String,
    gidNumber: Int,
    memberUids: Seq[String],
    description: String)
