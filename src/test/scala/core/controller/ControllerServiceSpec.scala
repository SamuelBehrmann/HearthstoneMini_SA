package core.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.*
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.parboiled2.RuleTrace.Run
import akka.testkit.ImplicitSender
import akka.testkit.TestActors
import akka.testkit.TestKit
import akka.testkit.TestProbe
import core.controller.Strategy.Strategy
import core.controller.component.ControllerInterface
import core.controller.component.controllerImpl.Controller
import core.controller.service.ControllerService
import core.util.CardProvider
import core.util.UndoManager
import model.GameState
import model.GameState.GameState
import model.Move
import model.cardComponent.CardInterface
import model.cardComponent.cardImpl.Card
import model.fieldComponent.FieldInterface
import model.fieldComponent.fieldImpl.Field
import model.playerComponent.PlayerInterface
import model.playerComponent.playerImpl.Player
import org.checkerframework.checker.units.qual.s
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import persistence.*
import persistence.fileIO.FileIOInterface
import persistence.fileIO.jsonIOImpl.JsonIO
import persistence.fileIO.service.PersistenceService
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import spray.json.JsString

class ControllerServiceSpec
    extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with MockFactory {

  var testCards: List[Card] = _
  var mockUndoManager: UndoManager = _
  var mockCardProvider: CardProvider = _
  var mockFileIO: FileIOInterface = _
  var controllerService: ControllerService = _
  var mockField: FieldInterface = _
  var mockController: ControllerInterface = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockCardProvider = CardProvider(inputFile = "/json/cards.json")
    mockUndoManager = mock[UndoManager]
    mockFileIO = mock[FileIOInterface]
    mockController = Controller(mockFileIO, mockUndoManager, mockCardProvider)

    testCards = List[Card](
      Card("test1", 1, 1, 1, "testEffect1", "testRarety1", 1, ""),
      Card("test1", 1, 1, 1, "testEffect1", "testRarety1", 1, ""),
      Card("test1", 1, 1, 1, "testEffect1", "testRarety1", 1, ""),
      Card("test1", 1, 1, 1, "testEffect1", "testRarety1", 1, ""),
      Card("test1", 1, 1, 1, "testEffect1", "testRarety1", 1, "")
    )
  }

  "Controller Service" should {
    "return message when calling GET /" in {
      val service = new ControllerService(using mockController)
      service.start()
      Get("/") ~> service.route ~> check {
        responseAs[
          String
        ] shouldEqual "HearthstoneMini ControllerAPI Service is online."
      }

      service.stop()
    }

    "return game state when calling GET /controller/gameState" in {
      val service = new ControllerService(using mockController)
      service.start()
      Get("/controller/gameState") ~> service.route ~> check {
        responseAs[String] shouldEqual GameState.CHOOSEMODE.toString()
      }

      service.stop()
    }

    "return field when calling GET /controller/field" in {
      val service = new ControllerService(using mockController)
      service.start()
      Get("/controller/field") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "save field when calling GET /controller/save" in {
        val service = new ControllerService(using mockController)
        service.start()
        val persistenceService = new PersistenceService()
        persistenceService.start()

        Get("/controller/save") ~> service.route ~> check {
          responseAs[String] shouldEqual "There was an internal server error."
        }

        service.stop()
        persistenceService.stop()
    }

    "load field " in {
      val service = new ControllerService(using mockController)
      service.start()
      val persistenceService = new PersistenceService()
      persistenceService.start()

      Get("/controller/load") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
      persistenceService.stop()
    }

    "draw card" in {
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/drawCard") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "switch player" in {
      (mockUndoManager.doStep _).expects(*)
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/switchPlayer") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "can undo" in {
      (mockUndoManager.canUndo _).expects().returning(false)

      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/canUndo") ~> service.route ~> check {
        responseAs[String] shouldEqual "false"
      }

      service.stop()
    }

    "can redo" in {
      (mockUndoManager.canRedo _).expects().returning(false)

      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/canRedo") ~> service.route ~> check {
        responseAs[String] shouldEqual "false"
      }

      service.stop()
    }

    "undo" in {
      (mockUndoManager.undoStep _)
        .expects(*)
        .returning(Success(mockController.field))
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/undo") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "redo" in {
      (mockUndoManager.redoStep _)
        .expects(*)
        .returning(Success(mockController.field))
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/redo") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "exit game" in {
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/exitGame") ~> service.route ~> check {
        responseAs[String] shouldEqual mockController.field.toJson.toString()
      }

      service.stop()
    }

    "return error message when calling invalid command" in {
      val service = new ControllerService(using mockController)
      service.start()

      Get("/controller/invalid") ~> service.route ~> check {
        response.status shouldEqual StatusCodes.InternalServerError
      }

      service.stop()
    }

    "post place card" in {
      (mockUndoManager.doStep _).expects(*)
      mockController.field = Field(
        players = Map[Int, Player](
          (1, Player(id = 1, manaValue = 100, hand = testCards)),
          (2, Player(id = 2))
        )
      )
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/placeCard",
        new Move().toJson.toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "set player names" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/setPlayerNames",
        Json
          .obj("playername1" -> "Player1", "playername2" -> "Player2")
          .toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "set game state" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/setGameState",
        Json.obj("gameState" -> "CHOOSEMODE").toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "attack" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/attack",
        new Move().toJson.toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "direct attack" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/directAttack",
        new Move().toJson.toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "set strategy" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/setStrategy",
        Json.obj("strategy" -> "debug").toString()
      ) ~> service.route ~> check {
        response.status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

    "return error message when calling invalid post command" in {
      val service = new ControllerService(using mockController)
      service.start()

      Post(
        "/controller/invalid",
        JsString("ahla").toString
      ) ~> service.route ~> check {
        status shouldEqual StatusCodes.OK
      }

      service.stop()
    }

  }

}