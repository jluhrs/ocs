package jsky.navigator;

import jsky.util.gui.SwingUtil;

/**
 * This class manages access to the Navigator window on behalf of clients.
 */
@Deprecated
public final class NavigatorManager {

    /**
     * The single Navigator, shared for all instances
     */
    private static Navigator _navigator;


    /**
     * Return the Navigator instance, if it exists, otherwise null.
     */
    public static Navigator get() {
        return _navigator;
    }

    /**
     * Open the Navigator window, creating it if necessary, and return a reference to it.
     */
    public static Navigator open() {
        if (_navigator == null && create() == null)
            return null;

        SwingUtil.showFrame(_navigator.getParentFrame());
        return _navigator;
    }


    /**
     * Create the Navigator window if necessary, and return a reference to it.
     */
    protected static Navigator create() {
        if (_navigator == null) {
            _navigator = new NavigatorFrame().getNavigator();
        }

        return _navigator;
    }
}
