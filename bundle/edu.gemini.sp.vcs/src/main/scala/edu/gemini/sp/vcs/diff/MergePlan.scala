package edu.gemini.sp.vcs.diff

import edu.gemini.pot.sp.ISPProgram

import scalaz._

/** Describes the modifications required for a local program to complete a
  * merge.
  */
case class MergePlan(update: Tree[MergeNode], delete: Set[Missing]) {

  /** Accepts a program and edits it according to this merge plan. */
  def merge(p: ISPProgram): Unit = ???
}