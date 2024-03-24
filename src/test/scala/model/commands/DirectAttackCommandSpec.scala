import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import hearthstoneMini.model.playerComponent.playerImpl
import hearthstoneMini.controller.component.controllerImpl.Controller
import hearthstoneMini.model.fieldComponent.fieldImpl.Field
import hearthstoneMini.model.playerComponent.playerImpl.Player
import hearthstoneMini.model.commands.DirectAttackCommand
import hearthstoneMini.model.Move


class DirectAttackCommandSpec extends AnyWordSpec with Matchers {
  "A controller" should {
    val controller = Controller(Field(slotNum = 5, players = Map[Int, Player]((1, Player(id = 1))
      , (2, Player(id = 2)))))

    controller.placeCard(Move(handSlot = 0, fieldSlotActive = 0))
    controller.switchPlayer()
    controller.switchPlayer()
    val field = controller.field
    val directAttackCommand = new DirectAttackCommand(controller, Move(fieldSlotActive = 0))
    "do step" in {
      directAttackCommand.doStep
      directAttackCommand.memento should be (field)
    }
    "undo step" in {
      val field = controller.field
      directAttackCommand.undoStep
      directAttackCommand.memento should be (field)
    }
    "redo step" in {
      directAttackCommand.redoStep
      directAttackCommand.memento should be (field)
    }
  }
}
