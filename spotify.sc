import $ivy.`io.circe::circe-core:0.13.0`
import $ivy.`io.circe::circe-generic:0.13.0`
import $ivy.`io.circe::circe-parser:0.13.0`
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import java.util.Base64
import java.nio.charset.StandardCharsets

import $file.models
import models.Band
import Models._

object Api {

  val clientId = sys.env("SPOTIFY_CLIENT_ID")
  val clientSecret = sys.env("SPOTIFY_CLIENT_SECRET")

  private def encode(str: String): String =
    Base64.getEncoder.encodeToString(str.getBytes(StandardCharsets.UTF_8))

  // Fetch auth token once at application start up.
  private val authTokenResp = requests.post(
    "https://accounts.spotify.com/api/token",
    headers = Map(
      "Authorization" -> s"Basic ${encode(s"$clientId:$clientSecret")}"
    ),
    data = Map(
      "grant_type" -> "client_credentials"
    )
  )

  private val authToken =
    decode[AuthTokenReponse](authTokenResp.text()).map(_.access_token).right.get

  val VALID_GENRE_SUBSTRINGS = List("emo", "screamo", "punk")

  def getArtistTournamentInfo(artistName: String): Band = {
    // See: https://developer.spotify.com/documentation/web-api/reference/#category-search
    val cleanedArtist = artistName.replace(" ", "%20")
    val resp = requests
      .get(
        s"https://api.spotify.com/v1/search?q=$cleanedArtist&type=artist",
        headers = Map(
          "Authorization" -> s"Bearer $authToken"
        )
      )
      .text()

    decode[SearchResponse](resp) match {
      case Left(err) => {
        println(s"Error for artist $artistName")
        throw err
      }
      case Right(response) => {

        val artist = response.artists.items
          // This is gross, but sometimes more popular bands with a similar name show up first
          // due to heuristics in Spotify's search API. We do our best here to grab the first
          // artist that matches one of the genres above.
          .find(
            _.genres.exists(genre => VALID_GENRE_SUBSTRINGS.find(g => genre.contains(g)).isDefined)
          )
          .getOrElse {
            println(artistName)
            response.artists.items.map(_.genres).foreach(println)
            throw new Exception("Couldn't find emo or punk artist")
          }
        Band(artist.name, artist.popularity, artist.followers.total)
      }
    }
  }
}

object Models {
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
