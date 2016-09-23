package ldap

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
