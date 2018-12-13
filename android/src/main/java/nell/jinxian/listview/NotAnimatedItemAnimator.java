package nell.jinxian.listview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Implementation of {@link RecyclerView.ItemAnimator} that disables all default animations.
 */
/*package*/ class NotAnimatedItemAnimator extends RecyclerView.ItemAnimator {

  @Override
  public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    return false;
  }

  @Override
  public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    return false;
  }

  @Override
  public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    return false;
  }

  @Override
  public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    return false;
  }

  @Override
  public void runPendingAnimations() {
    // nothing
  }

//  @Override
//  public boolean animateRemove(RecyclerView.ViewHolder holder) {
//    dispatchRemoveStarting(holder);
//    dispatchRemoveFinished(holder);
//    return true;
//  }
//
//  @Override
//  public boolean animateAdd(RecyclerView.ViewHolder holder) {
//    dispatchAddStarting(holder);
//    dispatchAddFinished(holder);
//    return true;
//  }
//
//  @Override
//  public boolean animateMove(
//      RecyclerView.ViewHolder holder,
//      int fromX,
//      int fromY,
//      int toX,
//      int toY) {
//    dispatchMoveStarting(holder);
//    dispatchMoveFinished(holder);
//    return true;
//  }
//
//  @Override
//  public boolean animateChange(
//      RecyclerView.ViewHolder oldHolder,
//      RecyclerView.ViewHolder newHolder,
//      int fromLeft,
//      int fromTop,
//      int toLeft,
//      int toTop) {
//    dispatchChangeStarting(oldHolder, true);
//    dispatchChangeFinished(oldHolder, true);
//
//    // TODO: capire perchè a volte diventa NULL
//    if (newHolder != null) {
//      dispatchChangeStarting(newHolder, false);
//      dispatchChangeFinished(newHolder, false);
//    }
//    return true;
//  }

  @Override
  public void endAnimation(RecyclerView.ViewHolder item) {
  }

  @Override
  public void endAnimations() {
  }

  @Override
  public boolean isRunning() {
    return false;
  }
}
