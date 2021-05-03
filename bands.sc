import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import $file.spotify
import $file.models

import models._

// Order in this list matters. Bands are recursively matched pair-wise.
// For example, [a, b, c, d] == ((a vs b) vs (c vs d))
// format: off
val bandNames: List[String] = List(
  "blink-182", "home grown", "a day to remember", "coheed and cambria",
  "panic! at the disco", "from first to last", "the used", "hawthorne heights",
  "simple plan", "bayside", "yellowcard", "the rocket Summer",
  "boys like girls", "senses fail", "brand new", "cute is what we aim for",
  "my chemical romance", "the red jumpsuit apparatus", "afi", "the academy is",
  "good charlotte", "plain white ts", "alkaline trio", "mae",
  "the starting line", "hidden in plain view", "sum-41", "the maine",
  "cartel", "acceptance", "jimmy eat world", "finch",
  "green Day", "spitalfied", "relient k", "saves the Day",
  "paramore", "hellogoodbye", "underoath", "saosin",
  "midtown", "story of the year", "new found glory", "the spill canvas",
  "something corporate", "thursday", "taking back sunday", "armor for sleep",
  "fall out boy", "forever the sickest kids", "motion city soundtrack", "say anything",
  "the all-american rejects", "silverstein", "the get up kids", "four year strong",
  "mayday parade", "the early november", "all time low", "matchbook romance",
  "the ataris", "the movielife", "dashboard confessionals", "allister"
)
// format: on

val BANDS_DATA_FILE = os.pwd / "bands-data.jsonl"

def getBands(): List[Band] = bandNames
  .map { artist =>
    Thread.sleep(100) // An attempt at being respecful of api rate limits uwu.
    spotify.Api.getArtistTournamentInfo(artist)
  }
  // Cache bands for repeat runs.
  .tapEach(b => os.write.append(BANDS_DATA_FILE, b.asJson.noSpaces + "\n"))

val fromFile = os.read
  .lines(BANDS_DATA_FILE)
  .map(decode[Band])
  .map(_.right.get)
  .toList

val data = if (fromFile.isEmpty) getBands() else fromFile
