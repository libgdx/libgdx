package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class CumulativeDistribution <T>{
	public class CumulativeValue{
		public T value;
		public float frequency;
		public float interval;
		public CumulativeValue(T value, float frequency, float interval){
			this.value = value;
			this.frequency = frequency;
			this.interval = interval;
		}
	}
	private Array<CumulativeValue> mValues;
	
	public CumulativeDistribution(Class<T> classOfT)
	{
		mValues = new Array<CumulativeValue>(false, 10, CumulativeValue.class);
	}
	
	public void add(T obj, float aIntervalSize)
	{
		mValues.add(new CumulativeValue(obj, 0, aIntervalSize));
	}
	
	public void add(T obj)
	{
		mValues.add(new CumulativeValue(obj, 0, 0));
	}
	
	public void generate()
	{	
		float sum = 0;
		for(int i=0; i < mValues.size; ++i)
		{
			sum += mValues.items[i].interval;
			mValues.items[i].frequency = sum;
		}
	}
	
	/** Generate the cumulative distribution which will be normalized.
	 * This means that each interval will get a frequency in [0,1] */
	public void generateNormalized()
	{	
		float sum = 0;
		for(int i=0; i < mValues.size; ++i)
		{
			sum += mValues.items[i].interval;
		}
		float intervalSum = 0;
		for(int i=0; i < mValues.size; ++i)
		{
			intervalSum += mValues.items[i].interval/sum;
			mValues.items[i].frequency = intervalSum;
		}
		
	}
	
	public void normalize()
	{
		float freq = 1f/mValues.size;
		for(int i=0; i < mValues.size; ++i)
		{
			//reset the interval to the normalized frequency
			mValues.items[i].interval = freq;
			mValues.items[i].frequency=(i+1)*freq;
		}
	}
	
	public T value(float aProbability)
	{
		CumulativeValue value = null;
		int imax = mValues.size-1, imin = 0, imid;
		while (imin <= imax)
		{
			imid = imin + ((imax - imin) / 2);
			value = mValues.items[imid];
			if(aProbability < value.frequency)
				imax = imid -1;
			else if(aProbability > value.frequency)
				imin = imid +1;
			else break;
		}
		
		return value.value;
	}
	
	public T value()
	{
		return value(MathUtils.random());
	}

	public int size(){
		return mValues.size;
	}
	
	public float getInterval(int aIndex)
	{
		return mValues.items[aIndex].interval;
	}
	
	public T getValue(int aIndex)
	{
		return mValues.items[aIndex].value;
	}
	
	public void setInterval(T obj, float aIntervalSize)
	{
		for(CumulativeValue value : mValues)
			if(value.value == obj){
				value.interval = aIntervalSize;
				break;
			}
	}
	
	public void setInterval(int aIndex, float aIntervalSize)
	{
		mValues.items[aIndex].interval = aIntervalSize;
	}
	
	public void clear(){
		mValues.clear();
	}
}
