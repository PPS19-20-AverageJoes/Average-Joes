package AverageJoes.model.user

  trait User {
    def name: String
    def surname: String
    def CF: String
  }

  trait SmartGymUser extends User {
    def userID: String
    def isLogged: Boolean
    def logIn(): Unit
    def logOut(): Unit
  }

  object SmartGymUser {
    def apply(name: String, surname: String, CF: String, userID: String): SmartGymUser = SmartGymUserImpl(name, surname, CF, userID)
  }

  case class SmartGymUserImpl(name: String, surname: String,  CF: String, userID: String) extends SmartGymUser {
    var logged = false

    override def isLogged: Boolean = logged

    override def logIn(): Unit = logged = true

    override def logOut(): Unit = logged = false
  }

  object Main extends App {

    var myUser1: SmartGymUser = SmartGymUser("sokol", "guri", "GRUSKL", "001")
    var myUser2: SmartGymUser = SmartGymUser("andrea", "guri", "ARUSKL", "002")
    var myUser3: SmartGymUser = SmartGymUser("elena", "guri", "ERUSKL", "003")

    println(myUser1)
    println(myUser2)
    println(myUser3)

    assert(myUser1.isLogged.equals(false))
    //assert(myUser2.isLogged, false)
    //assert(myUser3.isLogged, false)

    myUser1.logIn()

    assert(myUser1.isLogged, true)
  }
