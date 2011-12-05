package de.ccc.muc.zeusplus.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import de.ccc.muc.zeusplus.service.Moodlamp;
import de.ccc.muc.zeusplus.service.MoodlampRegistryListener;

public class LampsView extends LinearLayout implements MoodlampRegistryListener {

	private Context ctx;
	private Set<Moodlamp> mls = new TreeSet<Moodlamp>();

	public LampsView(Context ctx) {
		super(ctx);
		this.ctx = ctx;

		init();
	}

	public LampsView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		this.ctx = ctx;

		init();
	}

	private void init() {
	}

	@Override
	synchronized public void moodlampAdded(final Moodlamp ml) {
		mls.add(ml);
		int i = 0;
		for (Moodlamp mood : mls) {
			if (ml.equals(mood)) {
				break;
			}
			i++;
		}
		addView(new LampView(ctx, ml), i);
	}

	@Override
	synchronized public void moodlampsRemoved() {
		removeAllViews();
		mls.clear();
	}

	synchronized public List<Moodlamp> getSelected() {
		List<Moodlamp> moods = new LinkedList<Moodlamp>();
		LampView lv;
		for (int i = 0; i < getChildCount(); i++) {
			lv = (LampView) getChildAt(i);
			if (lv.isSelected()) {
				moods.add(lv.getMoodlamp());
			}
		}
		return moods;
	}
}
