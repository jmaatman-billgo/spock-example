package com.billgo.example.spock;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

interface DependencyOne {
  List<SomeObject> getMatchingObjects(String someParameter) throws RuntimeException;
}

interface DependencyTwo {
  void saveObjects(List<SomeObject> objectsToSave);
}

@Getter @Setter @EqualsAndHashCode @ToString
class SomeObject {
  private String propertyOne;
  private String propertyTwo;
}

@Slf4j
public class ClassUnderTestService {

  // dependencies that would be injected
  private DependencyOne dependencyOne;
  private DependencyTwo dependencyTwo;

  public List<SomeObject> methodUnderTest(String searchParameter, String filterParameterOne, String filterParameterTwo, String filterParameterThree) {

    if(searchParameter == null) {
      return Collections.emptyList();
    }

    if(filterParameterOne == null ||  filterParameterTwo == null || filterParameterThree == null) {
      throw new IllegalArgumentException("must provide valid filter parameters");
    }

    List<SomeObject> initialSomeOjbects;
    try {
      initialSomeOjbects = dependencyOne.getMatchingObjects(searchParameter);
    }
    catch(RuntimeException e) {
      log.warn("failed to get objects from dependency one, returning empty results", e);
      return Collections.emptyList();
    }

    List<SomeObject> filteredSomeObjects = initialSomeOjbects.stream()
        .filter(someObject -> someObject != null && someObject.getPropertyOne() != null && someObject.getPropertyTwo() != null)
        .filter(someObject -> someObject.getPropertyOne().equals(filterParameterOne) || someObject.getPropertyOne().equals(filterParameterTwo) || someObject.getPropertyOne().equals(filterParameterThree))
        .filter(someObject -> someObject.getPropertyTwo().equals(filterParameterOne) || someObject.getPropertyTwo().equals(filterParameterTwo) || someObject.getPropertyTwo().equals(filterParameterThree))
        .collect(Collectors.toList());

    dependencyTwo.saveObjects(filteredSomeObjects);

    return filteredSomeObjects;

  }

}
