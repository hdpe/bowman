package uk.co.blackpepper.bowman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;

class DefaultPropertyValueFactory implements PropertyValueFactory {

	@Override
	public <T extends Collection<?>> T createCollection(Class<?> collectionType) {
		Object collection = null;
		
		if (Collection.class.isAssignableFrom(collectionType) && !collectionType.isInterface()) {
			collection = BeanUtils.instantiate(collectionType);
		}
		else if (SortedSet.class.equals(collectionType)) {
			collection = new TreeSet<>();
		}
		else if (Set.class.equals(collectionType)) {
			collection = new LinkedHashSet<>();
		}
		else if (List.class.equals(collectionType) || Collection.class.equals(collectionType)) {
			collection = new ArrayList<>();
		}
		else {
			throw new ClientProxyException(String.format("Unsupported Collection type: %s", collectionType.getName()));
		}
		
		@SuppressWarnings("unchecked")
		T result = (T) collection;
		
		return result;
	}
}
