import $ivy.`io.circe::circe-core:0.13.0`
import $ivy.`io.circe::circe-generic:0.13.0`
import $ivy.`io.circe::circe-parser:0.13.0`
import $ivy.`io.circe::circe-optics:0.13.0`

import java.util.Base64
import java.nio.charset.StandardCharsets

import $file.models
import models.Band
import models.Spotify._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

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

  def getArtistTournamentInfo(artistName: String): Band = {
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
        val genres = List("emo", "screamo", "punk")

        val artist = response.artists.items
          .find(_.genres.exists(genre => genres.find(g => genre.contains(g)).isDefined))
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
