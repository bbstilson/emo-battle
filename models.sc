sealed trait Tournament

case class Band(name: String, popularity: Int, followers: Int) extends Tournament {
  lazy val score: Long = popularity * followers.toLong
}

case class Battle(left: Tournament, right: Tournament) extends Tournament
