package jsky.app.ot.tpe.feat;

import edu.gemini.pot.sp.ISPObsComponent;
import edu.gemini.shared.util.immutable.None;
import edu.gemini.shared.util.immutable.Option;
import edu.gemini.shared.util.immutable.Some;
import edu.gemini.spModel.target.SPCoordinates;
import edu.gemini.spModel.target.SPSkyObject;
import edu.gemini.spModel.target.SPTarget;
import edu.gemini.spModel.target.env.TargetEnvironment;
import edu.gemini.spModel.target.obsComp.TargetObsComp;
import edu.gemini.spModel.target.obsComp.TargetSelection;
import jsky.app.ot.tpe.*;

import java.awt.*;
import java.awt.geom.Point2D;

// TODO:ASTERISM: Draw base position … right now we only draw the targets
public class TpeAsterismFeature extends TpePositionFeature {

    public TpeAsterismFeature() {
        super("Asterism", "Show the science target asterism.");
    }

    public void reinit(TpeImageWidget iw, TpeImageInfo tii) {
        super.reinit(iw, tii);

        // Tell the position map that the base position is visible.
        TpePositionMap pm = TpePositionMap.getMap(iw);
        pm.setFindAsterism(true);
    }

    public void unloaded() {
        // Tell the position map that the base position is no longer visible.
        TpePositionMap pm = TpePositionMap.getExistingMap();
        if (pm != null) pm.setFindAsterism(false);

        super.unloaded();
    }

    public boolean erase(final TpeMouseEvent tme) {
        // You can't erase the base position
        return false;
    }

    /**
     * @see jsky.app.ot.tpe.TpeSelectableFeature
     */
    public Object select(final TpeMouseEvent tme) {
        final TpePositionMap pm = TpePositionMap.getMap(_iw);

        final int x = tme.xWidget;
        final int y = tme.yWidget;

        final TargetObsComp obsComp = getTargetObsComp();
        if (obsComp != null) {
            for (final SPTarget spt: obsComp.getAsterism().allSpTargetsJava()) {
              final PosMapEntry<SPSkyObject> pme = pm.getPositionMapEntry(spt);
              if ((pme != null) && (positionIsClose(pme, x, y)) && getContext().targets().shell().isDefined()) {
                  final TargetEnvironment env = getContext().targets().envOrNull();
                  final ISPObsComponent ispObsComponent = getContext().targets().shell().get();
                  if (pme.taggedPos instanceof SPTarget)
                    TargetSelection.setTargetForNode(env, ispObsComponent, (SPTarget) pme.taggedPos);
                  else if (pme.taggedPos instanceof SPCoordinates)
                      TargetSelection.setCoordinatesForNode(env, ispObsComponent, (SPCoordinates) pme.taggedPos);
                  return pme.taggedPos;
              }
          }
        }
        return null;
    }

    private void drawItem(final Graphics g, final Color color, final Point2D.Double base) {
        if (base == null)
            return;

        final int r = MARKER_SIZE;
        final int d = 2 * r;

        g.setColor(Color.yellow);
        g.drawOval((int) (base.x - r), (int) (base.y - r), d, d);
        g.drawLine((int) base.x, (int) (base.y - r), (int) base.x, (int) (base.y + r));
        g.drawLine((int) (base.x - r), (int) base.y, (int) (base.x + r), (int) base.y);
    }

    public void draw(final Graphics g, final TpeImageInfo tii) {
        final TpePositionMap pm = TpePositionMap.getMap(_iw);

        final TargetEnvironment env = getTargetEnvironment();
        if (env == null) return;

        // Draw the targets.
        env.getAsterism().allSpTargetsJava().foreach(spt ->
            drawItem(g, Color.yellow, pm.getLocationFromTag(spt))
        );

        // Draw the sky positions.
        env.getAsterism().allSpCoordinatesJava().foreach(spc ->
            drawItem(g, Color.orange, pm.getLocationFromTag(spc))
        );
    }

    /**
     */
    public Option<Object> dragStart(final TpeMouseEvent tme, final TpeImageInfo tii) {
        final TargetEnvironment env = getTargetEnvironment();
        if (env == null) return None.instance();

        final TpePositionMap pm = TpePositionMap.getMap(_iw);

        // Look for targets close to drag position.
        for (final SPTarget spt: env.getAsterism().allSpTargetsJava()) {
            final PosMapEntry<SPSkyObject> pme = pm.getPositionMapEntry(spt);
            if (pme != null && positionIsClose(pme, tme.xWidget, tme.yWidget)) {
                _dragObject = pme;
                return new Some<>(pme.taggedPos);
            }
        }

        // Look for coordinates close to drag position.
        for (final SPCoordinates spc: env.getAsterism().allSpCoordinatesJava()) {
            final PosMapEntry<SPSkyObject> pme = pm.getPositionMapEntry(spc);
            if (pme != null && positionIsClose(pme, tme.xWidget, tme.yWidget)) {
                _dragObject = pme;
                return new Some<>(pme.taggedPos);
            }
        }

        return None.instance();
    }

    /**
     */
    public void drag(final TpeMouseEvent tme) {
        if (_dragObject != null) {
            if (_dragObject.screenPos == null) {
                _dragObject.screenPos = new Point2D.Double(tme.xWidget, tme.yWidget);
            } else {
                _dragObject.screenPos.x = tme.xWidget;
                _dragObject.screenPos.y = tme.yWidget;
            }

            final SPSkyObject tp = _dragObject.taggedPos;
            tp.setRaDecDegrees(tme.pos.ra().toDegrees(), tme.pos.dec().toDegrees());
        }
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}
