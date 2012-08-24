package ru.scoria.configurations.transformers;

import java.lang.reflect.Field;

import ru.scoria.configurations.PropertyTransformer;
import ru.scoria.configurations.TransformFactory;
import ru.scoria.configurations.TransformationException;
import ru.scoria.util.ArrayUtils;

public class IntArrayTransformer implements PropertyTransformer<int []> {
	static {
		TransformFactory.registerTransformer(int[].class, new IntArrayTransformer());
	}
	@Override
	public int[] transform(String value, Field field, Object... data)
			throws TransformationException {
		int [] result = {};
		for(String s : value.split(",")) try {
			result = ArrayUtils.add(result, Integer.valueOf(s.trim()));
		} catch(NumberFormatException nfe  ) {} 
			
		return result;
	}

}
