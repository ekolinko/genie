package edu.ucsd.genie.userinterface.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import edu.ucsd.genie.R;
import edu.ucsd.genie.typemanager.UnitsManager;

public class MeterView extends View {

	/**
	 * The rectangle for the arc area.
	 */
	private RectF mArcArea;

	/**
	 * The rectangle for the needle base area.
	 */
	private RectF mNeedleBaseArea;

	/**
	 * The needle origin.
	 */
	private PointF mNeedleOrigin;

	/**
	 * The paint associated with painting the meter arc.
	 */
	private final Paint mArcPaint = new Paint();

	/**
	 * The width of the arc.
	 */
	private float mArcStrokeWidth = 0;

	/**
	 * The x-radius of the arc.
	 */
	private float mArcXRadius;

	/**
	 * The y-radius of the arc.
	 */
	private float mArcYRadius;

	/**
	 * The paint associated with painting the meter needle.
	 */
	private final Paint mNeedlePaint = new Paint();

	/**
	 * The paint associated with painting the meter needle base.
	 */
	private final Paint mNeedleBasePaint = new Paint();
	
	/**
	 * The units associated with this view. This value determines the maximum value of the meter.
	 */
	private String mUnits;
	
	/**
	 * The value associated with this view. This value determines the location of the needle.
	 */
	private double mValue = 0;

	public MeterView(Context context) {
		super(context);
		initialize();
	}

	public MeterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public MeterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	/**
	 * Initialize the paint for drawing the arc and needle.
	 */
	private void initialize() {

		// Initialize arc paint
		mArcStrokeWidth = getContext().getResources().getDimension(
				R.dimen.meter_view_arc_stroke_width);
		mArcPaint.setStrokeWidth(mArcStrokeWidth);
		mArcPaint.setColor(getContext().getResources().getColor(
				R.color.genie_blue));
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeCap(Paint.Cap.BUTT);
		mArcPaint.setAntiAlias(true);

		// Initialize needle paint
		mNeedlePaint.setStrokeWidth(getContext().getResources().getDimension(
				R.dimen.meter_view_needle_stroke_width));
		mNeedlePaint.setColor(getContext().getResources().getColor(
				R.color.black));
		mNeedlePaint.setStyle(Paint.Style.STROKE);
		mNeedlePaint.setAntiAlias(true);

		// Initialize needle base paint
		mNeedleBasePaint.setColor(getContext().getResources().getColor(
				R.color.black));
		mNeedleBasePaint.setStyle(Paint.Style.FILL);
		mNeedleBasePaint.setAntiAlias(true);
	}

	/**
	 * Update the meter rectangle when the size changes.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		float offset = mArcStrokeWidth / 2;
		float needleBaseRadius = getContext().getResources().getDimension(
				R.dimen.meter_view_needle_base_radius);
		mArcArea = new RectF(offset, offset, w - offset, 2 * (h - needleBaseRadius));
		mArcXRadius = w / 2 - offset;
		mArcYRadius = h - 2 * offset;
		mNeedleBaseArea = new RectF(w / 2 - needleBaseRadius, h - 2
				* needleBaseRadius, w / 2 + needleBaseRadius, h);
		mNeedleOrigin = new PointF(w / 2, h - needleBaseRadius);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mArcArea != null)
			canvas.drawArc(mArcArea, 180, 180, false, mArcPaint);

		if (mNeedleBaseArea != null)
			canvas.drawArc(mNeedleBaseArea, 90, 360, false, mNeedleBasePaint);

		if (mNeedleOrigin != null) {
			PointF endPoint = getNeedleEndPosition((mValue - UnitsManager.getInstance(getContext()).getMinValueForUnits(mUnits)) / UnitsManager.getInstance(getContext()).getMaxValueForUnits(mUnits));
			canvas.drawLine(mNeedleOrigin.x, mNeedleOrigin.y, endPoint.x,
					endPoint.y, mNeedlePaint);
		}
	}

	/**
	 * Get the end position of the needle dependent on the specified value (0 to 1.0).
	 * 
	 * @param value the value of the meter view from 0 to 1.0.
	 * @return the end position of the needle corresponding to the specified value.
	 */
	public PointF getNeedleEndPosition(double value) {
		// convert the value for an angle
		double angle = Math.PI - value * Math.PI;
		float a = mArcXRadius;
		float b = mArcYRadius;
		return new PointF(mNeedleOrigin.x + (float) (a * Math.cos(angle)),
				mNeedleOrigin.y - (float) (b * Math.sin(angle)));
	}

	public void setValue(double value) {
		mValue = value;
	}
	
	public void setUnits(String units) {
		mUnits = units;
	}
}
