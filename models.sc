sealed trait Tournament

case class Band(name: String, popularity: Int, followers: Int, id: Int = -1) extends Tournament {
  lazy val score: Long = popularity * followers.toLong
}

case class Battle(left: Tournament, right: Tournament) extends Tournament

object Spotify {
  case class AuthTokenReponse(access_token: String)
  case class Image(height: Int, width: Int, url: String)
  case class Followers(total: Int)

  case class ArtistItem(
    genres: List[String],
    followers: Followers,
    href: String,
    id: String,
    images: List[Image],
    name: String,
    popularity: Int,
    `type`: String,
    uri: String
  )

  case class SearchRseponseArtists(
    href: String,
    items: List[ArtistItem]
  )

  case class SearchResponse(
    artists: SearchRseponseArtists
  )
}
