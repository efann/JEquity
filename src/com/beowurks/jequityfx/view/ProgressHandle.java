/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.view;

import java.util.logging.Logger;

public final class ProgressHandle
{

  private static final Logger LOG = Logger.getLogger(ProgressHandle.class.getName());


  public static ProgressHandle createHandle(final String displayName)
  {
    return createHandle(displayName);
  }


  public final void start()
  {
    this.start(0, -1);
  }

  public final void start(final int workunits)
  {
    this.start(workunits, -1);
  }


  public final void start(final int workunits, final long estimate)
  {

  }


  public final void switchToIndeterminate()
  {

  }

  public final void switchToDeterminate(final int workunits)
  {
  }

  public final void finish()
  {

  }


  /**
   * Notify the user about completed workunits.
   * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
   *
   * @param workunit a cumulative number of workunits completed so far
   */
  public final void progress(final int workunit)
  {
    this.progress(null, workunit);
  }

  /**
   * Notify the user about progress by showing message with details.
   * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
   *
   * @param message details about the status of the task
   */
  public final void progress(final String message)
  {
    this.progress(message);
  }

  public final void progress(final String message, final int workunit)
  {
  }


  /**
   * Change the display name of the progress task. Use with care, please make sure the changed name is not completely different,
   * or otherwise it might appear to the user as a different task.
   *
   * @param newDisplayName a new name to set for the task
   * @since org.netbeans.api.progress 1.5
   */
  public final void setDisplayName(final String newDisplayName)
  {
  }

  /**
   * for unit testing only..
   */
  String getDisplayName()
  {
    return ("");
  }

}
