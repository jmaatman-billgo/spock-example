package com.billgo.example.spock

import spock.lang.Specification

// Note: spock tests by convention use the Spec suffix
class ClassUnderTestServiceSpec extends Specification {

    // Note: spock documentation: https://spockframework.org/spock/docs/2.1/index.html
    // Note: no ; required

    // Note: pojos can be created with a parameterized constructor
    private static final SomeObject SOME_OBJECT = new SomeObject(propertyOne: 'foo', propertyTwo: 'bar')


    private ClassUnderTestService service = new ClassUnderTestService()

    // declare mock objects (good practice to name them with mock prefix)
    private DependencyOne mockDependencyOne = Mock()
    private DependencyTwo mockDependencyTwo = Mock()

    // Note: setup method will be invoked prior to every test running
    def setup() {
        // Note: we can directly access private properties and methods of java classes
        service.dependencyOne = mockDependencyOne
        service.dependencyTwo = mockDependencyTwo
    }





    // Note: method names can be defined as strings permitting very easy to read and descriptive test names
    def 'test - methodUnderTest - null search parameter'() {
        expect:
        // Note: can declare empty list with [] and empty map with [:]
        // Note: can replace .equals() with ==
        service.methodUnderTest(null, 'parameter1', 'parameter2', 'parameter3') == []
    }





    def 'test - methodUnderTest - null filter parameters'() {
        when:
        service.methodUnderTest('searchParameter', parameter1, parameter2, parameter3)

        then:
        // Note: thrown() is used to assert that the desired exception was thrown
        // Note: typing is optional, can use 'def' in place of explicit type
        def e = thrown(IllegalArgumentException)
        e.message == 'must provide valid filter parameters'

        // Note: this is a data table, the test will be executed once for each row of the table
        where:
        parameter1 | parameter2 | parameter3
        null       | 'p2'       | 'p3'
        'p1'       | null       | 'p3'
        'p1'       | 'p2'       | null
    }





    def 'test - methodUnderTest - exception thrown from dependencyOne'() {
        when:
        def result = service.methodUnderTest('searchParameter', 'p1', 'p2', 'p3')

        then:
        result == []

        // Note: this is interaction testing, we can assert the exact number of expected interactions with our mock objects
        // Note: the "and:" block is optional, however it provides a nice separation between asserting results and asserting behavior/interactions
        and:
        // Note: in this scenario i've defined that result of the mock is a closure that will be executed when the mock runs
        1 * mockDependencyOne.getMatchingObjects('searchParameter') >> { throw new RuntimeException('search failed') }
        0 * _
    }





    def 'test - methodUnderTest - happy path'() {
        given:
        String searchParameter = 'searchParameter'

        when:
        def results = service.methodUnderTest(searchParameter, 'f1', 'f2', 'f3')

        then:
        results == expectedResults

        and:
        1 * mockDependencyOne.getMatchingObjects(searchParameter) >> searchResults
        1 * mockDependencyTwo.saveObjects(expectedResults)
        0 * _

        where:
        searchResults                                                         | expectedResults
        // first line of filtering removes invalid objects
        []                                                                    | []
        [null]                                                                | []
        [new SomeObject(propertyOne: 'p1')]                                   | []
        [new SomeObject(propertyTwo: 'p2')]                                   | []
        // require both parameters to match
        [new SomeObject(propertyOne: 'foo', propertyTwo: 'bar'), SOME_OBJECT] | []
        [new SomeObject(propertyOne: 'f1', propertyTwo: 'foo'), SOME_OBJECT]  | []
        [new SomeObject(propertyOne: 'f2', propertyTwo: 'foo'), SOME_OBJECT]  | []
        [new SomeObject(propertyOne: 'f3', propertyTwo: 'foo'), SOME_OBJECT]  | []
        [new SomeObject(propertyOne: 'foo', propertyTwo: 'f1'), SOME_OBJECT]  | []
        [new SomeObject(propertyOne: 'foo', propertyTwo: 'f2'), SOME_OBJECT]  | []
        [new SomeObject(propertyOne: 'foo', propertyTwo: 'f2'), SOME_OBJECT]  | []
        // all match permutations
        [new SomeObject(propertyOne: 'f1', propertyTwo: 'f2'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f1', propertyTwo: 'f2')]
        [new SomeObject(propertyOne: 'f1', propertyTwo: 'f3'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f1', propertyTwo: 'f3')]
        [new SomeObject(propertyOne: 'f2', propertyTwo: 'f1'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f2', propertyTwo: 'f1')]
        [new SomeObject(propertyOne: 'f2', propertyTwo: 'f3'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f2', propertyTwo: 'f3')]
        [new SomeObject(propertyOne: 'f3', propertyTwo: 'f1'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f3', propertyTwo: 'f1')]
        [new SomeObject(propertyOne: 'f3', propertyTwo: 'f2'), SOME_OBJECT]   | [new SomeObject(propertyOne: 'f3', propertyTwo: 'f2')]
        // no matches
        [SOME_OBJECT] | []
    }


}
