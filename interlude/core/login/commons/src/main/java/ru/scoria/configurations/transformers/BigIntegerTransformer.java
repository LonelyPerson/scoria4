package ru.scoria.configurations.transformers;

import java.lang.reflect.Field;
import java.math.BigInteger;

import ru.scoria.configurations.PropertyTransformer;
import ru.scoria.configurations.TransformFactory;
import ru.scoria.configurations.TransformationException;

public class BigIntegerTransformer implements PropertyTransformer<BigInteger>{
	static {
		TransformFactory.registerTransformer(BigInteger.class, new BigIntegerTransformer());
	}
	@Override
	public BigInteger transform(String value, Field field, Object... data)
			throws TransformationException {
		return new BigInteger(value);
	}
	

}
