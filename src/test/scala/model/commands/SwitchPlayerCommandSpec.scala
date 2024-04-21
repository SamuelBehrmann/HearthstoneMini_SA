package hearthstoneMini
package model.commands

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import core.controller.component.controllerImpl.Controller
import _root_.model.cardComponent.cardImpl.Card

import _root_.model.playerComponent.playerImpl.Player
import core.util.Observer
import _root_.model.fieldComponent.FieldInterface
import _root_.model.fieldComponent.fieldImpl.Field
import core.util.commands.commandImpl.SwitchPlayerCommand
import org.scalamock.scalatest.MockFactory

class SwitchPlayerCommandSpec
    extends AnyWordSpec
    with Matchers
    with MockFactory {
  "A controller" should {
    "when switching players" in {
      val controller = mock[Controller]
      val switchPlayer = new SwitchPlayerCommand(controller)
      val testField = controller.field
      switchPlayer.doStep
      switchPlayer.memento should be(testField)
    }
    "undo / redo step" in {
      val controller = mock[Controller]
      val switchPlayer = new SwitchPlayerCommand(controller)
      val testField = controller.field
      switchPlayer.undoStep
      switchPlayer.memento should be(testField)

      switchPlayer.redoStep
      switchPlayer.memento should be(testField)
    }
  }
}
