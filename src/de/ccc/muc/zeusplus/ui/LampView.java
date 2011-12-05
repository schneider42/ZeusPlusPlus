package de.ccc.muc.zeusplus.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.ccc.muc.zeusplus.service.Moodlamp;
import de.ccc.muc.zpp.R;

public class LampView extends LinearLayout {

	private Context ctx;
	private Moodlamp ml;
	private TextView tv;
	private ImageView iv;
	private boolean selected = false;

	public LampView(Context ctx, Moodlamp ml) {
		super(ctx);
		this.ctx = ctx;
		this.ml = ml;

		init();
	}

	private void init() {
		LayoutInflater layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = layoutInflater.inflate(R.layout.lamp, this);

		iv = (ImageView) v.findViewById(R.id.lampImage);
		tv = (TextView) v.findViewById(R.id.lampText);
		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				System.err.println("LOOOOOOOOOOOOOOOOONG: " + ml);
				return true;
			}
		});
		setLongClickable(true);
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleSelected();
			}
		});

		tv.setText(ml.getId());
		if (ml.getAddress().isMulticastAddress()) {
			iv.setImageResource(R.drawable.seekr);
		} else {
			iv.setImageResource(R.drawable.seekb);
		}

		invalidate();
	}

	public boolean isSelected() {
		return selected;
	}

	public void toggleSelected() {
		selected = !selected;
		if (selected) {
			setBackgroundColor(Color.parseColor("#DBD700"));
		} else {
			setBackgroundColor(Color.TRANSPARENT);
		}
	}

	public Moodlamp getMoodlamp() {
		return ml;
	}

}
