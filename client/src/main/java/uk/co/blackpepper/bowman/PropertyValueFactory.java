package uk.co.blackpepper.bowman;

import java.util.Collection;

interface PropertyValueFactory {

	<T extends Collection<?>> T createCollection(Class<?> collectionType);
}
