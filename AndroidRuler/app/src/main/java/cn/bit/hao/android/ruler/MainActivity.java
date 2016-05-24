package cn.bit.hao.android.ruler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

	private RulerView rulerView;
	private ImageView rotateIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findView();
		initView();
		setListener();
	}

	private void findView() {
		rulerView = (RulerView) findViewById(R.id.ruler_view);
		rotateIcon = (ImageView) findViewById(R.id.rotate);
	}

	private void initView() {
//		rotateIcon.setImageResource(rulerView.isOrientationVertical() ?
//				R.drawable.ic_rotate_left_black_48dp : R.drawable.ic_rotate_right_black_48dp);
	}

	private void setListener() {
		rotateIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rulerView.setRotateDegree(rulerView.getRotateDegree() + 90);
				rotateIcon.setRotation(rulerView.getRotateDegree());
//				rulerView.setOrientationVertical(!rulerView.isOrientationVertical());
//				rotateIcon.setImageResource(rulerView.isOrientationVertical() ?
//						R.drawable.ic_rotate_left_black_48dp : R.drawable.ic_rotate_right_black_48dp);
			}
		});
	}

}

