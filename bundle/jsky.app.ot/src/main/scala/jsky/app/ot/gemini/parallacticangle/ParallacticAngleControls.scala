package jsky.app.ot.gemini.parallacticangle

import java.awt.{Color, Insets}
import java.text.{Format, SimpleDateFormat}
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.border.EtchedBorder

import edu.gemini.skycalc.Angle
import edu.gemini.spModel.core.Site
import edu.gemini.spModel.inst.ParallacticAngleSupport
import edu.gemini.spModel.obs.{ObsTargetCalculatorService, SPObservation, SchedulingBlock}
import edu.gemini.spModel.rich.shared.immutable._
import edu.gemini.shared.util.immutable.{Option => JOption, ImOption}
import jsky.app.ot.ags.BagsManager
import jsky.app.ot.editor.OtItemEditor
import jsky.app.ot.gemini.editor.EphemerisUpdater
import jsky.app.ot.util.TimeZonePreference
import jsky.util.gui.DialogUtil

import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing._
import scala.swing.event.{ButtonClicked, Event}

import scalaz.syntax.std.boolean._, scalaz.syntax.apply._, scalaz.effect.IO

/**
 * This class encompasses all of the logic required to manage the average parallactic angle information associated
 * with an instrument configuration.
 *
 * @param isPaUi should be true for PA controls, false for Scheduling Block controls
 */
class ParallacticAngleControls(isPaUi: Boolean) extends GridBagPanel with Publisher {

  val Nop = new Runnable { def run = () }

  private var editor:    Option[OtItemEditor[_, _]] = None
  private var site:      Option[Site]   = None
  private var formatter: Option[Format] = None
  private var callback:  Runnable = Nop

  object ui {
    object relativeTimeMenu extends Menu("Set To:") {
      private val incrementsInMinutes = List(5, 10, 20, 30, 45, 60)

      private case class RelativeTime(desc: String, timeInMs: Long) extends MenuItem(desc) {
        action = Action(desc) {
          val start = System.currentTimeMillis + timeInMs
          val sb    = schedulingBlock.fold(SchedulingBlock(start))(sb => SchedulingBlock(start, sb.duration))
          updateSchedulingBlock(sb)
        }
      }

      horizontalTextPosition = Alignment.Left
      horizontalAlignment    = Alignment.Left
      iconTextGap            = 10
      icon                   = Resources.getIcon("eclipse/menu-trimmed.gif")
      margin                 = new Insets(-1, -10, -1, -5)

      def rebuild(): Unit = {
        contents.clear()

        // menu items that don't depend on the context
        val fixedItems = RelativeTime("Now", 0) :: incrementsInMinutes.map(m => RelativeTime(s"Now + $m min", m * 60000))

        // menu items that require an observation and instrument to compute
        val instItems = for {
          e    <- editor if isPaUi // we don't want these for scheduling block ui
          obs  <- Option(e.getContextObservation)
          inst <- Option(e.getContextInstrumentDataObject)
        } yield {
          // For some ridiculous reason, setup and reacq time is provided as
          // floating point seconds by the instrument implementations :/
          val setupTimeMs = math.round(inst.getSetupTime(obs) * 1000)
          val reacqTimeMs = math.round(inst.getReacquisitionTime(obs) * 1000)
          def formatMin(ms: Long): String = s"(${math.round(ms/60000.0)} min)"

          List(
            RelativeTime(s"Now + Setup ${formatMin(setupTimeMs)}",  setupTimeMs),
            RelativeTime(s"Now + Reacq. ${formatMin(reacqTimeMs)}", reacqTimeMs)
          )
        }

        contents ++= instItems.getOrElse(Nil) ++ fixedItems
      }
    }

    private object relativeTimeMenuBar extends MenuBar {
      contents    += relativeTimeMenu
      border      =  BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
      minimumSize =  preferredSize
      tooltip     = "Select a duration for the average parallactic angle calculation from the current time."
    }
    layout(relativeTimeMenuBar) = new Constraints() {
      anchor = Anchor.West
    }

    object dateTimeButton extends Button {
      icon    = Resources.getIcon("dates.gif")
      tooltip = "Select the time and duration for the average parallactic angle calculation."
    }
    layout(dateTimeButton) = new Constraints() {
      gridx  = 1
      anchor = Anchor.West
      insets = new Insets(0, 10, 0, 0)
    }
    listenTo(dateTimeButton)
    reactions += {
      case ButtonClicked(`dateTimeButton`) => displayParallacticAngleDialog()
    }

    object parallacticAngleFeedback extends Label {
      foreground             = Color.black
      horizontalAlignment    = Alignment.Left
      iconTextGap            = iconTextGap - 2

      def warningState(warn: Boolean): Unit =
        icon = if (warn) Resources.getIcon("eclipse/alert.gif") else Resources.getIcon("eclipse/blank.gif")
    }
    layout(parallacticAngleFeedback) = new Constraints() {
      gridx   = 2
      weightx = 1.0
      anchor  = Anchor.West
      fill    = Fill.Horizontal
      insets  = new Insets(0, 10, 0, 0)
    }
  }


  /**
   * Initialize the UI and set the instrument editor to allow for the parallactic angle updates.
   * The `Runnable` is a callback that will be invoked on the EDT after the target is updated.
   */
  def init(e: OtItemEditor[_, _], s: Option[Site], f: Format, c: Runnable): Unit = {
    editor    = Some(e)
    site      = s
    formatter = Some(f)
    callback  = c
    ui.relativeTimeMenu.rebuild()
    resetComponents()
  }

  def init(e: OtItemEditor[_, _], s: Site, f: Format): Unit =
    init(e, Some(s), f, Nop)

  def init(e: OtItemEditor[_, _], s: JOption[Site], f: Format): Unit =
    init(e, s.asScalaOpt, f, Nop)

  def init(e: OtItemEditor[_, _], s: JOption[Site], f: Format, callback: Runnable): Unit =
    init(e, s.asScalaOpt, f, callback)

  /** Current scheduling block, if any. */
  private def schedulingBlock: Option[SchedulingBlock] =
    for {
      e   <- editor
      obs <- Option(e.getContextObservation)
      sb  <- obs.getDataObject.asInstanceOf[SPObservation].getSchedulingBlock.asScalaOpt
    } yield sb

  /** Replace the scheduling block. */
  private def updateSchedulingBlock(sb: SchedulingBlock): Unit =
    for {
      e      <- editor
      ispObs <- Option(e.getContextObservation)
    } {
      val spObs = ispObs.getDataObject.asInstanceOf[SPObservation]
      val sameNight = spObs.getSchedulingBlock.asScalaOpt.exists(_.sameObservingNightAs(sb))

      // IO action on EDT
      def edt[A](a: => Unit): IO[Unit] =
        IO(Swing.onEDT(a))

      // The update we want to do
      val action: IO[Unit] =
        for {
          _ <- IO(spObs.setSchedulingBlock(ImOption.apply(sb)))
          _ <- IO(ispObs.setDataObject(spObs))
          _ <- sameNight.unlessM(EphemerisUpdater.refreshEphemerides(ispObs, e.getWindow))
          _ <- edt(callback.run())
          _ <- edt(resetComponents())
        } yield ()

      // Without sending events
      val quiet: IO[Unit] =
        IO(ispObs.setSendingEvents(false)) *> action ensuring IO(ispObs.setSendingEvents(true))

      // And with an exception handler
      val safe: IO[Unit] =
        quiet except { case t: Throwable => edt(DialogUtil.error(peer, t)) }

      // Run it on a short-lived worker
      new Thread(new Runnable() {
        def run = safe.unsafePerformIO
      }).start()

    }


  /**
   * Triggered when the date time button is clicked. Shows the ParallacticAngleDialog to allow the user to
   * explicitly set a date and duration for the parallactic angle calculation.
   */
  private def displayParallacticAngleDialog(): Unit =
    for {
      e <- editor
      o <- Option(e.getContextObservation)
    } {
      val dialog = new ParallacticAngleDialog(
        e.getViewer.getParentFrame,
        o,
        o.getDataObject.asInstanceOf[SPObservation].getSchedulingBlock.asScalaOpt,
        site.map(_.timezone),
        isPaUi)
      dialog.pack()
      dialog.visible = true
      updateSchedulingBlock(dialog.schedulingBlock)
    }


  /**
   * This should be called whenever the position angle changes to compare it to the parallactic angle.
   * A warning icon is displayed if the two are different. This is a consequence of allowing the user to
   * set the PA to something other than the parallactic angle, even when it is selected.
   */
  def positionAngleChanged(positionAngleText: String): Unit = {
    // We only do this if the parallactic angle can be calculated, and is different from the PA.
    for {
      e     <- editor
      angle <- parallacticAngle
      fmt   <- formatter
    } yield {
      val explicitlySet = !fmt.format(ParallacticAngleControls.angleToDegrees(angle)).equals(positionAngleText) &&
                          !fmt.format(ParallacticAngleControls.angleToDegrees(angle.add(Angle.ANGLE_PI))).equals(positionAngleText)
      ui.parallacticAngleFeedback.warningState(explicitlySet)
    }
  }


  /**
   * This should be called whenever the parallactic angle components need to be reinitialized, and at initialization.
   */
  def resetComponents(): Unit = {
    ui.parallacticAngleFeedback.text = ""
    for {
      e              <- editor
      ispObservation <- Option(e.getContextObservation)
      spObservation  = ispObservation.getDataObject.asInstanceOf[SPObservation]
      sb             <- spObservation.getSchedulingBlock.asScalaOpt
      fmt            <- formatter
    } {
      //object dateFormat extends SimpleDateFormat("MM/dd/yy 'at' HH:mm:ss z") {
      object dateFormat extends SimpleDateFormat("MM/dd/yy HH:mm:ss z") {
        setTimeZone(TimeZonePreference.get)
      }
      val dateTimeStr = dateFormat.format(new Date(sb.start))

      // Include tenths of a minute if not even.
      val duration = sb.duration.getOrElse(ParallacticAngleDialog.calculateRemainingTime(ispObservation)) / 60000.0
      val durationFmt = if (Math.round(duration * 10) == (Math.floor(duration) * 10).toLong) "%.0f" else "%.1f"
      val when = s"$dateTimeStr"

      ui.parallacticAngleFeedback.text =
        e.getDataObject match {
          case p: ParallacticAngleSupport if isPaUi =>
            parallacticAngle.fold(
              s"Target not visible ($when)")(angle =>
              s"${fmt.format(ParallacticAngleControls.angleToDegrees(angle))}\u00b0 ($when, ${durationFmt.format(duration)}m)")
          case _ =>
            s"$when"
        }

      publish(ParallacticAngleControls.ParallacticAngleChangedEvent)
    }
  }


  /**
   * The parallactic angle calculation, if it can be calculated
   */
  def parallacticAngle: Option[Angle] =
    for {
      e <- editor
      o <- Option(e.getContextObservation)
      a <- e.getDataObject match {
        case p: ParallacticAngleSupport => p.calculateParallacticAngle(o).asScalaOpt
        case _                          => None
      }
    } yield a

  override def enabled_=(b: Boolean): Unit = {
    super.enabled_=(b)
    ui.relativeTimeMenu.enabled = b
    ui.dateTimeButton.enabled = b
  }

}

object ParallacticAngleControls {
  case object ParallacticAngleChangedEvent extends Event

  def angleToDegrees(a: Angle): Double = a.toPositive.toDegrees.getMagnitude
}