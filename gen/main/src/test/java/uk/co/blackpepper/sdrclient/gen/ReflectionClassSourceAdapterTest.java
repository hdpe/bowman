package uk.co.blackpepper.sdrclient.gen;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

public class ReflectionClassSourceAdapterTest {

  @interface Annotation {
  }
  
  class Field {
  }
  
  class SimpleTypes {
    
    private String field;
  }
  
  @Test
  public void getFieldsReturnsFieldsWithSimpleTypes() {
    ReflectionClassSourceAdapter adapter = new ReflectionClassSourceAdapter(SimpleTypes.class);
    
    assertThat(adapter.getFields().iterator().next().getQualifiedTypeNameWithGenerics(),
        is("java.lang.String"));
  }
  
  class GenericTypes {
    
    private final Set<Field> field = new LinkedHashSet<Field>(); 
  }
  
  @Test
  @Ignore
  public void getFieldsReturnsFieldsWithGenericTypes() {
    ReflectionClassSourceAdapter adapter = new ReflectionClassSourceAdapter(GenericTypes.class);
    
    assertThat(adapter.getFields().iterator().next().getQualifiedTypeNameWithGenerics(),
        is("java.util.Set<uk.co.blackpepper.sdrclient.gen.Field>"));
  }
}
