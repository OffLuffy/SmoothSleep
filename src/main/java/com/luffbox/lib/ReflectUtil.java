package com.luffbox.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>ReflectionUtils</b>
 * <p>
 * This class provides useful methods which makes dealing with reflection much easier, especially when working with Bukkit
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following conditions:
 * <ul>
 * <li>Don't claim this class as your own
 * <li>Don't remove this disclaimer
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a published project</i>
 *
 * @author DarkBlade12
 * @version 1.1
 */
@SuppressWarnings("UnusedDeclaration")
public class ReflectUtil {
	// Prevent accidental construction
	private ReflectUtil() {}

	/**
	 * Returns a method of a class with the given parameter types
	 *
	 * @param clazz Target class
	 * @param methodName Name of the desired method
	 * @param parameterTypes Parameter types of the desired method
	 * @return The method of the target class with the specified name and parameter types
	 * @throws NoSuchMethodException If the desired method of the target class with the specified name and parameter types cannot be found
	 * @see DataType#getPrimitive(Class[])
	 * @see DataType#compare(Class[], Class[])
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
		for (Method method : clazz.getMethods()) {
			if (!method.getName().equals(methodName) || !DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
				continue;
			}
			return method;
		}
		throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
	}

	/**
	 * Invokes a method on an object with the given arguments
	 *
	 * @param instance Target object
	 * @param methodName Name of the desired method
	 * @param arguments Arguments which are used to invoke the desired method
	 * @return The result of invoking the desired method on the target object
	 * @throws IllegalAccessException If the desired method cannot be accessed due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not match the parameter types of the method (this should not occur since it searches for a method with the types of the arguments)
	 * @throws InvocationTargetException If the desired method cannot be invoked on the target object
	 * @throws NoSuchMethodException If the desired method of the class of the target object with the specified name and arguments cannot be found
	 * @see #getMethod(Class, String, Class...)
	 * @see DataType#getPrimitive(Object[])
	 */
	public static Object invokeMethod(Object instance, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
	}

	/**
	 * Invokes a method of the target class on an object with the given arguments
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param methodName Name of the desired method
	 * @param arguments Arguments which are used to invoke the desired method
	 * @return The result of invoking the desired method on the target object
	 * @throws IllegalAccessException If the desired method cannot be accessed due to certain circumstances
	 * @throws IllegalArgumentException If the types of the arguments do not match the parameter types of the method (this should not occur since it searches for a method with the types of the arguments)
	 * @throws InvocationTargetException If the desired method cannot be invoked on the target object
	 * @throws NoSuchMethodException If the desired method of the target class with the specified name and arguments cannot be found
	 * @see #getMethod(Class, String, Class...)
	 * @see DataType#getPrimitive(Object[])
	 */
	public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
	}

	/**
	 * Returns a field of the target class with the given name
	 *
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The field of the target class with the specified name
	 * @throws NoSuchFieldException If the desired field of the given class cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 */
	public static Field getField(Class<?> clazz, boolean declared, String fieldName) throws NoSuchFieldException, SecurityException {
		Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
		field.setAccessible(true);
		return field;
	}

	/**
	 * Returns the value of a field of the given class of an object
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @return The value of field of the target object
	 * @throws IllegalArgumentException If the target object does not feature the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target class cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #getField(Class, boolean, String)
	 */
	public static Object getValue(Object instance, Class<?> clazz, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return getField(clazz, declared, fieldName).get(instance);
	}

	/**
	 * Sets the value of a field of the given class of an object
	 *
	 * @param instance Target object
	 * @param clazz Target class
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @param value New value
	 * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target class cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #getField(Class, boolean, String)
	 */
	public static void setValue(Object instance, Class<?> clazz, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		getField(clazz, declared, fieldName).set(instance, value);
	}

	/**
	 * Sets the value of a field with the given name of an object
	 *
	 * @param instance Target object
	 * @param declared Whether the desired field is declared or not
	 * @param fieldName Name of the desired field
	 * @param value New value
	 * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
	 * @throws IllegalAccessException If the desired field cannot be accessed
	 * @throws NoSuchFieldException If the desired field of the target object cannot be found
	 * @throws SecurityException If the desired field cannot be made accessible
	 * @see #setValue(Object, Class, boolean, String, Object)
	 */
	public static void setValue(Object instance, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		setValue(instance, instance.getClass(), declared, fieldName, value);
	}

	/**
	 * Represents an enumeration of Java data types with corresponding classes
	 * <p>
	 * This class is part of the <b>ReflectionUtils</b> and follows the same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.0
	 */
	public enum DataType {
		BYTE(byte.class, Byte.class),
		SHORT(short.class, Short.class),
		INTEGER(int.class, Integer.class),
		LONG(long.class, Long.class),
		CHARACTER(char.class, Character.class),
		FLOAT(float.class, Float.class),
		DOUBLE(double.class, Double.class),
		BOOLEAN(boolean.class, Boolean.class);

		private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<>();
		private final Class<?> primitive;
		private final Class<?> reference;

		// Initialize map for quick class lookup
		static {
			for (DataType type : values()) {
				CLASS_MAP.put(type.primitive, type);
				CLASS_MAP.put(type.reference, type);
			}
		}

		/**
		 * Construct a new data type
		 *
		 * @param primitive Primitive class of this data type
		 * @param reference Reference class of this data type
		 */
		DataType(Class<?> primitive, Class<?> reference) {
			this.primitive = primitive;
			this.reference = reference;
		}

		/**
		 * Returns the primitive class of this data type
		 *
		 * @return The primitive class
		 */
		public Class<?> getPrimitive() {
			return primitive;
		}

		/**
		 * Returns the reference class of this data type
		 *
		 * @return The reference class
		 */
		public Class<?> getReference() {
			return reference;
		}

		/**
		 * Returns the data type with the given primitive/reference class
		 *
		 * @param clazz Primitive/Reference class of the data type
		 * @return The data type
		 */
		public static DataType fromClass(Class<?> clazz) {
			return CLASS_MAP.get(clazz);
		}

		/**
		 * Returns the primitive class of the data type with the given reference class
		 *
		 * @param clazz Reference class of the data type
		 * @return The primitive class
		 */
		public static Class<?> getPrimitive(Class<?> clazz) {
			DataType type = fromClass(clazz);
			return type == null ? clazz : type.getPrimitive();
		}

		/**
		 * Returns the reference class of the data type with the given primitive class
		 *
		 * @param clazz Primitive class of the data type
		 * @return The reference class
		 */
		public static Class<?> getReference(Class<?> clazz) {
			DataType type = fromClass(clazz);
			return type == null ? clazz : type.getReference();
		}

		/**
		 * Returns the primitive class array of the given class array
		 *
		 * @param classes Given class array
		 * @return The primitive class array
		 */
		public static Class<?>[] getPrimitive(Class<?>[] classes) {
			int length = classes == null ? 0 : classes.length;
			Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getPrimitive(classes[index]);
			}
			return types;
		}

		/**
		 * Returns the reference class array of the given class array
		 *
		 * @param classes Given class array
		 * @return The reference class array
		 */
		public static Class<?>[] getReference(Class<?>[] classes) {
			int length = classes == null ? 0 : classes.length;
			Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getReference(classes[index]);
			}
			return types;
		}

		/**
		 * Returns the primitive class array of the given object array
		 *
		 * @param objects Given object array
		 * @return The primitive class array
		 */
		public static Class<?>[] getPrimitive(Object[] objects) {
			int length = objects == null ? 0 : objects.length;
			Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getPrimitive(objects[index].getClass());
			}
			return types;
		}

		/**
		 * Returns the reference class array of the given object array
		 *
		 * @param objects Given object array
		 * @return The reference class array
		 */
		public static Class<?>[] getReference(Object[] objects) {
			int length = objects == null ? 0 : objects.length;
			Class<?>[] types = new Class<?>[length];
			for (int index = 0; index < length; index++) {
				types[index] = getReference(objects[index].getClass());
			}
			return types;
		}

		/**
		 * Compares two class arrays on equivalence
		 *
		 * @param primary Primary class array
		 * @param secondary Class array which is compared to the primary array
		 * @return Whether these arrays are equal or not
		 */
		public static boolean compare(Class<?>[] primary, Class<?>[] secondary) {
			if (primary == null || secondary == null || primary.length != secondary.length) {
				return false;
			}
			for (int index = 0; index < primary.length; index++) {
				Class<?> primaryClass = primary[index];
				Class<?> secondaryClass = secondary[index];
				if (primaryClass.equals(secondaryClass) || primaryClass.isAssignableFrom(secondaryClass)) {
					continue;
				}
				return false;
			}
			return true;
		}
	}
}
