package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.Array;

/** @author Inferno */
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
	private Array<CumulativeValue> values;

	public CumulativeDistribution(){
		values = new Array<CumulativeValue>(false, 10, CumulativeValue.class);
	}

	public void add(T obj, float intervalSize){
		values.add(new CumulativeValue(obj, 0, intervalSize));
	}

	public void add(T obj){
		values.add(new CumulativeValue(obj, 0, 0));
	}

	/** Generate the cumulative distribution */
	public void generate(){	
		float sum = 0;
		for(int i=0; i < values.size; ++i){
			sum += values.items[i].interval;
			values.items[i].frequency = sum;
		}
	}

	/** Generate the cumulative distribution in [0,1] where each interval will get a frequency */
	public void generateNormalized(){	
		float sum = 0;
		for(int i=0; i < values.size; ++i){
			sum += values.items[i].interval;
		}
		float intervalSum = 0;
		for(int i=0; i < values.size; ++i){
			intervalSum += values.items[i].interval/sum;
			values.items[i].frequency = intervalSum;
		}		
	}

	/** Generate the cumulative distribution in [0,1] where each value will have the same interval size */
	public void generateUniform(){
		float freq = 1f/values.size;
		for(int i=0; i < values.size; ++i){
			//reset the interval to the normalized frequency
			values.items[i].interval = freq;
			values.items[i].frequency=(i+1)*freq;
		}
	}


	/** @param probability is the value used to search the right interval inside the distribution 
	 * @return the value whose interval contains the probability */
	public T value(float probability){
		CumulativeValue value = null;
		int imax = values.size-1, imin = 0, imid;
		while (imin <= imax){
			imid = imin + ((imax - imin) / 2);
			value = values.items[imid];
			if(probability < value.frequency)
				imax = imid -1;
			else if(probability > value.frequency)
				imin = imid +1;
			else break;
		}

		return value.value;
	}

	/** @return a random value whose probability lies in [0,1] */
	public T value(){
		return value(MathUtils.random());
	}

	public int size(){
		return values.size;
	}

	public float getInterval(int index){
		return values.items[index].interval;
	}

	public T getValue(int index){
		return values.items[index].value;
	}

	/** Set the interval size on the passed in object.
	 *  The object has to be inside the distribution. */
	public void setInterval(T obj, float intervalSize){
		for(CumulativeValue value : values)
			if(value.value == obj){
				value.interval = intervalSize;
				return;
			}
	}

	public void setInterval(int index, float intervalSize){
		values.items[index].interval = intervalSize;
	}

	public void clear(){
		values.clear();
	}
}
