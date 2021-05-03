import $file.models
import $file.bands

import models._

import scala.annotation.tailrec

@tailrec
def buildTournament(battles: List[Tournament]): Tournament = battles match {
  case Nil            => ??? // This shouldn't happen. It would indicate an unbalanced battle.
  case tourney :: Nil => tourney
  case bs =>
    buildTournament {
      bs.sliding(2, 2).toList.collect { case List(left, right) =>
        Battle(left, right)
      }
    }
}

def bandBattle(b1: Band, b2: Band, tier: Int): Band = {
  val padding = ("- - " * tier) + " "
  val winner = if (b1.score > b2.score) b1 else b2
  println(padding + s"${b1.name} vs ${b2.name}. Winner is ${winner.name}.")
  winner
}

def runTournament(tournament: Tournament): Band = {
  def run(t: Tournament, tier: Int): Band = {
    t match {
      case Battle(b1: Band, b2: Band) => bandBattle(b1, b2, tier)
      case Battle(left, right) => {
        val nextTier = tier + 1
        val winnerLeft = run(left, nextTier)
        val winnerRight = run(right, nextTier)
        bandBattle(winnerLeft, winnerRight, tier)
      }
    }
  }
  run(tournament, 0)
}

val competitors = bands.data
val tournament = buildTournament(competitors)
val winner = runTournament(tournament)
println()
println("Champion: " + winner.name + "!")
