sealed trait Tournament

case class Band(name: String, popularity: Int, followers: Int, id: Int = -1) extends Tournament {
  lazy val score: Long = popularity * followers.toLong
}

case class Battle(left: Tournament, right: Tournament) extends Tournament
