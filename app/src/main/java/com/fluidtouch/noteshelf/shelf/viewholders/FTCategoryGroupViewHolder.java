package com.fluidtouch.noteshelf.shelf.viewholders;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.fluidtouch.noteshelf2.R;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class FTCategoryGroupViewHolder extends GroupViewHolder {

    private TextView groupNameTextView;
    private ImageView arrowImageView;

    public FTCategoryGroupViewHolder(View itemView) {
        super(itemView);

        groupNameTextView = itemView.findViewById(R.id.item_nd_group_name_text_view);
        arrowImageView = itemView.findViewById(R.id.item_nd_group_arrow_image_view);

    }

    public void setGroupName(String groupName) {
        groupNameTextView.setText(groupName);
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(-90,0, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrowImageView.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrowImageView.setAnimation(rotate);
    }
}
