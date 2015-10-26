package edu.ucsd.genie.typemanager;

import java.util.HashMap;

import android.content.Context;
import edu.ucsd.genie.R;

public class UnitsManager {

	/**
	 * The context of the application.
	 */
	private final Context mContext;
	
	/**
	 * Mapping from type to maximum value of the type.
	 */
	private final HashMap<String, Units> mUnits = new HashMap<String, Units>();
	
	/**
	 * The default maximum if no predefined type is found.
	 */
	private Units mDefaultUnits;
	
	private static UnitsManager instance = null;
	
	private UnitsManager(Context context) {
		mContext = context;
		initialize();
	}

	public static UnitsManager getInstance(Context context) {
		if (instance == null) {
			synchronized (UnitsManager.class) {
				if (instance == null) {
					instance = new UnitsManager(context);
				}
			}
		}
		return instance;
	}
	
	/**
	 * Initialize the maximum values of the types.
	 */
	private void initialize() {
		mUnits.put(mContext.getString(R.string.units_kilowatts), new Units(mContext.getResources().getDimension(R.dimen.units_kilowatts_min), mContext.getResources().getDimension(R.dimen.units_kilowatts_max)));
		mUnits.put(mContext.getString(R.string.units_fahrenheit), new Units(mContext.getResources().getDimension(R.dimen.units_fahrenheit_min), mContext.getResources().getDimension(R.dimen.units_fahrenheit_max)));
		mDefaultUnits = new Units(mContext.getResources().getDimension(R.dimen.units_default_min), mContext.getResources().getDimension(R.dimen.units_default_max));
	}
	
	public float getMinValueForUnits(String units) {
		if (mUnits.containsKey(units))
			return mUnits.get(units).getMin();
		
		return mDefaultUnits.getMin();
	}
	
	/**
	 * Get the maximum value for the specified type.
	 * 
	 * @param units the type for which to get the maximum value.
	 * @return the maximum value for the specified type.
	 */
	public float getMaxValueForUnits(String units) {
		if (mUnits.containsKey(units))
			return mUnits.get(units).getMax();
		
		return mDefaultUnits.getMax(); 
	}
	
	/**
	 * Class representing units with minimum and maximum values.
	 */
	private class Units {
		/**
		 * The minimum value of the units.
		 */
		private final float mMin;
		
		/**
		 * The maximum value of the units.
		 */
		private final float mMax;
		
		public Units(float min, float max) {
			mMin = min;
			mMax = max;
		}

		public float getMin() {
			return mMin;
		}

		public float getMax() {
			return mMax;
		}
	}
}
